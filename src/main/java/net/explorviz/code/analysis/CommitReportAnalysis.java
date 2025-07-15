package net.explorviz.code.analysis;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.explorviz.code.persistence.entity.Application;
import net.explorviz.code.persistence.entity.BranchPoint;
import net.explorviz.code.persistence.entity.CommitReport;
import net.explorviz.code.persistence.entity.CommitReport.FileMetric;
import net.explorviz.code.persistence.entity.FileReportTable;
import net.explorviz.code.persistence.entity.LatestCommit;
import net.explorviz.code.persistence.repository.ApplicationRepository;
import net.explorviz.code.persistence.repository.BranchPointRepository;
import net.explorviz.code.persistence.repository.CommitReportRepository;
import net.explorviz.code.persistence.repository.FileReportTableRepository;
import net.explorviz.code.persistence.repository.LatestCommitRepository;
import net.explorviz.code.proto.CommitReportData;
import net.explorviz.code.proto.FileMetricData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Analysis class for every incoming CommitReport record.
 */
@ApplicationScoped
public class CommitReportAnalysis {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommitReportAnalysis.class);

  private static final String NO_ANCESTOR = "NONE";

  private final ApplicationRepository appRepo;
  private final LatestCommitRepository latestCommitRepository;
  private final BranchPointRepository branchPointRepo;
  private final CommitReportRepository commitReportRepo;
  private final FileReportTableRepository fileReportTableRepository;

  @Inject
  public CommitReportAnalysis(final ApplicationRepository appRepo,
      final LatestCommitRepository latestCommitRepository,
      final BranchPointRepository branchPointRepo,
      final CommitReportRepository commitReportRepository,
      final FileReportTableRepository fileReportTableRepository) {
    this.appRepo = appRepo;
    this.latestCommitRepository = latestCommitRepository;
    this.branchPointRepo = branchPointRepo;
    this.commitReportRepo = commitReportRepository;
    this.fileReportTableRepository = fileReportTableRepository;
  }

  /**
   * Processes a CommitReportData package. Stores the data into the local storage.
   *
   * @param commitReportData the CommitReportData to handle
   */
  public void processCommitReport(final CommitReportData commitReportData) { // NOPMD

    LOGGER.atTrace().addArgument(commitReportData).log("Received Commit report: {}");

    final String receivedCommitReportCommitId = commitReportData.getCommitID();
    final String receivedCommitReportLandscapeToken = commitReportData.getLandscapeToken(); // NOPMD
    final String receivedCommitReportApplicationName = commitReportData // NOPMD
        .getApplicationName();

    final CommitReport oldReport = this.commitReportRepo.findByTokenAndApplicationNameAndCommitId(
        receivedCommitReportLandscapeToken, receivedCommitReportApplicationName,
        receivedCommitReportCommitId);

    if (oldReport != null) {
      return;
    }

    final String receivedCommitReportAncestorId = commitReportData.getParentCommitID();
    // Add entry for FileReportTable. We need to initiate it here because there can
    // be some
    // commits where the Code-Agent won't send File Reports at all

    final FileReportTable fileReportTable = this.fileReportTableRepository
        .findByTokenAndAppName(receivedCommitReportLandscapeToken,
            receivedCommitReportApplicationName);

    if (fileReportTable != null) {
      final Map<String, Map<String, String>> table = fileReportTable
          .getCommitIdTofqnFileNameToCommitIdMap();

      if (!NO_ANCESTOR.equals(receivedCommitReportAncestorId)) {

        final Map<String, String> parentEntries = table.get(receivedCommitReportAncestorId);

        if (parentEntries != null) {
          final boolean keyExists = table.containsKey(receivedCommitReportCommitId);

          Map<String, String> fqFileNameToCommitId = new HashMap<>();
          if (keyExists) {
            LOGGER.warn(
                "Commit Report normally should be sent and received before its File Reports");
            fqFileNameToCommitId = table.get(receivedCommitReportCommitId);
          }

          for (final Map.Entry<String, String> entry : parentEntries.entrySet()) {
            if (keyExists) {
              if (!fqFileNameToCommitId.containsKey(entry.getKey())) { // don't overwrite new data
                fqFileNameToCommitId.put(entry.getKey(), entry.getValue());
              }
            } else {
              fqFileNameToCommitId.put(entry.getKey(), entry.getValue());
            }
          }

          table.put(receivedCommitReportCommitId, fqFileNameToCommitId);
          fileReportTable.setCommitIdTofqnFileNameToCommitIdMap(table);
          fileReportTable.update();
        }
      }
    }

    final String receivedCommitReportBranchName = commitReportData.getBranchName();
    final List<String> receivedCommitReportFiles = commitReportData.getFilesList();
    final List<String> receivedCommitReportModified = commitReportData.getModifiedList();
    final List<String> receivedCommitReportDeleted = commitReportData.getDeletedList();
    final List<String> receivedCommitReportAdded = commitReportData.getAddedList();
    final List<FileMetricData> receivedCommitReportFileMetricData = commitReportData // NOPMD
        .getFileMetricList();
    final List<FileMetric> receivedCommitReportFileMetric = new ArrayList<>();
    final List<String> receivedCommitReportFileHash = commitReportData.getFileHashList();

    for (final FileMetricData fileMetricData : receivedCommitReportFileMetricData) {
      final CommitReport.FileMetric fileMetric = new CommitReport.FileMetric(); // NOPMD
      fileMetric.setFileName(fileMetricData.getFileName());
      fileMetric.setLoc(fileMetricData.getLoc());
      fileMetric.setCloc(fileMetricData.getCloc());
      fileMetric.setSize(fileMetricData.getFileSize());
      fileMetric.setCyclomaticComplexity(fileMetricData.getCyclomaticComplexity());
      fileMetric.setNumberOfMethods(fileMetricData.getNumberOfMethods());
      receivedCommitReportFileMetric.add(fileMetric);
    }

    final CommitReport commitReport = new CommitReport(receivedCommitReportCommitId, receivedCommitReportAncestorId,
        receivedCommitReportBranchName, receivedCommitReportFiles, receivedCommitReportModified,
        receivedCommitReportDeleted, receivedCommitReportAdded,
        receivedCommitReportFileMetric, receivedCommitReportLandscapeToken,
        receivedCommitReportFileHash, receivedCommitReportApplicationName);

    if (!NO_ANCESTOR.equals(receivedCommitReportAncestorId)) { // NOPMD
      if (this.commitReportRepo.findByTokenAndApplicationNameAndCommitId(// NOPMD
          receivedCommitReportLandscapeToken,
          receivedCommitReportApplicationName, receivedCommitReportAncestorId) != null) {
        // no missing reports
        this.commitReportRepo.persist(commitReport);
        LatestCommit latestCommit = this.latestCommitRepository
            .findByLandscapeTokenAndApplicationNameAndBranchName(
                receivedCommitReportLandscapeToken, receivedCommitReportApplicationName,
                receivedCommitReportBranchName);
        if (latestCommit == null) {
          // commit of a new branch
          latestCommit = new LatestCommit(receivedCommitReportCommitId, receivedCommitReportBranchName,
              receivedCommitReportLandscapeToken, receivedCommitReportApplicationName);
          this.latestCommitRepository.persist(latestCommit);

          final CommitReport ancestorCommitReport = this.commitReportRepo
              .findByTokenAndApplicationNameAndCommitId(receivedCommitReportLandscapeToken,
                  receivedCommitReportApplicationName, receivedCommitReportAncestorId);

          final BranchPoint branchPoint = new BranchPoint(receivedCommitReportCommitId, receivedCommitReportBranchName,
              ancestorCommitReport.branchName(), ancestorCommitReport.commitId(),
              receivedCommitReportLandscapeToken, receivedCommitReportApplicationName);

          this.branchPointRepo.persist(branchPoint);
        } else {
          this.latestCommitRepository.updateCommitIdOfLatestCommit(
              receivedCommitReportCommitId, latestCommit);
        }
      } else { // NOPMD
        this.commitReportRepo.persist(commitReport);
        LatestCommit latestCommit = this.latestCommitRepository
            .findByLandscapeTokenAndApplicationNameAndBranchName(
                receivedCommitReportLandscapeToken, receivedCommitReportApplicationName,
                receivedCommitReportBranchName);
        if (latestCommit == null) {
          // commit of a new branch
          latestCommit = new LatestCommit(receivedCommitReportCommitId, receivedCommitReportBranchName,
              receivedCommitReportLandscapeToken, receivedCommitReportApplicationName);

          this.latestCommitRepository.persist(latestCommit);

          final BranchPoint branchPoint = new BranchPoint(receivedCommitReportCommitId, receivedCommitReportBranchName,
              "UNKNOWN-EMERGED-BRANCH", "UNKNOWN-EMERGED-COMMIT",
              receivedCommitReportLandscapeToken, receivedCommitReportApplicationName);
          this.branchPointRepo.persist(branchPoint);
        } else {
          this.latestCommitRepository.updateCommitIdOfLatestCommit(
              receivedCommitReportCommitId, latestCommit);
        }

        Application application = this.appRepo.findByLandscapeTokenAndApplicationName(
            receivedCommitReportLandscapeToken,
            receivedCommitReportApplicationName);

        if (application == null) {
          application = new Application(receivedCommitReportApplicationName,
              receivedCommitReportLandscapeToken);
          this.appRepo.persist(application);
        }
      }
    } else {
      // first commit ever
      this.commitReportRepo.persist(commitReport);

      final LatestCommit latestCommit = new LatestCommit(receivedCommitReportCommitId, receivedCommitReportBranchName,
          receivedCommitReportLandscapeToken, receivedCommitReportApplicationName);
      this.latestCommitRepository.persist(latestCommit);

      final BranchPoint branchPoint = new BranchPoint(receivedCommitReportCommitId, receivedCommitReportBranchName,
          NO_ANCESTOR,
          "", receivedCommitReportLandscapeToken, receivedCommitReportApplicationName);

      this.branchPointRepo.persist(branchPoint);

      Application application = this.appRepo.findByLandscapeTokenAndApplicationName(receivedCommitReportLandscapeToken,
          receivedCommitReportApplicationName);

      if (application == null) {
        application = new Application(receivedCommitReportApplicationName,
            receivedCommitReportLandscapeToken);

        this.appRepo.persist(application);
      }
    }
  }

}
