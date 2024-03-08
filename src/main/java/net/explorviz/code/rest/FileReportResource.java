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
   ** @param token the landscape token.
   ** @param appName the application name.
   ** @param fqFileName the full qualified file name.
   ** @param commit the commit id.
   ** @return the file report matching the params above. If the
   ** file with the given file name has not been added or modified
   ** in the given commit, the file report gets returned that matches above params
   ** except the commit id being the latest where the given file name has indeed been
   ** modified or added. 
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
