package net.explorviz.code.api;


import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.explorviz.code.helper.TokenHelper;
import net.explorviz.code.persistence.entity.CommitReport;
import net.explorviz.code.persistence.repository.CommitReportRepository;


/**
 * ...
 */
@Path("/v2/code/commit-report/{token}/{appName}/{commit}")
public class CommitReportResource {


  private final CommitReportRepository commitReportRepository;

  @Inject
  public CommitReportResource(final CommitReportRepository commitReportRepository) {
    this.commitReportRepository = commitReportRepository;
  }

  /**
   * ... * @param token the landsacpe token. * @param appName the application name. * @param commit
   * the commit id. * @return the commit report matching the params.
   */
  @GET
  public CommitReport list(@PathParam("token") final String token,
      @PathParam("appName") final String appName, @PathParam("commit") final String commit) {
    final CommitReport cr =
        this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(token,
            appName, commit);

    return new CommitReport(cr, TokenHelper.handlePotentialDummyToken(cr.landscapeToken()));
    // we could enhance the file metric with the help of the
    // FileReport before we return
  }

}
