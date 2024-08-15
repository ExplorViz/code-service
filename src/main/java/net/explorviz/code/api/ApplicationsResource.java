package net.explorviz.code.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.ArrayList;
import java.util.List;
import net.explorviz.code.persistence.entity.Application;
import net.explorviz.code.persistence.repository.ApplicationRepository;


/**
 * ...
 */
@Path("/v2/code/")
public class ApplicationsResource {

  private final ApplicationRepository appRepo;

  @Inject
  public ApplicationsResource(final ApplicationRepository appRepo) {
    this.appRepo = appRepo;
  }

  /**
   * ... * @param token the landscape token. * @return A list of application names that have been
   * analyzed and are part of * the landscape with the given token.
   */
  @GET
  @Path("applications/{token}")
  public List<String> list(@PathParam("token") final String token) {

    final List<Application> applications = this.appRepo.findByLandscapeToken(token);

    final List<String> applicationNames = new ArrayList<>();
    for (final Application application : applications) {
      applicationNames.add(application.applicationName());
    }
    return applicationNames;
  }
}
