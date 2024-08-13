package net.explorviz.code.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.ArrayList;
import java.util.List;
import net.explorviz.code.persistence.Application;


/**
 * ...
 */
@Path("/v2/code/")
public class ApplicationsResource {

  /**
   * ... * @param token the landscape token. * @return A list of application names that have been
   * analyzed and are part of * the landscape with the given token.
   */
  @GET
  @Path("applications/{token}")
  public List<String> list(@PathParam("token") final String token) {

    final List<Application> applications = Application.findByLandscapeToken(token);
    final List<String> applicationNames = new ArrayList<>();
    for (final Application application : applications) {
      applicationNames.add(application.getApplicationName());
    }
    return applicationNames;
  }
}
