package net.explorviz.code.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.explorviz.code.beans.CommitTree;
import net.explorviz.code.beans.CommitTree.Branch;
import net.explorviz.code.beans.CommitTree.Branch.BranchPoint2;
import net.explorviz.code.mongo.BranchPoint;
import net.explorviz.code.mongo.CommitReport;
import net.explorviz.code.mongo.LatestCommit;

public final class CommitTreeHelper {

  private CommitTreeHelper() {
  }

  public static CommitTree createCommitTree(final String appName, final String landscapeToken) {
    final List<BranchPoint> branchPoints = BranchPoint.findByTokenAndApplicationName(landscapeToken,
        appName);
    final List<Branch> branches = new ArrayList<>();

    // Collect all branch names from branch points
    List<String> branchNames = branchPoints.stream()
        .map(BranchPoint::getBranchName)
        .distinct()
        .collect(Collectors.toList());

    // Fetch all latest commits for these branches
    List<LatestCommit> latestCommits = LatestCommit.findAllLatestCommitsByLandscapeTokenAndApplicationName(
        landscapeToken, appName, branchNames);

    // Map latest commits by branch name for quick access
    Map<String, LatestCommit> latestCommitMap = latestCommits.stream()
        .collect(Collectors.toMap(LatestCommit::getBranchName, Function.identity()));

    // Process each branch point
    for (BranchPoint branchPoint : branchPoints) {
      final String branchName = branchPoint.getBranchName();
      final String branchPointCommitId = branchPoint.getCommitId();
      final String emergedFromBranchName = branchPoint.getEmergedFromBranchName();
      final String emergedFromCommitId = branchPoint.getEmergedFromCommitId();

      final LatestCommit latestCommit = latestCommitMap.get(branchName);

      if (latestCommit == null) {
        continue;
      }

      List<CommitReport> commitReportsForBranch = CommitReport.findByTokenAndApplicationNameAndBranchName(
          landscapeToken, appName, branchName);

      Map<String, CommitReport> commitReportMap = new HashMap<>();
      for (CommitReport report : commitReportsForBranch) {
        commitReportMap.put(report.getCommitId(), report);
      }

      String currentCommitId = latestCommit.getCommitId();
      final Stack<String> commits = new Stack<>();
      boolean finished = false;
      while (!finished) {
        CommitReport currentCommitReport = commitReportMap.get(currentCommitId);
        if (currentCommitReport != null) {
          boolean isCommitReportRelevant = !currentCommitReport.getAdded().isEmpty() ||
              !currentCommitReport.getDeleted().isEmpty() ||
              !currentCommitReport.getModified().isEmpty();

          if (isCommitReportRelevant) {
            commits.push(currentCommitId);
          }

          if (currentCommitId.equals(branchPointCommitId)) {
            finished = true;
          } else {
            currentCommitId = currentCommitReport.getParentCommitId();
          }
        } else {
          break;
        }
      }

      Collections.reverse(commits);
      final BranchPoint2 branchPoint2 = new BranchPoint2(emergedFromBranchName,
          emergedFromCommitId);
      final Branch branch = new Branch(branchName, new ArrayList<>(commits), branchPoint2);
      branches.add(branch);
    }

    return new CommitTree(appName, branches);
  }
}
