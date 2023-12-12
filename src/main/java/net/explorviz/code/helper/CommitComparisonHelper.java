package net.explorviz.code.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.explorviz.code.mongo.BranchPoint;
import net.explorviz.code.mongo.CommitReport;
import net.explorviz.code.mongo.LatestCommit;

/**
 * ...
 */
public final class CommitComparisonHelper {

  private CommitComparisonHelper() {
  }

  /**
   * ...
   ** @param firstSelectedId ...
   ** @param secondSelectedId ...
   ** @return returns the empty string ("") if no latest common commit existent, otherwise its id
   * 
   */


  public static String getLatestCommonCommitId(final String firstSelectedId, // NOPMD
      final String secondSelectedId, final String landscapeToken,
      final String applicationName) {

    final CommitReport firstSelected = CommitReport.findByTokenAndApplicationNameAndCommitId(
          landscapeToken, applicationName, firstSelectedId);
    final CommitReport secondSelected = CommitReport.findByTokenAndApplicationNameAndCommitId(
         landscapeToken, applicationName, secondSelectedId);

    if (firstSelected == null || secondSelected == null) {
      return "";
    }

    final List<BranchPoint> firstSelectedBranchPoints = new ArrayList<>();
    final List<BranchPoint> secondSelectedBranchPoints = new ArrayList<>();

    final BranchPoint firstSelectedBranchPoint = new BranchPoint();
    firstSelectedBranchPoint.setEmergedFromBranchName(firstSelected.getBranchName());
    firstSelectedBranchPoint.setEmergedFromCommitId(firstSelected.getCommitId());

    final BranchPoint secondSelectedBranchPoint = new BranchPoint();
    secondSelectedBranchPoint.setEmergedFromBranchName(secondSelected.getBranchName());
    secondSelectedBranchPoint.setEmergedFromCommitId(secondSelected.getCommitId());

    BranchPoint firstSelectedCurrentBranchPoint = firstSelectedBranchPoint; // NOPMD
    BranchPoint secondSelectedCurrentBranchPoint = secondSelectedBranchPoint; // NOPMD

    firstSelectedBranchPoints.add(firstSelectedCurrentBranchPoint);
    while ((firstSelectedCurrentBranchPoint = // NOPMD
    BranchPoint.findByTokenAndBranchName(landscapeToken, firstSelectedCurrentBranchPoint
        .getEmergedFromBranchName())) != null) {
      firstSelectedBranchPoints.add(firstSelectedCurrentBranchPoint);
    }

    secondSelectedBranchPoints.add(secondSelectedCurrentBranchPoint);
    while ((secondSelectedCurrentBranchPoint = // NOPMD
    BranchPoint.findByTokenAndBranchName(
      landscapeToken, secondSelectedCurrentBranchPoint.getEmergedFromBranchName())) != null) {
      secondSelectedBranchPoints.add(secondSelectedCurrentBranchPoint);
    }

    // latest common branch
    firstSelectedBranchPoints.removeIf(
        b1 -> {
        return secondSelectedBranchPoints.stream()
                                  .filter(b2 -> b2.getEmergedFromBranchName()
                                                  .equals(b1.getEmergedFromBranchName()))
                                  .collect(Collectors.toList())
                                  .size() == 0;
      });
    

    final String latestCommonBranchName = firstSelectedBranchPoints.get(0)
        .getEmergedFromBranchName();
    String firstSelectedCommitInCommonBranch = firstSelectedBranchPoints.get(0) // NOPMD
        .getEmergedFromCommitId();
    final List<BranchPoint> temp = secondSelectedBranchPoints.stream()
                                                        .filter(b -> b.getEmergedFromBranchName()
                                                                      .equals(latestCommonBranchName
                                                               )
                                                        )
                                                        .collect(Collectors.toList());
                     
    if (!temp.isEmpty()) {
      final String secondSelectedCommitInCommonBranch = temp.get(0) // NOPMD
          .getEmergedFromCommitId(); // NOPMD

      if (firstSelectedCommitInCommonBranch.equals(secondSelectedCommitInCommonBranch)) {
        return firstSelectedCommitInCommonBranch;
      }

      // the common commit id is the one that comes before the other
      final LatestCommit latestCommit = 
          LatestCommit.findByBranchNameAndLandscapeToken(latestCommonBranchName, landscapeToken);
      if (latestCommit == null) {
        return "";
      }
      String currentCommit = latestCommit.getCommitId();
      
      CommitReport cr;
      while ((cr = CommitReport.findByTokenAndApplicationNameAndCommitId(landscapeToken, // NOPMD 
         applicationName, currentCommit)) != null 
        &&
        !cr.getCommitId().equals(firstSelectedCommitInCommonBranch) 
        && 
        !cr.getCommitId().equals(secondSelectedCommitInCommonBranch)
      ) {
        currentCommit = cr.getParentCommitId();
      }

      if (cr.getCommitId().equals(firstSelectedCommitInCommonBranch)) {
        return secondSelectedCommitInCommonBranch;
      }

      if (cr.getCommitId().equals(secondSelectedCommitInCommonBranch)) {
        return firstSelectedCommitInCommonBranch;
      }

      return "";
    } 
    return "";                                              
  }

