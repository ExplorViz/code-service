package net.explorviz.code.kafka;

import javax.enterprise.context.ApplicationScoped;
import net.explorviz.code.grpc.FileDataServiceImpl;
import net.explorviz.code.proto.CommitReportData;
import net.explorviz.code.proto.FileData;
import net.explorviz.code.proto.StateDataRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class KafkaGateway {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileDataServiceImpl.class);

  public void processCommitReport(CommitReportData commitReportData) {
    LOGGER.trace("Received Commit report: {}", commitReportData);
  }


  public void processFileData(FileData fileData) {
    LOGGER.trace("Received file data: {}", fileData);
  }

  public String processStateData(StateDataRequest stateDataRequest) {
    LOGGER.trace("Received request to send state of branch: {}", stateDataRequest.getBranchName());
    // Send the empty string if the state of the branch is unknown, otherwise the SHA1 of the branch
    return "";
  }
}
