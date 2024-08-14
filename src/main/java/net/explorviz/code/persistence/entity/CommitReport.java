package net.explorviz.code.persistence.entity;

import java.util.List;
import java.util.Objects;


/**
 * A class to store the commit reports that the code-agent sends to us.
 */
public record CommitReport(String commitId, String parentCommitId, String branchName,
                           List<String> files, List<String> modified, List<String> deleted,
                           List<String> added,
                           List<FileMetric> fileMetric,
                           String landscapeToken,
                           List<String> fileHash,
                           String applicationName) {

  // Custom constructor that takes an existing CommitReport and a new landscapeToken
  public CommitReport(CommitReport original, String newLandscapeToken) {
    this(
        original.commitId(),
        original.parentCommitId(),
        original.branchName(),
        original.files(),
        original.modified(),
        original.deleted(),
        original.added(),
        original.fileMetric(),
        newLandscapeToken,
        original.fileHash(),
        original.applicationName()
    );
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

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      FileMetric that = (FileMetric) o;
      return loc == that.loc && cyclomaticComplexity == that.cyclomaticComplexity
          && numberOfMethods == that.numberOfMethods && Objects.equals(fileName,
          that.fileName);
    }

    @Override
    public int hashCode() {
      return Objects.hash(fileName, loc, cyclomaticComplexity, numberOfMethods);
    }

  }


}
