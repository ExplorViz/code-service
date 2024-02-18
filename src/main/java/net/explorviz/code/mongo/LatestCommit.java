package net.explorviz.code.mongo;

import io.quarkus.mongodb.panache.PanacheMongoEntity;

/**
 * ...
 */
public class LatestCommit extends PanacheMongoEntity {

  private String commitId;
  private String branchName;
  private String landscapeToken;
  private String applicationName;

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

  /**
   * ...
   ** @param landscapeToken ...
   ** @param applicationName ...
   ** @param branchName ...
   ** @return ...
   */
  public static LatestCommit findByLandscapeTokenAndApplicationNameAndBranchName(
       final String landscapeToken, final String applicationName, final String branchName) {
    return find("landscapeToken = ?1 and applicationName = ?2 and branchName = ?3", landscapeToken, 
        applicationName, branchName)
        .firstResult();
  }
}
