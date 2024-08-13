package net.explorviz.code.helper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import net.explorviz.code.persistence.BranchPoint;
import net.explorviz.code.persistence.CommitReport;
import net.explorviz.code.persistence.entity.LatestCommit;
import net.explorviz.code.persistence.repository.LatestCommitRepository;


@ApplicationScoped
public class CommitComparisonHelper {

  private final LatestCommitRepository latestCommitRepository;

  @Inject
  public CommitComparisonHelper(final LatestCommitRepository latestCommitRepository) {
    this.latestCommitRepository = latestCommitRepository;
  }

  /**
   * ... * @param firstSelectedId the first selected commit id. * @param secondSelectedId the second
   * selected commit id. * @return returns the empty string ("") if no latest common commit
   * existent, otherwise its latest common commit id.
   */


  public String getLatestCommonCommitId(final String firstSelectedId, // NOPMD
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
          return secondSelectedBranchPoints.stream().noneMatch(b2 -> b2.getEmergedFromBranchName()
              .equals(b1.getEmergedFromBranchName()));
        });

    final String latestCommonBranchName = firstSelectedBranchPoints.getFirst()
        .getEmergedFromBranchName();
    String firstSelectedCommitInCommonBranch = firstSelectedBranchPoints.getFirst() // NOPMD
        .getEmergedFromCommitId();
    final List<BranchPoint> temp = secondSelectedBranchPoints.stream()
        .filter(b -> b.getEmergedFromBranchName()
            .equals(latestCommonBranchName
            )
        )
        .toList();

    if (!temp.isEmpty()) {
      final String secondSelectedCommitInCommonBranch = temp.getFirst() // NOPMD
          .getEmergedFromCommitId(); // NOPMD

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
      while ((cr = CommitReport.findByTokenAndApplicationNameAndCommitId(landscapeToken, // NOPMD 
          applicationName, currentCommit)) != null
          &&
          !cr.getCommitId().equals(firstSelectedCommitInCommonBranch)
          &&
          !cr.getCommitId().equals(secondSelectedCommitInCommonBranch)
      ) {
        currentCommit = cr.getParentCommitId();
      }

      if (cr != null && cr.getCommitId().equals(firstSelectedCommitInCommonBranch)) {
        return secondSelectedCommitInCommonBranch;
      }

      if (cr != null && cr.getCommitId().equals(secondSelectedCommitInCommonBranch)) {
        return firstSelectedCommitInCommonBranch;
      }

      return "";
    }
    return "";
  }

  /**
   * ... * @param firstSelectedCommitId the first selected commit's id. * @param
   * secondSelectedCommitId the second selected commit's id. * @param landscapeToken the landscape
   * token * @return the list of file names that have been added in the second selected commit *
   * (i.e. that exist in the second selected commit but not in the first selected commit).
   */
  public List<String> getComparisonAddedFiles(final String firstSelectedCommitId, // NOPMD
      final String secondSelectedCommitId, final String landscapeToken,
      final String applicationName) {

    final List<String> addedFiles = new ArrayList<>();

    final CommitReport commitReportFirstSelectedCommit = CommitReport // NOPMD
        .findByTokenAndApplicationNameAndCommitId(landscapeToken, applicationName,
            firstSelectedCommitId);

    final CommitReport commitReportSecondSelectedCommit = CommitReport // NOPMD
        .findByTokenAndApplicationNameAndCommitId(landscapeToken, applicationName,
            secondSelectedCommitId);

    if (commitReportFirstSelectedCommit != null
        &&
        commitReportSecondSelectedCommit != null) {

      final List<String> filesInFirstSelectedCommit = commitReportFirstSelectedCommit.getFiles();
      final List<String> filesInSecondSelectedCommit = commitReportSecondSelectedCommit.getFiles();
      filesInSecondSelectedCommit.forEach(file -> {

        if (!filesInFirstSelectedCommit.contains(file)) {
          addedFiles.add(file);
        }
      });
    }

    return addedFiles;
  }

  /**
   * ... * @param firstSelectedCommitId the first selected commit's id. * @param
   * secondSelectedCommitId the second selected commit's id. * @param landscapeToken the landscape
   * token. * @return the list of file names that have been deleted in the second selected commit *
   * (i.e. that do not exist in the second selected commit but in the first selected commit).
   */
  public List<String> getComparisonDeletedFiles(final String firstSelectedCommitId, // NOPMD
      final String secondSelectedCommitId, final String landscapeToken,
      final String applicationName) {

    final List<String> deletedFiles = new ArrayList<>();
    final CommitReport commitReportFirstSelectedCommit = CommitReport // NOPMD
        .findByTokenAndApplicationNameAndCommitId(landscapeToken, applicationName,
            firstSelectedCommitId);

    final CommitReport commitReportSecondSelectedCommit = CommitReport // NOPMD
        .findByTokenAndApplicationNameAndCommitId(landscapeToken, applicationName,
            secondSelectedCommitId);

    if (commitReportFirstSelectedCommit != null
        &&
        commitReportSecondSelectedCommit != null) {
      final List<String> filesInFirstSelectedCommit = commitReportFirstSelectedCommit.getFiles();
      final List<String> filesInSecondSelectedCommit = commitReportSecondSelectedCommit.getFiles();
      filesInFirstSelectedCommit.forEach(file -> {
        if (!filesInSecondSelectedCommit.contains(file)) {
          deletedFiles.add(file);
        }
      });
    }

    return deletedFiles;
  }


  /**
   * ... * @param firstSelectedCommitId the first selected commit's id. * @param
   * secondSelectedCommitId the second selected commit's id. * @param landscapeToken the landscape
   * token. * @return the list of file names that have been modified in the second selected commit *
   * (i.e. files that exist in both commits but have a different hash value).
   */
  public List<String> getComparisonModifiedFiles(final String firstSelectedCommitId, // NOPMD
      final String secondSelectedCommitId, final String landscapeToken,
      final String applicationName) {

    final List<String> modifiedFiles = new ArrayList<>();
    final CommitReport commitReportFirstSelectedCommit = CommitReport // NOPMD
        .findByTokenAndApplicationNameAndCommitId(landscapeToken, applicationName,
            firstSelectedCommitId);

    final CommitReport commitReportSecondSelectedCommit = CommitReport // NOPMD
        .findByTokenAndApplicationNameAndCommitId(landscapeToken, applicationName,
            secondSelectedCommitId);

    if (commitReportFirstSelectedCommit != null
        &&
        commitReportSecondSelectedCommit != null) {
      final List<String> filesInFirstSelectedCommit = commitReportFirstSelectedCommit.getFiles();
      final List<String> filesInSecondSelectedCommit = commitReportSecondSelectedCommit.getFiles();

      filesInFirstSelectedCommit.forEach(file -> {
        if (filesInSecondSelectedCommit.contains(file)) {
          final int indexFirstSelected = filesInFirstSelectedCommit.indexOf(file);
          final int indexSecondSelected = filesInSecondSelectedCommit.indexOf(file);
          final String fileHashFirstSelected = commitReportFirstSelectedCommit.getFileHash()
              .get(indexFirstSelected);
          final String fileHashSecondSelected = commitReportSecondSelectedCommit.getFileHash()
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
