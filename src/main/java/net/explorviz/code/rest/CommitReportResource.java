package net.explorviz.code.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.jboss.resteasy.reactive.RestPath;

import net.explorviz.code.helper.CommitTreeHelper;
import net.explorviz.code.mongo.CommitReport;

/**
 * ...
 */
@Path("/commit-report/{landscapeToken}/{applicationName}/{commit}")
public class CommitReportResource {

  @GET
  public CommitReport list(@RestPath String landscapeToken, @RestPath String applicationName,
      @RestPath String commit) {
    CommitReport cr = CommitReport.findByTokenAndApplicationNameAndCommitId(landscapeToken, 
        applicationName, commit);
    if (cr != null) {
      return cr;
    } else {
      return new CommitReport();
    }
  }
    
}
