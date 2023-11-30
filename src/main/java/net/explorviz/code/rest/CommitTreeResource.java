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
  public /*CommitTree*/ String list() { // TODO: based on landscapetoken and appname

    List<CommitReport> commitReports = CommitReport.listAll();
    for (final CommitReport report : commitReports) {
      System.out.println("CREATED COMMIT REPORT ID: " + report.commitId);
      report.fileMetric.forEach((f -> System.out.println(f.fileName)));
    }
    //return CommitTreeHelper.createCommitTree("testapp");
    return CommitComparisonHelper
      .latestCommonCommitId(
         "d84b11267ec4e4176df4c8eb69ca8da9fe6d22fe",  "6e5228a9fafa3c180d03f6865167e6d805476568646",
         "default-token");
  }
}
