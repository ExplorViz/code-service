package net.explorviz.code.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.jboss.resteasy.reactive.RestPath;

import net.explorviz.code.helper.CommitTreeHelper;
import net.explorviz.code.mongo.CommitReport;

/**
 * ...
 */
@Path("/commit-report/{commit}")
public class CommitReportResource {

  @GET
  public CommitReport list(@RestPath String commit) { //TODO:alsobased on landscapetoken and appname
    return CommitReport.findByCommitId(commit);
  }
    
}
