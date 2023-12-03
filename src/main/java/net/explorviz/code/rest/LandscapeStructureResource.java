package net.explorviz.code.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import net.explorviz.code.beans.LandscapeStructure;
import org.jboss.resteasy.reactive.RestPath;

/**
 * ...
 */
@Path("/structure/{token}/{appName}")
public class LandscapeStructureResource {

  @Path("{commit}")
  @GET
  public LandscapeStructure singleStructure(@RestPath String token, 
      @RestPath String appName, String commit) {
    return new LandscapeStructure();
  }

  @Path("{firstCommit}-{secondCommit}")
  @GET
  public LandscapeStructure mixedStructure(@RestPath String token, 
      @RestPath String appName, String firstCommit, String secondCommit) {
    return new LandscapeStructure();
  }
    
}
