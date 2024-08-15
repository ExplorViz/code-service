package net.explorviz.code.persistence.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import net.explorviz.code.persistence.entity.BranchPoint;

@ApplicationScoped
public class BranchPointRepository implements PanacheMongoRepository<BranchPoint> {

  public BranchPoint findByTokenAndBranchName(final String landscapeToken,
      final String branchName) {
    return find("landscapeToken = ?1 and branchName = ?2", landscapeToken,
        branchName).firstResult();
  }

  public List<BranchPoint> findByTokenAndApplicationName(final String landscapeToken,
      final String applicationName) {
    return list("landscapeToken = ?1 and applicationName = ?2", landscapeToken, applicationName);
  }

}
