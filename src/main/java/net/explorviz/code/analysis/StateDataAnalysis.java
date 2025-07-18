package net.explorviz.code.analysis;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.explorviz.code.persistence.entity.LatestCommit;
import net.explorviz.code.persistence.repository.LatestCommitRepository;
import net.explorviz.code.proto.StateDataRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Analysis class for every incoming StateData record.
 */
@ApplicationScoped
public class StateDataAnalysis {

  private static final Logger LOGGER = LoggerFactory.getLogger(StateDataAnalysis.class);

  private final LatestCommitRepository latestCommitRepository;

  @Inject
  public StateDataAnalysis(final LatestCommitRepository latestCommitRepository) {
    this.latestCommitRepository = latestCommitRepository;
  }


  /**
   * Processes a stateDataRequest. Looks into the used storage and returns the branch's last
   * commit.
   *
   * @param stateDataRequest the StateDataRequest to handle
   * @return The current commit's sha1
   */
  public String processStateData(final StateDataRequest stateDataRequest) {

    LOGGER.atTrace().addArgument(stateDataRequest.getUpstreamName())
        .addArgument(stateDataRequest.getBranchName())
        .addArgument(stateDataRequest.getLandscapeToken())
        .addArgument(stateDataRequest.getLandscapeSecret())
        .addArgument(stateDataRequest.getApplicationName())
        .log("Request for state - upstream: {}, branch: {}, token: {}, secret: {},"
            + " application name: {}");

    final String branchName = stateDataRequest.getBranchName();
    final String landscapeToken = stateDataRequest.getLandscapeToken();
    final String applicationName = stateDataRequest.getApplicationName();
    final LatestCommit latestCommit = this.latestCommitRepository
        .findByLandscapeTokenAndApplicationNameAndBranchName(landscapeToken, applicationName,
            branchName);

    // Send the empty string if the state of the branch is unknown, otherwise the SHA1 of
    // the branch's last commit
    if (latestCommit != null) {
      return latestCommit.commitId();
    }
    return "";
  }

}
