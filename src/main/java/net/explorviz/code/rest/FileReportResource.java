package net.explorviz.code.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import net.explorviz.code.helper.LandscapeStructureHelper;
import net.explorviz.code.mongo.FileReport;

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
