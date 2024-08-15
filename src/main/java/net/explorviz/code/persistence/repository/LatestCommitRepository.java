package net.explorviz.code.persistence.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import net.explorviz.code.persistence.entity.LatestCommit;

@ApplicationScoped
public class LatestCommitRepository implements PanacheMongoRepository<LatestCommit> {

  private static final String WHERE_QUERY_STRING =
      "landscapeToken = ?1 and applicationName = ?2 and branchName = ?3";

  public LatestCommit findByLandscapeTokenAndApplicationNameAndBranchName(
      final String landscapeToken, final String applicationName, final String branchName) {
    return find(WHERE_QUERY_STRING, landscapeToken,
        applicationName, branchName)
        .firstResult();
  }

  public long updateCommitIdOfLatestCommit(
      final String newCommitId,
      final LatestCommit latestCommit) {
    return update("commitId", newCommitId).where(
        WHERE_QUERY_STRING,
        latestCommit.landscapeToken(),
        latestCommit.applicationName(), latestCommit.branchName());
  }

  public List<LatestCommit> findAllLatestCommitsByLandscapeTokenAndApplicationName(
      final String landscapeToken, final String applicationName, List<String> branchNames) {
    return find(WHERE_QUERY_STRING,
        landscapeToken, applicationName, branchNames).list();
  }
}
