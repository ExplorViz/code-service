package net.explorviz.code.mongo;

import io.quarkus.mongodb.panache.PanacheMongoEntity;

/**
 * ...
 */
public class LatestCommit extends PanacheMongoEntity {

  private String commitId;
  private String branchName;
  private String landscapeToken;

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

  public static LatestCommit findByBranchNameAndLandscapeToken(final String branchName, 
      final String landscapeToken) {
    return find("branchName = ?1 and landscapeToken = ?2", branchName, landscapeToken)
        .firstResult();
  }
}
