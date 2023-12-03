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

@Path("/commit-comparison/{token}")
public class CommitComparisonResource {

  /**
   * ...
   ** @return ...
   */
  @Path("{firstCommit}-{secondCommit}")
  @GET
  public CommitComparison list(@RestPath String token, String firstCommit, String secondCommit) {
    final String firstSelectedCommitId = firstCommit;
    //"874a7d244da169c5effecf3ff918291f99c2c3ae";
    final String secondSelectedCommitId = secondCommit;
    //"ec999dc8f9958d281886c3e0a655d101db211b8e";
    final String landscapeToken = token; //"default-token";

    List<String> added = CommitComparisonHelper.getComparisonAddedFiles(
        firstSelectedCommitId, 
        secondSelectedCommitId, landscapeToken);

    List<String> modified = CommitComparisonHelper.getComparisonModifiedFiles(
        firstSelectedCommitId, 
        secondSelectedCommitId, landscapeToken);

    List<String> deleted = CommitComparisonHelper.getComparisonDeletedFiles(
        firstSelectedCommitId, 
        secondSelectedCommitId, landscapeToken);

    return new CommitComparison(added, modified, deleted, null);
  }
    
}
