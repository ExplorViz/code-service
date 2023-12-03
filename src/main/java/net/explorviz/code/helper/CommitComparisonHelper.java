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
  private static String getLatestCommonCommitId(final String firstSelectedId, 
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
    String latestCommonCommit = getLatestCommonCommitId(firstSelectedCommitId, 
        secondSelectedCommitId, landscapeToken);

    if (latestCommonCommit.equals("")) {
      return null;
    }
    
    //CommitReport commitReportFirstSelectedCommit = CommitReport.findByCommitId(firstSelectedCommitId);
    // if (commitReportFirstSelectedCommit == null) {
    //   return null;
    // }
    // List<String> filesFirstSelectedCommit = commitReportFirstSelectedCommit.getFiles();

    // TODO: ? collect all modified files from latest common commit to second selected commit
    // Each one of them that do not exist in first selected commit need to be marked as added

    // mark all files that do not exist in first selected commit but that do exist in second 
    // the selected commit as added


    CommitReport commitReportSecondSelectedCommit = CommitReport
        .findByCommitId(secondSelectedCommitId);

    if (commitReportSecondSelectedCommit == null) {
      return null;
    }

    Stack<String> commitIds = new Stack<String>();
    CommitReport currentCommitReport = commitReportSecondSelectedCommit;
    commitIds.push(currentCommitReport.commitId);
    while ((currentCommitReport = CommitReport.findByCommitId(
          currentCommitReport.getParentCommitId())) != null 
          && !currentCommitReport.commitId.equals(latestCommonCommit)) {
      commitIds.push(currentCommitReport.commitId);
    }

    if (!currentCommitReport.commitId.equals(latestCommonCommit)) {
      // should never appear if we have a non-empty latestCommonCommit string
      return null;
    }

    List<String> addedFiles = new ArrayList<>();
    String commitId;
    while (!commitIds.isEmpty()) {
      commitId = commitIds.pop();
      currentCommitReport = CommitReport.findByCommitId(commitId);

      currentCommitReport.deleted.forEach(fqnFileName -> {
        addedFiles.remove(fqnFileName); 
      });

      currentCommitReport.added.forEach(fqnFileName -> {
        if (!addedFiles.contains(fqnFileName)) {
          addedFiles.add(fqnFileName);
        }
      });
    }

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
    String latestCommonCommit = getLatestCommonCommitId(firstSelectedCommitId, 
        secondSelectedCommitId, landscapeToken);

    if (latestCommonCommit.equals("")) {
      return null;
    }
    
    CommitReport commitReportSecondSelectedCommit = CommitReport
        .findByCommitId(secondSelectedCommitId);

    if (commitReportSecondSelectedCommit == null) {
      return null;
    }

    Stack<String> commitIds = new Stack<String>();
    CommitReport currentCommitReport = commitReportSecondSelectedCommit;
    commitIds.push(currentCommitReport.commitId);
    while ((currentCommitReport = CommitReport.findByCommitId(
          currentCommitReport.getParentCommitId())) != null 
          && !currentCommitReport.commitId.equals(latestCommonCommit)) {
      commitIds.push(currentCommitReport.commitId);
    }

    if (!currentCommitReport.commitId.equals(latestCommonCommit)) {
      // should never appear if we have a non-empty latestCommonCommit string
      return null;
    }

    List<String> deletedFiles = new ArrayList<>();
    String commitId;
    while (!commitIds.isEmpty()) {
      commitId = commitIds.pop();
      currentCommitReport = CommitReport.findByCommitId(commitId);

      currentCommitReport.added.forEach(fqnFileName -> {
        deletedFiles.remove(fqnFileName);
      });

      currentCommitReport.deleted.forEach(fqnFileName -> {
        if (!deletedFiles.contains(fqnFileName)) { // make sure only one fqn exists at a time
          deletedFiles.add(fqnFileName);
        }
      });
    }

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
    String latestCommonCommit = getLatestCommonCommitId(firstSelectedCommitId, 
        secondSelectedCommitId, landscapeToken);

    if (latestCommonCommit.equals("")) {
      return null;
    }
    
    CommitReport commitReportSecondSelectedCommit = CommitReport
        .findByCommitId(secondSelectedCommitId);

    if (commitReportSecondSelectedCommit == null) {
      return null;
    }

    Stack<String> commitIds = new Stack<String>();
    CommitReport currentCommitReport = commitReportSecondSelectedCommit;
    commitIds.push(currentCommitReport.commitId);
    while ((currentCommitReport = CommitReport.findByCommitId(
          currentCommitReport.getParentCommitId())) != null 
          && !currentCommitReport.commitId.equals(latestCommonCommit)) {
      commitIds.push(currentCommitReport.commitId);
    }

    if (!currentCommitReport.commitId.equals(latestCommonCommit)) {
      // should never appear if we have a non-empty latestCommonCommit string
      return null;
    }

    List<String> modifiedFiles = new ArrayList<>();
    String commitId;
    while (!commitIds.isEmpty()) {
      commitId = commitIds.pop();
      currentCommitReport = CommitReport.findByCommitId(commitId);

      currentCommitReport.deleted.forEach(fqnFileName -> {
        modifiedFiles.remove(fqnFileName);
      });

      currentCommitReport.modified.forEach(fqnFileName -> {
        if (!modifiedFiles.contains(fqnFileName)) { // make sure only one fqn exists at a time
          modifiedFiles.add(fqnFileName);
        }
      });
    }

    return modifiedFiles;
  }





}
