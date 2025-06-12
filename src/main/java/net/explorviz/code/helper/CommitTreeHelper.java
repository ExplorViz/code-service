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
import net.explorviz.code.persistence.entity.BranchPoint;
import net.explorviz.code.persistence.entity.CommitReport;
import net.explorviz.code.persistence.entity.LatestCommit;
import net.explorviz.code.persistence.repository.BranchPointRepository;
import net.explorviz.code.persistence.repository.CommitReportRepository;
import net.explorviz.code.persistence.repository.LatestCommitRepository;

/**
 * Helper class that calculates the commit tree.
 */
@ApplicationScoped
public class CommitTreeHelper {

  private final LatestCommitRepository latestCommitRepository;
  private final BranchPointRepository branchPointRepository;
  private final CommitReportRepository commitReportRepository;


  @Inject
  public CommitTreeHelper(final LatestCommitRepository latestCommitRepository,
      final BranchPointRepository branchPointRepository,
      final CommitReportRepository commitReportRepository) {
    this.latestCommitRepository = latestCommitRepository;
    this.branchPointRepository = branchPointRepository;
    this.commitReportRepository = commitReportRepository;
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
    final List<BranchPoint> branchPoints =
        this.branchPointRepository.findByTokenAndApplicationName(landscapeToken,
            appName);
    final List<BranchDto> branches = new ArrayList<>();

    // Collect all branch names from branch points
    List<String> branchNames = branchPoints.stream()
        .map(BranchPoint::branchName)
        .distinct()
        .collect(Collectors.toList());

    // Fetch all latest commits for these branches
    List<LatestCommit> latestCommits = this.latestCommitRepository
        .findAllLatestCommitsByLandscapeTokenAndApplicationName(
            landscapeToken, appName, branchNames);

    /*
    // DEBUGGING: Check for duplicate branches
    Map<String, List<LatestCommit>> grouped = latestCommits.stream()
    .collect(Collectors.groupingBy(LatestCommit::branchName));

    grouped.entrySet().stream()
        .filter(e -> e.getValue().size() > 1)
        .forEach(e -> System.out.println("Duplicate branch: " + e.getKey() + " -> " + e.getValue()));
    // END DEBUGGING
    */

    // Map latest commits by branch name for quick access
    Map<String, LatestCommit> latestCommitMap = latestCommits.stream()
        .collect(Collectors.toMap(LatestCommit::branchName, Function.identity()/*, (existing, duplicate) -> existing)
        */)); // TODO: We had a bug where identical entries were added to the map. We still need to find out why so we don't have to use this workaround.

    // Process each branch point
    for (BranchPoint branchPoint : branchPoints) {
      final String branchName = branchPoint.branchName();
      final String branchPointCommitId = branchPoint.commitId();
      final String emergedFromBranchName = branchPoint.emergedFromBranchName();
      final String emergedFromCommitId = branchPoint.emergedFromCommitId();

      final LatestCommit latestCommit = latestCommitMap.get(branchName);

      if (latestCommit == null) {
        continue;
      }

      List<CommitReport> commitReportsForBranch = this.commitReportRepository
          .findByTokenAndApplicationNameAndBranchName(
              landscapeToken, appName, branchName);

      Map<String, CommitReport> commitReportMap = new HashMap<>();
      for (CommitReport report : commitReportsForBranch) {
        commitReportMap.put(report.commitId(), report);
      }

      String currentCommitId = latestCommit.commitId();
      final Stack<String> commits = new Stack<>();
      boolean finished = false;
      while (!finished) {
        CommitReport currentCommitReport = commitReportMap.get(currentCommitId);
        if (currentCommitReport != null) {
          boolean isCommitReportRelevant = !currentCommitReport.added().isEmpty()
              || !currentCommitReport.deleted().isEmpty()
              || !currentCommitReport.modified().isEmpty();

          if (isCommitReportRelevant) {
            commits.push(currentCommitId);
          }

          if (currentCommitId.equals(branchPointCommitId)) {
            finished = true;
          } else {
            currentCommitId = currentCommitReport.parentCommitId();
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
