package net.explorviz.code.api;


import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.explorviz.code.dto.commit.tree.CommitTree;
import net.explorviz.code.helper.CommitTreeHelper;


/**
 * ...
 */
@Path("/v2/code/commit-tree/{token}/{appName}")
public class CommitTreeResource {

  private final CommitTreeHelper commitTreeHelper;

  @Inject
  public CommitTreeResource(final CommitTreeHelper commitTreeHelper) {
    this.commitTreeHelper = commitTreeHelper;
  }

  /**
   * * @param token the landscape token. * @param appName the application name. * @return the commit
   * tree for the given landscape token and application name.
   */
  @GET
  public CommitTree list(@PathParam("token") final String token,
      @PathParam("appName") final String appName) {
    return this.commitTreeHelper.createCommitTree(appName, token);
  }
}