  /**
   * ...
   ** @param firstSelectedCommitId ...
   ** @param secondSelectedCommitId ...
   ** @param landscapeToken ...
   ** @return ...
   */
  public static List<String> getComparisonAddedFiles(final String firstSelectedCommitId, // NOPMD
        final String secondSelectedCommitId, final String landscapeToken, 
        final String applicationName) {

    final CommitReport commitReportFirstSelectedCommit = CommitReport // NOPMD
        .findByTokenAndApplicationNameAndCommitId(landscapeToken, applicationName, 
            firstSelectedCommitId);
    
    final CommitReport commitReportSecondSelectedCommit = CommitReport // NOPMD
        .findByTokenAndApplicationNameAndCommitId(landscapeToken, applicationName, 
            secondSelectedCommitId);

    if (commitReportFirstSelectedCommit == null 
        || 
        commitReportSecondSelectedCommit == null) {
      return null;
    }

    final List<String> filesInFirstSelectedCommit = commitReportFirstSelectedCommit.getFiles();
    final List<String> filesInSecondSelectedCommit = commitReportSecondSelectedCommit.getFiles();
    final List<String> addedFiles = new ArrayList<>();
    filesInSecondSelectedCommit.forEach(file -> {
      if (!filesInFirstSelectedCommit.contains(file)) {
        addedFiles.add(file);
      }
    });
    return addedFiles;
  }

  /**
   * ...
   ** @param firstSelectedCommitId ...
   ** @param secondSelectedCommitId ...
   ** @param landscapeToken ...
   ** @return ...
   */
  public static List<String> getComparisonDeletedFiles(final String firstSelectedCommitId, // NOPMD
        final String secondSelectedCommitId, final String landscapeToken, 
        final String applicationName) {
    
    final CommitReport commitReportFirstSelectedCommit = CommitReport // NOPMD
        .findByTokenAndApplicationNameAndCommitId(landscapeToken, applicationName, 
            firstSelectedCommitId);
    
    final CommitReport commitReportSecondSelectedCommit = CommitReport // NOPMD
        .findByTokenAndApplicationNameAndCommitId(landscapeToken, applicationName, 
            secondSelectedCommitId);

    if (commitReportFirstSelectedCommit == null 
        || 
        commitReportSecondSelectedCommit == null) {
      return null;
    }

    final List<String> filesInFirstSelectedCommit = commitReportFirstSelectedCommit.getFiles();
    final List<String> filesInSecondSelectedCommit = commitReportSecondSelectedCommit.getFiles();
    final List<String> deletedFiles = new ArrayList<>();
    filesInFirstSelectedCommit.forEach(file -> {
      if (!filesInSecondSelectedCommit.contains(file)) {
        deletedFiles.add(file);
      }
    });
    return deletedFiles;
  }


  /**
   * ...
   ** @param firstSelectedCommitId ...
   ** @param secondSelectedCommitId ...
   ** @param landscapeToken ...
   ** @return ...
   */
  public static List<String> getComparisonModifiedFiles(final String firstSelectedCommitId, // NOPMD
        final String secondSelectedCommitId, final String landscapeToken, 
        final String applicationName) {

    final CommitReport commitReportFirstSelectedCommit = CommitReport // NOPMD
        .findByTokenAndApplicationNameAndCommitId(landscapeToken, applicationName, 
            firstSelectedCommitId);
    
    final CommitReport commitReportSecondSelectedCommit = CommitReport // NOPMD
        .findByTokenAndApplicationNameAndCommitId(landscapeToken, applicationName, 
            secondSelectedCommitId);

    if (commitReportFirstSelectedCommit == null 
        || 
        commitReportSecondSelectedCommit == null) {
      return null;
    }

    final List<String> filesInFirstSelectedCommit = commitReportFirstSelectedCommit.getFiles();
    final List<String> filesInSecondSelectedCommit = commitReportSecondSelectedCommit.getFiles();
    final List<String> modifiedFiles = new ArrayList<>();



    filesInFirstSelectedCommit.forEach(file -> {
      if (filesInSecondSelectedCommit.contains(file)) {
        final int indexFirstSelected = filesInFirstSelectedCommit.indexOf(file);
        final int indexSecondSelected = filesInSecondSelectedCommit.indexOf(file);
        final String fileHashFirstSelected = commitReportFirstSelectedCommit.getFileHash()
            .get(indexFirstSelected);
        final String fileHashSecondSelected = commitReportSecondSelectedCommit.getFileHash()
            .get(indexSecondSelected);
        if (!fileHashFirstSelected.equals(fileHashSecondSelected)) {
          modifiedFiles.add(file);
        }
      }
    });
    return modifiedFiles;
  }





}
