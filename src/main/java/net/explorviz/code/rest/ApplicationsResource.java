package net.explorviz.code.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.jboss.resteasy.reactive.RestPath;

import net.explorviz.code.helper.LandscapeStructureHelper;
import net.explorviz.code.mongo.Application;
import net.explorviz.code.mongo.FileReport;

/**
 * ...
 */
@Path("/v2/applications/{token}")
public class ApplicationsResource {

  /**
   * ...
   ** @param token ...
   ** @param appName ...
   ** @param fqFileName ...
   ** @param commit ...
   ** @return ...
   */
  @GET
  public List<String> list(final @RestPath String token) {

    final List<Application> applications = Application.findByLandscapeToken(token);
    final List<String> applicationNames = new ArrayList<String>();
    for (final Application application : applications) {
      applicationNames.add(application.getApplicationName());
    }
    return applicationNames;
  }
}
