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
   * ..
   * *@return .
   */
  @GET
  public CommitTree list(@RestPath final String token, @RestPath final String appName) { 
    return CommitTreeHelper.createCommitTree(appName, token);
  }
}
