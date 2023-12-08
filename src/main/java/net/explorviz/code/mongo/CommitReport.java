package net.explorviz.code.mongo;

import java.util.List;

import io.quarkus.mongodb.panache.PanacheMongoEntity;

/**
 * ...
 * 
 */
public class CommitReport extends PanacheMongoEntity  {
  public String commitId;
  public String parentCommitId;
  public String branchName;
  public List<String> files;
  public List<String> modified;
  public List<String> deleted;
  public List<String> added;
  public List<FileMetric> fileMetric;
  public String landscapeToken;
  public List<String> fileHash;
  public String applicationName;

  /**
   * ...
   */
  public static class FileMetric {
    public String fileName;
    public int loc;
    public int cyclomaticComplexity;
    public int numberOfMethods;
  }

  public static CommitReport findByTokenAndApplicationNameAndCommitId(final String landscapeToken,
      final String applicationName, final String commitId) {
    return find("landscapeToken = ?1 and applicationName = ?2 and commitId = ?3", 
        landscapeToken, applicationName, commitId).firstResult();
  }

  public static CommitReport findByParentCommitId(final String parentCommitId) {
    return find("parentCommitId", parentCommitId).firstResult();
  }

  public String getCommitId() {
    return this.commitId;
  }

  public void setCommitId(final String commitId) {
    this.commitId = commitId;
  }

  public String getParentCommitId() {
    return this.parentCommitId;
  }

  public void setParentCommitId(final String parentCommitId) {
    this.parentCommitId = parentCommitId;
  }

  public List<String> getFiles() {
    return this.files;
  }

}
