package net.explorviz.code.analysis;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.explorviz.code.mongo.Application;
import net.explorviz.code.mongo.BranchPoint;
import net.explorviz.code.mongo.CommitReport;
import net.explorviz.code.mongo.CommitReport.FileMetric;
import net.explorviz.code.mongo.FileReportTable;
import net.explorviz.code.mongo.LatestCommit;
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

    final CommitReport oldReport = CommitReport.findByTokenAndApplicationNameAndCommitId(
        receivedCommitReportLandscapeToken, receivedCommitReportApplicationName,
        receivedCommitReportCommitId);

    if (oldReport != null) {
      return;
    }

    final String receivedCommitReportAncestorId = commitReportData.getParentCommitID();
    // Add entry for FileReportTable. We need to initiate it here because there can be some
    // commits where the Code-Agent won't send File Reports at all

    final FileReportTable fileReportTable = FileReportTable
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
      fileMetric.setCyclomaticComplexity(fileMetricData.getCyclomaticComplexity());
      fileMetric.setNumberOfMethods(fileMetricData.getNumberOfMethods());
      receivedCommitReportFileMetric.add(fileMetric);
    }

    final CommitReport commitReport = new CommitReport();
    commitReport.setCommitId(receivedCommitReportCommitId);
    commitReport.setParentCommitId(receivedCommitReportAncestorId);
    commitReport.setBranchName(receivedCommitReportBranchName);
    commitReport.setFiles(receivedCommitReportFiles);
    commitReport.setModified(receivedCommitReportModified);
    commitReport.setDeleted(receivedCommitReportDeleted);
    commitReport.setAdded(receivedCommitReportAdded);
    commitReport.setFileMetric(receivedCommitReportFileMetric);
    commitReport.setLandscapeToken(receivedCommitReportLandscapeToken);
    commitReport.setFileHash(receivedCommitReportFileHash);
    commitReport.setApplicationName(receivedCommitReportApplicationName);

    if (!NO_ANCESTOR.equals(receivedCommitReportAncestorId)) { // NOPMD
      if (CommitReport.findByTokenAndApplicationNameAndCommitId(// NOPMD
          receivedCommitReportLandscapeToken,
          receivedCommitReportApplicationName, receivedCommitReportAncestorId) != null) {
        // no missing reports
        commitReport.persist();
        LatestCommit latestCommit = LatestCommit
            .findByLandscapeTokenAndApplicationNameAndBranchName(
                receivedCommitReportLandscapeToken, receivedCommitReportApplicationName,
                receivedCommitReportBranchName);
        if (latestCommit == null) {
          // commit of a new branch
          latestCommit = new LatestCommit();
          latestCommit.setBranchName(receivedCommitReportBranchName);
          latestCommit.setCommitId(receivedCommitReportCommitId);
          latestCommit.setLandscapeToken(receivedCommitReportLandscapeToken);
          latestCommit.setApplicationName(receivedCommitReportApplicationName);
          latestCommit.persist();

          final BranchPoint branchPoint = new BranchPoint();
          branchPoint.setBranchName(receivedCommitReportBranchName);
          branchPoint.setCommitId(receivedCommitReportCommitId);
          branchPoint.setLandscapeToken(receivedCommitReportLandscapeToken);
          branchPoint.setApplicationName(receivedCommitReportApplicationName);
          final CommitReport ancestorCommitReport = CommitReport
              .findByTokenAndApplicationNameAndCommitId(receivedCommitReportLandscapeToken,
                  receivedCommitReportApplicationName, receivedCommitReportAncestorId);
          branchPoint.setEmergedFromCommitId(ancestorCommitReport.getCommitId());
          branchPoint.setEmergedFromBranchName(ancestorCommitReport.getBranchName());
          branchPoint.persist();
        } else {
          latestCommit.setCommitId(receivedCommitReportCommitId);
          latestCommit.update();
        }
      } else { // NOPMD
        commitReport.persist();
        LatestCommit latestCommit = LatestCommit
            .findByLandscapeTokenAndApplicationNameAndBranchName(
                receivedCommitReportLandscapeToken, receivedCommitReportApplicationName,
                receivedCommitReportBranchName);
        if (latestCommit == null) {
          // commit of a new branch
          latestCommit = new LatestCommit();
          latestCommit.setBranchName(receivedCommitReportBranchName);
          latestCommit.setCommitId(receivedCommitReportCommitId);
          latestCommit.setLandscapeToken(receivedCommitReportLandscapeToken);
          latestCommit.setApplicationName(receivedCommitReportApplicationName);
          latestCommit.persist();

          final BranchPoint branchPoint = new BranchPoint();
          branchPoint.setBranchName(receivedCommitReportBranchName);
          branchPoint.setCommitId(receivedCommitReportCommitId);
          branchPoint.setLandscapeToken(receivedCommitReportLandscapeToken);
          branchPoint.setApplicationName(receivedCommitReportApplicationName);
          branchPoint.setEmergedFromCommitId("UNKNOWN-EMERGED-COMMIT");
          branchPoint.setEmergedFromBranchName("UNKNOWN-EMERGED-BRANCH");
          branchPoint.persist();
        } else {
          latestCommit.setCommitId(receivedCommitReportCommitId);
          latestCommit.update();
        }
        Application application = Application.findByLandscapeTokenAndApplicationName(
            receivedCommitReportLandscapeToken, receivedCommitReportApplicationName);
        if (application == null) {
          application = new Application();
          application.setApplicationName(receivedCommitReportApplicationName);
          application.setLandscapeToken(receivedCommitReportLandscapeToken);
          application.persist();
        }
      }
    } else {
      // first commit ever
      commitReport.persist();
      final LatestCommit latestCommit = new LatestCommit();
      latestCommit.setBranchName(receivedCommitReportBranchName);
      latestCommit.setCommitId(receivedCommitReportCommitId);
      latestCommit.setLandscapeToken(receivedCommitReportLandscapeToken);
      latestCommit.setApplicationName(receivedCommitReportApplicationName);
      latestCommit.persist();
      final BranchPoint branchPoint = new BranchPoint();
      branchPoint.setBranchName(receivedCommitReportBranchName);
      branchPoint.setCommitId(receivedCommitReportCommitId);
      branchPoint.setLandscapeToken(receivedCommitReportLandscapeToken);
      branchPoint.setApplicationName(receivedCommitReportApplicationName);
      branchPoint.setEmergedFromBranchName(NO_ANCESTOR);
      branchPoint.setEmergedFromCommitId("");
      branchPoint.persist();

      Application application = Application.findByLandscapeTokenAndApplicationName(
          receivedCommitReportLandscapeToken, receivedCommitReportApplicationName);
      if (application == null) {
        application = new Application();
        application.setApplicationName(receivedCommitReportApplicationName);
        application.setLandscapeToken(receivedCommitReportLandscapeToken);
        application.persist();
      }
    }
  }

}
