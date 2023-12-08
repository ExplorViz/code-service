package net.explorviz.code.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import net.explorviz.code.beans.CommitTree.Branch;
import net.explorviz.code.mongo.BranchPoint;
import net.explorviz.code.mongo.CommitReport;
import net.explorviz.code.mongo.LatestCommit;

/**
 * ...
 */
public class CommitComparisonHelper {

  /**
   * ...
   ** @param firstSelectedId ...
   ** @param secondSelectedId ...
   ** @return returns the empty string ("") if no latest common commit existent, otherwise its id
   * 
   */
  public static String getLatestCommonCommitId(final String firstSelectedId, 
      final String secondSelectedId, final String landscapeToken) {

    final CommitReport firstSelected = CommitReport.findByCommitId(firstSelectedId);
    final CommitReport secondSelected = CommitReport.findByCommitId(secondSelectedId);

    if (firstSelected == null || secondSelected == null) {
      return "";
    }

    final List<BranchPoint> firstSelectedBranchPoints = new ArrayList<>();
    final List<BranchPoint> secondSelectedBranchPoints = new ArrayList<>();

    BranchPoint firstSelectedBranchPoint = new BranchPoint();
    firstSelectedBranchPoint.emergedFromBranchName = firstSelected.branchName;
    firstSelectedBranchPoint.emergedFromCommitId = firstSelected.commitId;

    BranchPoint secondSelectedBranchPoint = new BranchPoint();
    secondSelectedBranchPoint.emergedFromBranchName = secondSelected.branchName;
    secondSelectedBranchPoint.emergedFromCommitId = secondSelected.commitId;

    BranchPoint firstSelectedCurrentBranchPoint = firstSelectedBranchPoint;
    BranchPoint secondSelectedCurrentBranchPoint = secondSelectedBranchPoint;

    firstSelectedBranchPoints.add(firstSelectedCurrentBranchPoint);
    while ((firstSelectedCurrentBranchPoint = 
    BranchPoint.findByBranchName(firstSelectedCurrentBranchPoint.emergedFromBranchName)) != null) {
      firstSelectedBranchPoints.add(firstSelectedCurrentBranchPoint);
    }

    secondSelectedBranchPoints.add(secondSelectedCurrentBranchPoint);
    while ((secondSelectedCurrentBranchPoint = 
    BranchPoint.findByBranchName(secondSelectedCurrentBranchPoint.emergedFromBranchName)) != null) {
      secondSelectedBranchPoints.add(secondSelectedCurrentBranchPoint);
    }

    // latest common branch
    firstSelectedBranchPoints.removeIf(
        b1 -> {
        return secondSelectedBranchPoints.stream()
                                  .filter(b2 -> b2.emergedFromBranchName
                                                  .equals(b1.emergedFromBranchName))
                                  .collect(Collectors.toList())
                                  .size() == 0;
      });
    

    String latestCommonBranchName = firstSelectedBranchPoints.get(0).emergedFromBranchName;
    String firstSelectedCommitInCommonBranch = firstSelectedBranchPoints.get(0).emergedFromCommitId;
    List<BranchPoint> temp = secondSelectedBranchPoints.stream()
                                                        .filter(b -> b.emergedFromBranchName
                                                                      .equals(latestCommonBranchName
                                                               )
                                                        )
                                                        .collect(Collectors.toList());
                     
    if (temp.size() > 0) {
      String secondSelectedCommitInCommonBranch = temp.get(0).emergedFromCommitId;

      if (firstSelectedCommitInCommonBranch.equals(secondSelectedCommitInCommonBranch)) {
        return firstSelectedCommitInCommonBranch;
      }

      // the common commit id is the one that comes before the other
      LatestCommit latestCommit = 
          LatestCommit.findByBranchNameAndLandscapeToken(latestCommonBranchName, landscapeToken);
      if (latestCommit == null) {
        return "";
      }
      String currentCommit = latestCommit.commitId;
      
      CommitReport cr;
      while ((cr = CommitReport.findByCommitId(currentCommit)) != null 
        &&
        !cr.commitId.equals(firstSelectedCommitInCommonBranch) 
        && 
        !cr.commitId.equals(secondSelectedCommitInCommonBranch)
      ){
        currentCommit = cr.parentCommitId;
      }

      if (cr.commitId.equals(firstSelectedCommitInCommonBranch)) {
        return secondSelectedCommitInCommonBranch;
      }

      if (cr.commitId.equals(secondSelectedCommitInCommonBranch)) {
        return firstSelectedCommitInCommonBranch;
      }

      return "";
    } else {
      return "";
    }                                                   
  }

