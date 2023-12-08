package net.explorviz.code.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.jboss.resteasy.reactive.RestPath;

import net.explorviz.code.beans.CommitComparison;
import net.explorviz.code.helper.CommitComparisonHelper;

/**
 * ...
 */

@Path("/commit-comparison/{token}/{appName}")
public class CommitComparisonResource {

  /**
   * ...
   ** @return ...
   */
  @Path("{firstCommit}-{secondCommit}")
  @GET
  public  CommitComparison list(@RestPath String token, @RestPath String appName, 
      String firstCommit, String secondCommit) {
    final String firstSelectedCommitId = firstCommit;
    final String secondSelectedCommitId = secondCommit;
    final String landscapeToken = token;

    List<String> added = CommitComparisonHelper.getComparisonAddedFiles(
        firstSelectedCommitId, 
        secondSelectedCommitId, landscapeToken, appName);

    List<String> modified = CommitComparisonHelper.getComparisonModifiedFiles(
        firstSelectedCommitId, 
        secondSelectedCommitId, landscapeToken, appName);

    List<String> deleted = CommitComparisonHelper.getComparisonDeletedFiles(
        firstSelectedCommitId, 
        secondSelectedCommitId, landscapeToken, appName);

    return new CommitComparison(added, modified, deleted);
  }
    
}
