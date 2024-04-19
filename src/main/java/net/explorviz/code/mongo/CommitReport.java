package net.explorviz.code.mongo;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import java.util.List;


/**
 * A class to store the commit reports that the code-agent sends to us.
 */
public class CommitReport extends PanacheMongoEntity {

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

  public static CommitReport findByTokenAndApplicationNameAndCommitId(
      final String landscapeToken, final String applicationName, final String commitId) {
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

  public String getBranchName() {
    return this.branchName;
  }

  public void setBranchName(final String branchName) {
    this.branchName = branchName;
  }

  public List<String> getFiles() {
    return this.files;
  }

  public void setFiles(final List<String> files) {
    this.files = files;
  }

  public List<String> getModified() {
    return this.modified;
  }

  public void setModified(final List<String> modified) {
    this.modified = modified;
  }

  public List<String> getDeleted() {
    return this.deleted;
  }

  public void setDeleted(final List<String> deleted) {
    this.deleted = deleted;
  }

  public List<String> getAdded() {
    return this.added;
  }

  public void setAdded(final List<String> added) {
    this.added = added;
  }

  public List<FileMetric> getFileMetric() {
    return this.fileMetric;
  }

  public void setFileMetric(final List<FileMetric> fileMetric) {
    this.fileMetric = fileMetric;
  }

  public String getLandscapeToken() {
    return this.landscapeToken;
  }

  public void setLandscapeToken(final String landscapeToken) {
    this.landscapeToken = landscapeToken;
  }

  public List<String> getFileHash() {
    return this.fileHash;
  }

  public void setFileHash(final List<String> fileHash) {
    this.fileHash = fileHash;
  }

  public String getApplicationName() {
    return this.applicationName;
  }

  public void setApplicationName(final String applicationName) {
    this.applicationName = applicationName;
  }

  /**
   * A class for the file metrics of the modified files within a commit report.
   */
  public static class FileMetric {

    private String fileName;
    private int loc;
    private int cyclomaticComplexity;
    private int numberOfMethods;
    // private int numberOfAddedLines;
    // private int numberOfModifiedLines;
    // private int numberOfDeletedLines;

    public String getFileName() {
      return this.fileName;
    }

    public void setFileName(final String fileName) {
      this.fileName = fileName;
    }

    public int getLoc() {
      return this.loc;
    }

    public void setLoc(final int loc) {
      this.loc = loc;
    }

    public int getCyclomaticComplexity() {
      return this.cyclomaticComplexity;
    }

    public void setCyclomaticComplexity(final int cyclomaticComplexity) {
      this.cyclomaticComplexity = cyclomaticComplexity;
    }

    public int getNumberOfMethods() {
      return this.numberOfMethods;
    }

    public void setNumberOfMethods(final int numberOfMethods) {
      this.numberOfMethods = numberOfMethods;
    }

    // public void setNumberOfAddedLines(final int numberOfAddedLines) {
    //   this.numberOfAddedLines = numberOfAddedLines;
    // }

    // public int getNumberOfAddedLines() {
    //   return this.numberOfAddedLines;
    // }

    // public void setNumberOfModifiedLines(final int numberOfModifiedLines) {
    //   this.numberOfModifiedLines = numberOfModifiedLines;
    // }

    // public int getNumberOfDeletedLines() {
    //   return this.numberOfDeletedLines;
    // }

  }


}
