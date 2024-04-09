package net.explorviz.code.rest;


import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import net.explorviz.code.mongo.CommitReport;
import org.jboss.resteasy.reactive.RestPath;


/**
 * ...
 */
@Path("/commit-report/{landscapeToken}/{applicationName}/{commit}")
public class CommitReportResource {

  /**
   * ...
   ** @param landscapeToken the landsacpe token.
   ** @param applicationName the application name.
   ** @param commit the commit id.
   ** @return the commit report matching the params.
   */
  @GET
  public CommitReport list(@RestPath final String landscapeToken, 
      @RestPath final String applicationName, @RestPath final String commit) {
    final CommitReport cr = CommitReport.findByTokenAndApplicationNameAndCommitId(landscapeToken, 
        applicationName, commit);
    if (cr != null) {
      return cr;
    } 
    return new CommitReport(); // we could enhance the file metric with the help of the 
    // FileReport before we return
  }
    
}
