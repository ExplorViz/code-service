package net.explorviz.code.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.explorviz.code.helper.LandscapeStructureHelper;
import net.explorviz.code.helper.TokenHelper;
import net.explorviz.code.mongo.FileReport;

/**
 * ...
 */
@Path("/v2/code/file-report")
public class FileReportResource {

  /**
   * ... * @param token the landscape token. * @param appName the application name. * @param
   * fqFileName the full qualified file name. * @param commit the commit id. * @return the file
   * report matching the params above. If the * file with the given file name has not been added or
   * modified * in the given commit, the file report gets returned that matches above params *
   * except the commit id being the latest where the given file name has indeed been * modified or
   * added.
   */
  @Path("{token}/{appName}/{fqFileName}/{commit}")
  @GET
  public FileReport list(@PathParam("token") final String token, // NOPMD
      @PathParam("appName") final String appName, @PathParam("fqFileName") final String fqFileName,
      @PathParam("commit") final String commit) {

    final FileReport fileReport = LandscapeStructureHelper.getFileReport(token, appName,
        fqFileName, commit);

    fileReport.setLandscapeToken(
        TokenHelper.handlePotentialDummyToken(fileReport.getLandscapeToken()));

    if (fileReport != null) {
      return fileReport;
    }
    return new FileReport();
  }
}
