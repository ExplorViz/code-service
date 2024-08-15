package net.explorviz.code.persistence.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import net.explorviz.code.persistence.entity.Application;

@ApplicationScoped
public class ApplicationRepository implements PanacheMongoRepository<Application> {

  public List<Application> findByLandscapeToken(final String landscapeToken) {
    return list("landscapeToken", landscapeToken);
  }

  public Application findByLandscapeTokenAndApplicationName(final String landscapeToken,
      final String applicationName) {
    return find("landscapeToken = ?1 and applicationName = ?2", landscapeToken,
        applicationName).firstResult();
  }

}