  /**
   * ...
   ** @param firstSelectedCommitId ...
   ** @param secondSelectedCommitId ...
   ** @param landscapeToken ...
   ** @return ...
   */
  public static List<String> getComparisonAddedFiles(String firstSelectedCommitId, 
        String secondSelectedCommitId, String landscapeToken) {

    CommitReport commitReportFirstSelectedCommit = CommitReport
        .findByCommitId(firstSelectedCommitId);
    
    CommitReport commitReportSecondSelectedCommit = CommitReport
        .findByCommitId(secondSelectedCommitId);

    if (commitReportFirstSelectedCommit == null 
        || 
        commitReportSecondSelectedCommit == null) {
      return null;
    }

    List<String> filesInFirstSelectedCommit = commitReportFirstSelectedCommit.files;
    List<String> filesInSecondSelectedCommit = commitReportSecondSelectedCommit.files;
    List<String> addedFiles = new ArrayList<>();
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
  public static List<String> getComparisonDeletedFiles(String firstSelectedCommitId, 
        String secondSelectedCommitId, String landscapeToken) {
    
    CommitReport commitReportFirstSelectedCommit = CommitReport
        .findByCommitId(firstSelectedCommitId);
    
    CommitReport commitReportSecondSelectedCommit = CommitReport
        .findByCommitId(secondSelectedCommitId);

    if (commitReportFirstSelectedCommit == null 
        || 
        commitReportSecondSelectedCommit == null) {
      return null;
    }

    List<String> filesInFirstSelectedCommit = commitReportFirstSelectedCommit.files;
    List<String> filesInSecondSelectedCommit = commitReportSecondSelectedCommit.files;
    List<String> deletedFiles = new ArrayList<>();
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
  public static List<String> getComparisonModifiedFiles(String firstSelectedCommitId, 
        String secondSelectedCommitId, String landscapeToken) {

    CommitReport commitReportFirstSelectedCommit = CommitReport
        .findByCommitId(firstSelectedCommitId);
    
    CommitReport commitReportSecondSelectedCommit = CommitReport
        .findByCommitId(secondSelectedCommitId);

    if (commitReportFirstSelectedCommit == null 
        || 
        commitReportSecondSelectedCommit == null) {
      return null;
    }

    List<String> filesInFirstSelectedCommit = commitReportFirstSelectedCommit.files;
    List<String> filesInSecondSelectedCommit = commitReportSecondSelectedCommit.files;
    List<String> modifiedFiles = new ArrayList<>();



    filesInFirstSelectedCommit.forEach(file -> {
      if (filesInSecondSelectedCommit.contains(file)) {
        int indexFirstSelected = filesInFirstSelectedCommit.indexOf(file);
        int indexSecondSelected = filesInSecondSelectedCommit.indexOf(file);
        String fileHashFirstSelected = commitReportFirstSelectedCommit.fileHash
            .get(indexFirstSelected);
        String fileHashSecondSelected = commitReportSecondSelectedCommit.fileHash
            .get(indexSecondSelected);
        if (!fileHashFirstSelected.equals(fileHashSecondSelected)) {
          modifiedFiles.add(file);
        }
      }
    });
    return modifiedFiles;
  }





}
