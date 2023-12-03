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

    List<CommitReport> commitReports = CommitReport.listAll();
    for (final CommitReport report : commitReports) {
      System.out.println("CREATED COMMIT REPORT ID: " + report.commitId);
      report.fileMetric.forEach((f -> System.out.println(f.fileName)));
    }
    return CommitTreeHelper.createCommitTree("testapp");
  }
}
