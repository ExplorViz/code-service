package net.explorviz.code.persistence;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import java.util.List;

/**
 * The class for every application that the code-agent analyzed.
 */
public class Application extends PanacheMongoEntity {

  private String applicationName;
  private String landscapeToken;

  public static List<Application> findByLandscapeToken(final String landscapeToken) {
    return list("landscapeToken", landscapeToken);
  }

  public static Application findByLandscapeTokenAndApplicationName(final String landscapeToken,
      final String applicationName) {
    return find("landscapeToken = ?1 and applicationName = ?2", landscapeToken,
        applicationName).firstResult();
  }

  public String getApplicationName() {
    return this.applicationName;
  }

  public void setApplicationName(final String applicationName) {
    this.applicationName = applicationName;
  }

  public String getLandscapeToken() {
    return this.landscapeToken;
  }

  public void setLandscapeToken(final String landscapeToken) {
    this.landscapeToken = landscapeToken;
  }
}
