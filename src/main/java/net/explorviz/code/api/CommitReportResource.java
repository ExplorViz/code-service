package net.explorviz.code.api;


import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.explorviz.code.helper.TokenHelper;
import net.explorviz.code.mongo.CommitReport;


/**
 * ...
 */
@Path("/v2/code/commit-report/{token}/{appName}/{commit}")
public class CommitReportResource {

  /**
   * ... * @param token the landsacpe token. * @param appName the application name. * @param commit
   * the commit id. * @return the commit report matching the params.
   */
  @GET
  public CommitReport list(@PathParam("token") final String token,
      @PathParam("appName") final String appName, @PathParam("commit") final String commit) {
    final CommitReport cr = CommitReport.findByTokenAndApplicationNameAndCommitId(
        TokenHelper.handlePotentialDummyToken(token),
        appName, commit);
    if (cr != null) {
      return cr;
    }
    return new CommitReport(); // we could enhance the file metric with the help of the 
    // FileReport before we return
  }

}
