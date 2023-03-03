package net.explorviz.code.kafka;

import javax.enterprise.context.ApplicationScoped;
import net.explorviz.code.grpc.FileDataServiceImpl;
import net.explorviz.code.proto.CommitReportData;
import net.explorviz.code.proto.FileData;
import net.explorviz.code.proto.StateDataRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class functions as a gateway for the analysis data into kafka or another storage. It gets
 * called by the respective GRPC endpoints.
 */
@ApplicationScoped
public class KafkaGateway {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileDataServiceImpl.class);

  /**
   * Processes a CommitReportData package. Stores the data into the local storage.
   *
   * @param commitReportData the CommitReportData to handle
   */
  public void processCommitReport(final CommitReportData commitReportData) {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Received Commit report: {}", commitReportData);
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
      LOGGER.trace("Received request to send state of branch: {}",
          stateDataRequest.getBranchName());
    }
    // Send the empty string if the state of the branch is unknown, otherwise the SHA1 of
    // the branch's last branch
    return "";
  }
}
