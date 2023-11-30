package net.explorviz.code.kafka;


import java.util.ArrayList;
import java.util.List;
import net.explorviz.code.proto.FileMetricData;
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


    //LatestCommit.deleteAll();
    //CommitReport.deleteAll();
    
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

    //commitReport.persist();

    //CommitReport.deleteAll();
    





      // PROBLEM: https://stackoverflow.com/questions/58416014/how-can-i-find-the-first-commit-of-a-branch-with-jgit
      // HOW TO SOLVE IT? MAYBE SET A TAG FOR EACH COMMIT IN CODE-AGENT WHICH MARKS THE CORRESPONDING BRANCH
      // TODO: Try to find the latest ancestor commit in our database. If it is the direct ancestor
      // of the received commit (if existent, otherwise first commit ever), we set the received 
      // commit as new latest received commit for the corresponding 
      // branch (also for the first commit ever). Otherwise we are missing CommitReports.
      // We fetch and persist all the missing CommitReports before we finally store the 
      // received CommitReport. Then we set the received commit as new latest received commit.
      // If one of the missing commit reports can't be fetched we abort and nothing will be stored.

      // if (receivedCommitReportAncestorId.equals("NONE")) {
      //   // first commit 
      //   System.out.println("First commit");
      // } else {
      //   List<CommitReport> commitReportList = new ArrayList<>();
      //   final CommitReport newCommitReport = new CommitReport();
      //   // build it from received data
      //   newCommitReport.setCommitId(receivedCommitReportCommitId);
      //   commitReportList.add(newCommitReport);
      //   CommitReport commitReport = CommitReport.findByCommitId(receivedCommitReportAncestorId);
      //   while (commitReport == null){
      //     // request commitreport with commitid receivedCommitReportAncestorId from code-agent
      //     // if(!fetchedCommitReportAncestorId.equals("NONE")) 
      //     //  commitReport = CommitReport.findByCommitId(fetchedCommitReportAncestorId);
      //     // else
      //     //    commitReport = fetchedCommitReport // add this one only if not already stored
      //   }
      // }
      
      //final CommitReport commitReport = new CommitReport();
      
      //commitReport.persist();
      //List<CommitReport> allReports = CommitReport.listAll();
      //for (final CommitReport report : allReports){
      // System.out.println("commitreport id:" + report.commitId);
      //}
  }

  /**
   * Processes a FileData package. Stores the data into the local storage.
   *
   * @param fileData the FileData to handle
   */
  public void processFileData(final FileData fileData) {
    if (LOGGER.isTraceEnabled()) {
      // LOGGER.trace("Received file data: {}", fileData);
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
