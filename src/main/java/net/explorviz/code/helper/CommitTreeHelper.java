package net.explorviz.code.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import net.explorviz.code.beans.CommitTree;
import net.explorviz.code.beans.CommitTree.Branch;
import net.explorviz.code.beans.CommitTree.Branch.BranchPoint2;
import net.explorviz.code.mongo.BranchPoint;
import net.explorviz.code.mongo.CommitReport;
import net.explorviz.code.mongo.LatestCommit;

/**
 * .
 */
public class CommitTreeHelper {

  /**
   * ...
   ** @return ..
   */
  public static CommitTree createCommitTree(final String appName/*, final String landscapeToken*/) {
    final List<BranchPoint> branchPoints = BranchPoint.listAll(); /* TODO: filter 
                                                                   based on landscapeToken and 
                                                                   appName*/
    final List<Branch> branches = new ArrayList<>();

    branchPoints.forEach(branchPoint -> {
      final String branchName = branchPoint.branchName;
      final String branchPointCommitId = branchPoint.commitId;
      final String landscapeToken = branchPoint.landscapeToken; // TODO: use parameter
      final String emergedFromBranchName = branchPoint.emergedFromBranchName;
      final String emergedFromCommitId = branchPoint.emergedFromCommitId;
      LatestCommit latestCommit = LatestCommit
          .findByBranchNameAndLandscapeToken(branchName, landscapeToken);

      // now iterate from latestCommit to branchPoint commit
      String currentCommitId = latestCommit.commitId;
      Stack<String> commits = new Stack<>();
      Boolean finished = false;
      while (!finished) {
        commits.add(currentCommitId);
        if (currentCommitId.equals(branchPointCommitId)) {
          finished = true;
        } else {
          CommitReport currenCommitReport = CommitReport.findByCommitId(currentCommitId);
          if (currenCommitReport != null) {
            // should always be the case
            currentCommitId = currenCommitReport.parentCommitId;
          } else {
            break;
          }
        }
      }

      List<String> commitList = new ArrayList<String>(commits);
      Collections.reverse(commitList);
      BranchPoint2 branchPoint2 = new BranchPoint2(emergedFromBranchName, emergedFromCommitId);
      Branch branch = new Branch(branchName, commitList, branchPoint2);
      branches.add(branch);
    });

    return new CommitTree(appName, branches);
  }
}
