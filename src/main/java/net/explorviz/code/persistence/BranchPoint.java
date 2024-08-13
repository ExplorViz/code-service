package net.explorviz.code.persistence;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import java.util.List;


/**
 * A class for the very first commit of a branch.
 */
public class BranchPoint extends PanacheMongoEntity {

  private String commitId;
  private String branchName;
  private String emergedFromBranchName;
  private String emergedFromCommitId;
  private String landscapeToken;
  private String applicationName;

  public static BranchPoint findByTokenAndBranchName(final String landscapeToken,
      final String branchName) {
    return find("landscapeToken = ?1 and branchName = ?2", landscapeToken,
        branchName).firstResult();
  }

  public static List<BranchPoint> findByTokenAndApplicationName(final String landscapeToken,
      final String applicationName) {
    return list("landscapeToken = ?1 and applicationName = ?2", landscapeToken, applicationName);
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

  public String getEmergedFromBranchName() {
    return this.emergedFromBranchName;
  }

  public void setEmergedFromBranchName(final String emergedFromBranchName) {
    this.emergedFromBranchName = emergedFromBranchName;
  }

  public String getEmergedFromCommitId() {
    return this.emergedFromCommitId;
  }

  public void setEmergedFromCommitId(final String emergedFromCommitId) {
    this.emergedFromCommitId = emergedFromCommitId;
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
