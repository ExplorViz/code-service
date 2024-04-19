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
public final class CommitTreeHelper {

  private CommitTreeHelper() {
  }

  /**
   * * @param appName the application name. * @param landscapeToken the landscape token. * @return
   * the commit tree matching above params.
   */
  public static CommitTree createCommitTree(final String appName, final String landscapeToken) {
    final List<BranchPoint> branchPoints = BranchPoint
        .findByTokenAndApplicationName(landscapeToken, appName);
    final List<Branch> branches = new ArrayList<>();

    branchPoints.forEach(branchPoint -> {
      final String branchName = branchPoint.getBranchName();
      final String branchPointCommitId = branchPoint.getCommitId(); // NOPMD
      final String emergedFromBranchName = branchPoint.getEmergedFromBranchName(); // NOPMD
      final String emergedFromCommitId = branchPoint.getEmergedFromCommitId(); // NOPMD
      final LatestCommit latestCommit = LatestCommit
          .findByLandscapeTokenAndApplicationNameAndBranchName(landscapeToken, appName, branchName);

      if (latestCommit == null) {
        return;
      }

      // now iterate from latestCommit to branchPoint commit
      String currentCommitId = latestCommit.getCommitId();
      final Stack<String> commits = new Stack<>();
      Boolean finished = false;
      while (!finished) {
        commits.add(currentCommitId);
        if (currentCommitId.equals(branchPointCommitId)) {
          finished = true;
        } else {
          final CommitReport currenCommitReport = CommitReport
              .findByTokenAndApplicationNameAndCommitId(
                  landscapeToken, appName, currentCommitId);
          if (currenCommitReport != null) { // NOPMD
            // should always be the case
            currentCommitId = currenCommitReport.getParentCommitId();
          } else {
            break;
          }
        }
      }

      final List<String> commitList = new ArrayList<String>(commits);
      Collections.reverse(commitList);
      final BranchPoint2 branchPoint2 = new BranchPoint2(emergedFromBranchName,
          emergedFromCommitId);
      final Branch branch = new Branch(branchName, commitList, branchPoint2);
      branches.add(branch);
    });

    return new CommitTree(appName, branches);
  }
}
