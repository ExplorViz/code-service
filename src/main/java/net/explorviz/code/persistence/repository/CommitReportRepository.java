package net.explorviz.code.persistence.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import net.explorviz.code.persistence.entity.CommitReport;

@ApplicationScoped
public class CommitReportRepository implements PanacheMongoRepository<CommitReport> {

  public CommitReport findByTokenAndApplicationNameAndCommitId(
      final String landscapeToken, final String applicationName, final String commitId) {
    return find("landscapeToken = ?1 and applicationName = ?2 and commitId = ?3",
        landscapeToken, applicationName, commitId).firstResult();
  }

  public List<CommitReport> findByTokenAndApplicationNameAndCommitIds(
      String landscapeToken, String applicationName, List<String> commitIds) {
    return list("landscapeToken = ?1 and applicationName = ?2 and commitId in ?3",
        landscapeToken, applicationName, commitIds);
  }

  public List<CommitReport> findByTokenAndApplicationNameAndBranchName(
      String landscapeToken, String applicationName, String branchName) {
    return list("landscapeToken = ?1 and applicationName = ?2 and branchName in ?3",
        landscapeToken, applicationName, branchName);
  }
}
