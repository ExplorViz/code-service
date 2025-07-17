package net.explorviz.code.helper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import net.explorviz.code.persistence.entity.BranchPoint;
import net.explorviz.code.persistence.entity.CommitReport;
import net.explorviz.code.persistence.entity.LatestCommit;
import net.explorviz.code.persistence.repository.BranchPointRepository;
import net.explorviz.code.persistence.repository.CommitReportRepository;
import net.explorviz.code.persistence.repository.LatestCommitRepository;


@ApplicationScoped
public class CommitComparisonHelper {

  private final LatestCommitRepository latestCommitRepository;
  private final BranchPointRepository branchPointRepository;
  private final CommitReportRepository commitReportRepository;

  @Inject
  public CommitComparisonHelper(final LatestCommitRepository latestCommitRepository,
      final BranchPointRepository branchPointRepository,
      final CommitReportRepository commitReportRepository) {
    this.latestCommitRepository = latestCommitRepository;
    this.branchPointRepository = branchPointRepository;
    this.commitReportRepository = commitReportRepository;
  }

  /**
   * Returns the latest common commit ID between two selected commits for a given landscape and
   * application.
   *
   * @param firstSelectedId The ID of the first selected commit.
   * @param secondSelectedId The ID of the second selected commit.
   * @param landscapeToken The landscape token.
   * @param applicationName The application name.
   * @return The latest common commit ID, or an empty string ("") if no latest common commit exists.
   */
  public String getLatestCommonCommitId(final String firstSelectedId, // NOPMD
      final String secondSelectedId, final String landscapeToken,
      final String applicationName) {

    final CommitReport firstSelected =
        this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(
            landscapeToken, applicationName, firstSelectedId);
    final CommitReport secondSelected =
        this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(
            landscapeToken, applicationName, secondSelectedId);

    if (firstSelected == null || secondSelected == null) {
      return "";
    }

    final List<BranchPoint> firstSelectedBranchPoints = new ArrayList<>();
    final List<BranchPoint> secondSelectedBranchPoints = new ArrayList<>();

    final BranchPoint firstSelectedBranchPoint = new BranchPoint(
        null,
        null,
        firstSelected.branchName(), // emergedFromBranchName initially the same as branchName
        firstSelected.commitId(),   // emergedFromCommitId initially the same as commitId
        null,
        null
    );

    final BranchPoint secondSelectedBranchPoint = new BranchPoint(
        null,
        null,
        secondSelected.branchName(), // emergedFromBranchName initially the same as branchName
        secondSelected.commitId(),   // emergedFromCommitId initially the same as commitId
        null,
        null
    );

    BranchPoint firstSelectedCurrentBranchPoint = firstSelectedBranchPoint; // NOPMD
    BranchPoint secondSelectedCurrentBranchPoint = secondSelectedBranchPoint; // NOPMD

    firstSelectedBranchPoints.add(firstSelectedCurrentBranchPoint);
    while ((firstSelectedCurrentBranchPoint = // NOPMD
        this.branchPointRepository.findByTokenAndBranchName(landscapeToken,
            firstSelectedCurrentBranchPoint
                .emergedFromBranchName())) != null) {
      firstSelectedBranchPoints.add(firstSelectedCurrentBranchPoint);
    }

    secondSelectedBranchPoints.add(secondSelectedCurrentBranchPoint);
    while ((secondSelectedCurrentBranchPoint = // NOPMD
        this.branchPointRepository.findByTokenAndBranchName(
            landscapeToken, secondSelectedCurrentBranchPoint.emergedFromBranchName())) != null) {
      secondSelectedBranchPoints.add(secondSelectedCurrentBranchPoint);
    }

    // latest common branch
    firstSelectedBranchPoints.removeIf(
        b1 -> {
          return secondSelectedBranchPoints.stream().noneMatch(b2 -> b2.emergedFromBranchName()
              .equals(b1.emergedFromBranchName()));
        });

    final String latestCommonBranchName = firstSelectedBranchPoints.getFirst()
        .emergedFromBranchName();
    String firstSelectedCommitInCommonBranch = firstSelectedBranchPoints.getFirst() // NOPMD
        .emergedFromBranchName();
    final List<BranchPoint> temp = secondSelectedBranchPoints.stream()
        .filter(b -> b.emergedFromBranchName()
            .equals(latestCommonBranchName
            )
        )
        .toList();

    if (!temp.isEmpty()) {
      final String secondSelectedCommitInCommonBranch = temp.getFirst() // NOPMD
          .emergedFromCommitId(); // NOPMD

      if (firstSelectedCommitInCommonBranch.equals(secondSelectedCommitInCommonBranch)) {
        return firstSelectedCommitInCommonBranch;
      }

      // the common commit id is the one that comes before the other
      final LatestCommit latestCommit =
          this.latestCommitRepository.findByLandscapeTokenAndApplicationNameAndBranchName(
              landscapeToken,
              applicationName, latestCommonBranchName);
      if (latestCommit == null) {
        return "";
      }
      String currentCommit = latestCommit.commitId();

      CommitReport cr;
      while ((cr =
          this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(landscapeToken,
              // NOPMD
              applicationName, currentCommit)) != null
          &&
          !cr.commitId().equals(firstSelectedCommitInCommonBranch)
          &&
          !cr.commitId().equals(secondSelectedCommitInCommonBranch)
      ) {
        currentCommit = cr.parentCommitId();
      }

      if (cr != null && cr.commitId().equals(firstSelectedCommitInCommonBranch)) {
        return secondSelectedCommitInCommonBranch;
      }

      if (cr != null && cr.commitId().equals(secondSelectedCommitInCommonBranch)) {
        return firstSelectedCommitInCommonBranch;
      }

      return "";
    }
    return "";
  }

