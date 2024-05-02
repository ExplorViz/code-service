package net.explorviz.code.mongo;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import java.util.List;

/**
 * The class for the most recent commit of a branch w.r.t the application and landscape token.
 */
public class LatestCommit extends PanacheMongoEntity {

  private String commitId;
  private String branchName;
  private String landscapeToken;
  private String applicationName;

  /**
   * ... * @param landscapeToken the landscape token. * @param applicationName the application name.
   * * @param branchName the branch name. * @return the latest commit that matches the params
   * above.
   */
  public static LatestCommit findByLandscapeTokenAndApplicationNameAndBranchName(
      final String landscapeToken, final String applicationName, final String branchName) {
    return find("landscapeToken = ?1 and applicationName = ?2 and branchName = ?3", landscapeToken,
        applicationName, branchName)
        .firstResult();
  }

  public static List<LatestCommit> findAllLatestCommitsByLandscapeTokenAndApplicationName(
      final String landscapeToken, final String applicationName, List<String> branchNames) {
    return find("landscapeToken = ?1 and applicationName = ?2 and branchName in ?3",
        landscapeToken, applicationName, branchNames).list();
  }

  public String getCommitId() {
    return this.commitId;
  }

  public void setCommitId(final String commitId) {
    this.commitId = commitId;
  }

  public String getBranchName() {
    return this.branchName;
  }

  public void setBranchName(final String branchName) {
    this.branchName = branchName;
  }

  public String getLandscapeToken() {
    return this.landscapeToken;
  }

  public void setLandscapeToken(final String landscapeToken) {
    this.landscapeToken = landscapeToken;
  }

  public String getApplicationName() {
    return this.applicationName;
  }

  public void setApplicationName(final String applicationName) {
    this.applicationName = applicationName;
  }
}
