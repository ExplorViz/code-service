package net.explorviz.code.rest;


import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import net.explorviz.code.beans.CommitTree;
import net.explorviz.code.helper.CommitComparisonHelper;
import net.explorviz.code.helper.CommitTreeHelper;
import net.explorviz.code.mongo.CommitReport;

/**
 * ...
 */
@Path("/commit-tree")
public class CommitTreeResource {
  /**
   * ..
   * *@return .
   */
  @GET
  public CommitTree list() { // TODO: based on landscapetoken and appname
    return CommitTreeHelper.createCommitTree("testapp"); // TODO: app name
  }
}
