package net.explorviz.code.mongo;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import java.util.List;


/**
 * ...
 * 
 */
public class CommitReport extends PanacheMongoEntity  {
  private String commitId;
  private String parentCommitId;
  private String branchName;
  private List<String> files;
  private List<String> modified;
  private List<String> deleted;
  private List<String> added;
  private List<FileMetric> fileMetric;
  private String landscapeToken;
  private List<String> fileHash;
  private String applicationName;

  public String getCommitId() {
    return this.commitId;
  }

  public void setCommitId(String commitId) {
    this.commitId = commitId;
  }
  
  public String getParentCommitId() {
    return this.parentCommitId;
  }

  public void setParentCommitId(final String parentCommitId) {
    this.parentCommitId = parentCommitId;
  }

  public String getBranchName() {
    return this.branchName;
  }

  public void setBranchName(String branchName) {
    this.branchName = branchName;
  }

  public List<String> getFiles() {
    return this.files;
  }

  public void setFiles(List<String> files) {
    this.files = files;
  }

  public List<String> getModified() {
    return this.modified;
  }

  public void setModified(List<String> modified) {
    this.modified = modified;
  }

  public List<String> getDeleted() {
    return this.deleted;
  }

  public void setDeleted(List<String> deleted) {
    this.deleted = deleted;
  }

  public List<String> getAdded() {
    return this.added;
  }

  public void setAdded(List<String> added) {
    this.added = added;
  }

  public List<FileMetric> getFileMetric() {
    return this.fileMetric;
  }

  public void setFileMetric(List<FileMetric> fileMetric) {
    this.fileMetric = fileMetric;
  }

  public String getLandscapeToken() {
    return this.landscapeToken;
  }

  public void setLandscapeToken(String landscapeToken) {
    this.landscapeToken = landscapeToken;
  }

  public List<String> getFileHash() {
    return this.fileHash;
  }

  public void setFileHash(List<String> fileHash) {
    this.fileHash = fileHash;
  }

  public String getApplicationName() {
    return this.applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  /**
   * ...
   */
  public static class FileMetric {
    private String fileName;
    private int loc;
    private int cyclomaticComplexity;
    private int numberOfMethods;

    public void setFileName(String fileName) {
      this.fileName = fileName;
    }

    public String getFileName() {
      return this.fileName;
    }

    public void setLoc(int loc) {
      this.loc = loc;
    }

    public int getLoc() {
      return this.loc;
    }

    public void setCyclomaticComplexity(int cyclomaticComplexity) {
      this.cyclomaticComplexity = cyclomaticComplexity;
    }

    public int getCyclomaticComplexity() {
      return this.cyclomaticComplexity;
    }

    public void setNumberOfMethods(int numberOfMethods) {
      this.numberOfMethods = numberOfMethods;
    }

    public int getNumberOfMethods() {
      return this.numberOfMethods;
    }

  }


  public static CommitReport findByTokenAndApplicationNameAndCommitId(final String landscapeToken,
      final String applicationName, final String commitId) {
    return find("landscapeToken = ?1 and applicationName = ?2 and commitId = ?3", 
        landscapeToken, applicationName, commitId).firstResult();
  }

  public static CommitReport findByParentCommitId(final String parentCommitId) {
    return find("parentCommitId", parentCommitId).firstResult();
  }




}
