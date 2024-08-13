package net.explorviz.code.helper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.explorviz.code.dto.commit.tree.BranchDto;
import net.explorviz.code.dto.commit.tree.BranchPointDto;
import net.explorviz.code.dto.commit.tree.CommitTree;
import net.explorviz.code.persistence.BranchPoint;
import net.explorviz.code.persistence.CommitReport;
import net.explorviz.code.persistence.entity.LatestCommit;
import net.explorviz.code.persistence.repository.LatestCommitRepository;

/**
 * Helper class that calculates the commit tree.
 */
@ApplicationScoped
public class CommitTreeHelper {

  private final LatestCommitRepository latestCommitRepository;

  @Inject
  public CommitTreeHelper(final LatestCommitRepository latestCommitRepository) {
    this.latestCommitRepository = latestCommitRepository;
  }

  /**
   * Calculates the CommitTree, i.e., all relevant commits and branches, for a given token and
   * appName.
   *
   * @param appName        application name
   * @param landscapeToken landscape token value
   * @return CommitTree for the given parameters.
   */
  public CommitTree createCommitTree(final String appName, final String landscapeToken) {
    final List<BranchPoint> branchPoints = BranchPoint.findByTokenAndApplicationName(landscapeToken,
        appName);
    final List<BranchDto> branches = new ArrayList<>();

    // Collect all branch names from branch points
    List<String> branchNames = branchPoints.stream()
        .map(BranchPoint::getBranchName)
        .distinct()
        .collect(Collectors.toList());

    // Fetch all latest commits for these branches
    List<LatestCommit> latestCommits = this.latestCommitRepository
        .findAllLatestCommitsByLandscapeTokenAndApplicationName(
            landscapeToken, appName, branchNames);

    // Map latest commits by branch name for quick access
    Map<String, LatestCommit> latestCommitMap = latestCommits.stream()
        .collect(Collectors.toMap(LatestCommit::branchName, Function.identity()));

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

      List<CommitReport> commitReportsForBranch = CommitReport
          .findByTokenAndApplicationNameAndBranchName(
              landscapeToken, appName, branchName);

      Map<String, CommitReport> commitReportMap = new HashMap<>();
      for (CommitReport report : commitReportsForBranch) {
        commitReportMap.put(report.getCommitId(), report);
      }

      String currentCommitId = latestCommit.commitId();
      final Stack<String> commits = new Stack<>();
      boolean finished = false;
      while (!finished) {
        CommitReport currentCommitReport = commitReportMap.get(currentCommitId);
        if (currentCommitReport != null) {
          boolean isCommitReportRelevant = !currentCommitReport.getAdded().isEmpty()
              || !currentCommitReport.getDeleted().isEmpty()
              || !currentCommitReport.getModified().isEmpty();

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
      final BranchPointDto branchPointDto = new BranchPointDto(emergedFromBranchName,
          emergedFromCommitId);
      final BranchDto branch = new BranchDto(branchName, new ArrayList<>(commits), branchPointDto);
      branches.add(branch);
    }

    return new CommitTree(appName, branches);
  }
}
