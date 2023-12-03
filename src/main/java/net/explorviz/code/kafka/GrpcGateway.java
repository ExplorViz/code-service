package net.explorviz.code.kafka;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.explorviz.code.proto.FileMetricData;
import net.explorviz.code.proto.ClassData;
import javax.enterprise.context.ApplicationScoped;
// import javax.inject.Inject;
import net.explorviz.code.grpc.FileDataServiceImpl;
import net.explorviz.code.mongo.BranchPoint;
import net.explorviz.code.mongo.CommitReport;
import net.explorviz.code.mongo.LatestCommit;
import net.explorviz.code.mongo.CommitReport.FileMetric;
import net.explorviz.code.proto.CommitReportData;
import net.explorviz.code.proto.FileData;
import net.explorviz.code.proto.StateDataRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class functions as a gateway for the analysis data into kafka or another storage. It gets
 * called by the respective GRPC endpoints. The first time analyzation from the code-agent should 
 * always start with the main/master branch
 */
@ApplicationScoped
public class GrpcGateway {

  private static final Logger LOGGER = LoggerFactory.getLogger(GrpcGateway.class);

  /**
   * Processes a CommitReportData package. Stores the data into the local storage.
   *
   * @param commitReportData the CommitReportData to handle
   */
  public void processCommitReport(final CommitReportData commitReportData) {


    // TODO: app name also needs to be received
    
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Received Commit report: {}", commitReportData);
    }

    final String receivedCommitReportCommitId = commitReportData.getCommitID();
    final String receivedCommitReportAncestorId = commitReportData.getParentCommitID();
    final String receivedCommitReportBranchName = commitReportData.getBranchName();
    final List<String> receivedCommitReportFiles = commitReportData.getFilesList();
    final List<String> receivedCommitReportModified = commitReportData.getModifiedList();
    final List<String> receivedCommitReportDeleted = commitReportData.getDeletedList();
    final List<String> receivedCommitReportAdded = commitReportData.getAddedList();
    final List<FileMetricData> receivedCommitReportFileMetricData = commitReportData
        .getFileMetricList();
    final List<FileMetric> receivedCommitReportFileMetric = new ArrayList<>();
    final String receivedCommitReportLandscapeToken = commitReportData.getLandscapeToken();

    for (final FileMetricData fileMetricData : receivedCommitReportFileMetricData) {
      CommitReport.FileMetric fileMetric = new CommitReport.FileMetric(); 
      fileMetric.fileName = fileMetricData.getFileName();
      fileMetric.loc = fileMetricData.getLoc();
      fileMetric.cyclomaticComplexity = fileMetricData.getCyclomaticComplexity();
      receivedCommitReportFileMetric.add(fileMetric);
    }

    final CommitReport oldReport = CommitReport.findByCommitId(receivedCommitReportCommitId);

    if (oldReport != null) {
      return;
    }

    CommitReport commitReport = new CommitReport();
    commitReport.commitId = receivedCommitReportCommitId;
    commitReport.parentCommitId = receivedCommitReportAncestorId;
    commitReport.branchName = receivedCommitReportBranchName;
    commitReport.files = receivedCommitReportFiles;
    commitReport.modified = receivedCommitReportModified;
    commitReport.deleted = receivedCommitReportDeleted;
    commitReport.added = receivedCommitReportAdded;
    commitReport.fileMetric = receivedCommitReportFileMetric;
    commitReport.landscapeToken = receivedCommitReportLandscapeToken;


    if (!receivedCommitReportAncestorId.equals("NONE")) {
      if (CommitReport.findByCommitId(receivedCommitReportAncestorId) != null) {
        // no missing reports
        commitReport.persist();
        LatestCommit latestCommit = LatestCommit
            .findByBranchNameAndLandscapeToken(receivedCommitReportBranchName, 
                                               receivedCommitReportLandscapeToken);
        if (latestCommit == null) {
          // commit of a new branch
          latestCommit = new LatestCommit();
          latestCommit.branchName = receivedCommitReportBranchName;
          latestCommit.commitId = receivedCommitReportCommitId;
          latestCommit.landscapeToken = receivedCommitReportLandscapeToken;
          latestCommit.persist();

          BranchPoint branchPoint = new BranchPoint();
          branchPoint.branchName = receivedCommitReportBranchName;
          branchPoint.commitId = receivedCommitReportCommitId;
          branchPoint.landscapeToken = receivedCommitReportLandscapeToken;
          CommitReport ancestorCommitReport = CommitReport
               .findByCommitId(receivedCommitReportAncestorId);
          branchPoint.emergedFromCommitId = ancestorCommitReport.commitId;
          branchPoint.emergedFromBranchName = ancestorCommitReport.branchName; 
          branchPoint.persist();
        } else {
          latestCommit.commitId = receivedCommitReportCommitId;
          latestCommit.update();
        }
      } else {
        // missing reports. Do nothing until we get the missing reports. Analyzer has to rerun
      }
    } else {
      // first commit ever
      commitReport.persist();
      LatestCommit latestCommit = new LatestCommit();
      latestCommit.branchName = receivedCommitReportBranchName;
      latestCommit.commitId = receivedCommitReportCommitId;
      latestCommit.landscapeToken = receivedCommitReportLandscapeToken;
      latestCommit.persist();
      BranchPoint branchPoint = new BranchPoint();
      branchPoint.branchName = receivedCommitReportBranchName;
      branchPoint.commitId = receivedCommitReportCommitId;
      branchPoint.landscapeToken = receivedCommitReportLandscapeToken;
      branchPoint.emergedFromBranchName = "NONE";
      branchPoint.emergedFromCommitId = "";
      branchPoint.persist();
    }
  }

  /**
   * Processes a FileData package. Stores the data into the local storage.
   *
   * @param fileData the FileData to handle
   */
  public void processFileData(final FileData fileData) {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Received file data: {}", fileData);

      final String receivedFileDataCommitId = fileData.getCommitID();
      final String receivedFileDataFileName = fileData.getFileName();
      final String receivedFileDataPackageName = fileData.getPackageName();
      final List<String> receivedFileDataImportName = fileData.getImportNameList();
      final Map<String, ClassData> receivedFileDataClassData = fileData.getClassDataMap();
      final Map<String, String> receivedFileDataMetric = fileData.getMetricMap();
      final String receivedFileDataAuthor = fileData.getAuthor();
      final int receivedFileDataModifiedLines = fileData.getModifiedLines();
      final int receivedFileDataAddedLines = fileData.getAddedLines();
      final int receivedFileDataDeletedLines = fileData.getDeletedLines();

      return; // NOPMD
    }
  }

  /**
   * Processes a stateDataRequest. Looks into the used storage and returns the branch's last
   * commit.
   *
   * @param stateDataRequest the StateDataRequest to handle
   * @return the current commit's sha1
   */
  public String processStateData(final StateDataRequest stateDataRequest) {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Request for state - upstream: {}, branch: {}, token: {}, secret: {}",
          stateDataRequest.getUpstreamName(),
          stateDataRequest.getBranchName(),
          stateDataRequest.getLandscapeToken(),
          stateDataRequest.getLandscapeSecret());
    }

    final String branchName = stateDataRequest.getBranchName();
    final String landscapeToken = stateDataRequest.getLandscapeToken();
    LatestCommit latestCommit = LatestCommit
        .findByBranchNameAndLandscapeToken(branchName, landscapeToken);

    // Send the empty string if the state of the branch is unknown, otherwise the SHA1 of
    // the branch's last commit
    if (latestCommit != null) {
      return latestCommit.commitId;
    } 
    return "";
  }

}
