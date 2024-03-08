package net.explorviz.code.rest;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import net.explorviz.code.beans.CommitTree;
import net.explorviz.code.helper.CommitTreeHelper;
import org.jboss.resteasy.reactive.RestPath;


/**
 * ...
 */
@Path("/commit-tree/{token}/{appName}")
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
