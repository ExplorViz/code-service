package net.explorviz.code.api;


import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import net.explorviz.code.beans.CommitTree;
import net.explorviz.code.helper.CommitTreeHelper;
import org.jboss.resteasy.reactive.RestPath;


/**
 * ...
 */
@Path("/v2/code/commit-tree/{token}/{appName}")
public class CommitTreeResource {
  /**
   ** @param token the landscape token.
   ** @param appName the application name.
   ** @return the commit tree for the given landscape token and application name.
   */
  @GET
  public CommitTree list(@RestPath final String token, @RestPath final String appName) { 
    return CommitTreeHelper.createCommitTree(appName, token);
  }
}
