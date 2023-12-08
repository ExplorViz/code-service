package net.explorviz.code.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import net.explorviz.code.beans.LandscapeStructure;
import net.explorviz.code.beans.LandscapeStructure.Node;
import net.explorviz.code.beans.LandscapeStructure.Node.Application;
import net.explorviz.code.beans.LandscapeStructure.Node.Application.Package;
import net.explorviz.code.helper.CommitComparisonHelper;
import net.explorviz.code.helper.LandscapeStructureHelper;
import net.explorviz.code.mongo.FileReport;
import net.explorviz.code.mongo.FileReport.ClassData2;

/**
 * ...
 */
@Path("/file-report")
public class FileReportResource {

  /**
   * ...
   ** @param token ...
   ** @param appName ...
   ** @param fqFileName ...
   ** @param commit ...
   ** @return ...
   */
  @Path("{token}/{appName}/{fqFileName}/{commit}")
  @GET
  public FileReport list(String token, 
      String appName, String fqFileName, String commit) {

    FileReport fileReport = LandscapeStructureHelper.getFileReport(token, appName, 
        fqFileName, commit);

    if (fileReport != null) {
      return fileReport;
    } else {
      return new FileReport();
    }
  }
}