  /**
   * Retrieves the list of file names that have been added in the second selected commit (i.e., that
   * exist in the second selected commit but not in the first selected commit).
   *
   * @param firstSelectedCommitId The first selected commit's ID.
   * @param secondSelectedCommitId The second selected commit's ID.
   * @param landscapeToken The landscape token.
   * @param applicationName The application name.
   * @return The list of file names that have been added in the second selected commit.
   */
  public List<String> getComparisonAddedFiles(final String firstSelectedCommitId, // NOPMD
      final String secondSelectedCommitId, final String landscapeToken,
      final String applicationName) {

    final List<String> addedFiles = new ArrayList<>();

    final CommitReport commitReportFirstSelectedCommit = this.commitReportRepository // NOPMD
        .findByTokenAndApplicationNameAndCommitId(landscapeToken, applicationName,
            firstSelectedCommitId);

    final CommitReport commitReportSecondSelectedCommit = this.commitReportRepository // NOPMD
        .findByTokenAndApplicationNameAndCommitId(landscapeToken, applicationName,
            secondSelectedCommitId);

    if (commitReportFirstSelectedCommit != null
        &&
        commitReportSecondSelectedCommit != null) {

      final List<String> filesInFirstSelectedCommit = commitReportFirstSelectedCommit.files();
      final List<String> filesInSecondSelectedCommit = commitReportSecondSelectedCommit.files();
      filesInSecondSelectedCommit.forEach(file -> {

        if (!filesInFirstSelectedCommit.contains(file)) {
          addedFiles.add(file);
        }
      });
    }

    return addedFiles;
  }

  /**
   * Retrieves the list of file names that have been deleted in the second selected commit (i.e.,
   * that do not exist in the second selected commit but in the first selected commit).
   *
   * @param firstSelectedCommitId The first selected commit's ID.
   * @param secondSelectedCommitId The second selected commit's ID.
   * @param landscapeToken The landscape token.
   * @param applicationName The application name.
   * @return The list of file names that have been deleted in the second selected commit.
   */
  public List<String> getComparisonDeletedFiles(final String firstSelectedCommitId, // NOPMD
      final String secondSelectedCommitId, final String landscapeToken,
      final String applicationName) {

    final List<String> deletedFiles = new ArrayList<>();
    final CommitReport commitReportFirstSelectedCommit = this.commitReportRepository // NOPMD
        .findByTokenAndApplicationNameAndCommitId(landscapeToken, applicationName,
            firstSelectedCommitId);

    final CommitReport commitReportSecondSelectedCommit = this.commitReportRepository // NOPMD
        .findByTokenAndApplicationNameAndCommitId(landscapeToken, applicationName,
            secondSelectedCommitId);

    if (commitReportFirstSelectedCommit != null
        &&
        commitReportSecondSelectedCommit != null) {
      final List<String> filesInFirstSelectedCommit = commitReportFirstSelectedCommit.files();
      final List<String> filesInSecondSelectedCommit = commitReportSecondSelectedCommit.files();
      filesInFirstSelectedCommit.forEach(file -> {
        if (!filesInSecondSelectedCommit.contains(file)) {
          deletedFiles.add(file);
        }
      });
    }

    return deletedFiles;
  }

  /**
   * Retrieves the list of file names that have been modified in the second selected commit (i.e.,
   * files that exist in both commits but have a different hash value).
   *
   * @param firstSelectedCommitId The first selected commit's ID.
   * @param secondSelectedCommitId The second selected commit's ID.
   * @param landscapeToken The landscape token.
   * @param applicationName The application name.
   * @return The list of file names that have been modified in the second selected commit.
   */
  public List<String> getComparisonModifiedFiles(final String firstSelectedCommitId, // NOPMD
      final String secondSelectedCommitId, final String landscapeToken,
      final String applicationName) {

    final List<String> modifiedFiles = new ArrayList<>();
    final CommitReport commitReportFirstSelectedCommit = this.commitReportRepository // NOPMD
        .findByTokenAndApplicationNameAndCommitId(landscapeToken, applicationName,
            firstSelectedCommitId);

    final CommitReport commitReportSecondSelectedCommit = this.commitReportRepository // NOPMD
        .findByTokenAndApplicationNameAndCommitId(landscapeToken, applicationName,
            secondSelectedCommitId);

    if (commitReportFirstSelectedCommit != null
        &&
        commitReportSecondSelectedCommit != null) {
      final List<String> filesInFirstSelectedCommit = commitReportFirstSelectedCommit.files();
      final List<String> filesInSecondSelectedCommit = commitReportSecondSelectedCommit.files();

      filesInFirstSelectedCommit.forEach(file -> {
        if (filesInSecondSelectedCommit.contains(file)) {
          final int indexFirstSelected = filesInFirstSelectedCommit.indexOf(file);
          final int indexSecondSelected = filesInSecondSelectedCommit.indexOf(file);
          final String fileHashFirstSelected = commitReportFirstSelectedCommit.fileHash()
              .get(indexFirstSelected);
          final String fileHashSecondSelected = commitReportSecondSelectedCommit.fileHash()
              .get(indexSecondSelected);
          if (!fileHashFirstSelected.equals(fileHashSecondSelected)) { // NOPMD
            modifiedFiles.add(file);
          }
        }
      });
    }
    return modifiedFiles;
  }


}
