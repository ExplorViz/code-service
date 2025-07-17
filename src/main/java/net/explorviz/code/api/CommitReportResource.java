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
   * Retrieves the commit report for given commit.
   *
   * @param token The landscape token.
   * @param appName The application name.
   * @param commit The commit ID.
   * @return The commit report for given commit ID.
   */
  @GET
  public CommitReport list(@PathParam("token") final String token,
      @PathParam("appName") final String appName, @PathParam("commit") final String commit) {
    final CommitReport cr =
        this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(token,
            appName, commit);

    return new CommitReport(cr, TokenHelper.handlePotentialDummyToken(cr.landscapeToken()));
  }

}
