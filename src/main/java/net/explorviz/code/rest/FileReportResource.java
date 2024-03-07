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
   ** @return ... important note: it is even possible to request 
    * a file report of a file that exists for a given commit but
    * that has not been modified or added in that commit. We then 
    * return the file report for the newest commit where that file
    * has indeed been modified or added.
   */
  @Path("{token}/{appName}/{fqFileName}/{commit}")
  @GET
  public FileReport list(final String token, // NOPMD
      final String appName, final String fqFileName, final String commit) {

    final FileReport fileReport = LandscapeStructureHelper.getFileReport(token, appName, 
        fqFileName, commit);

    if (fileReport != null) {
      return fileReport;
    } 
    return new FileReport();
  }
}
