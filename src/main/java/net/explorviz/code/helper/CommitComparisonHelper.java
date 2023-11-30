package net.explorviz.code.helper;

import java.util.ArrayList;
import java.util.List;
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
  public static String latestCommonCommitId(final String firstSelectedId, 
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





}
