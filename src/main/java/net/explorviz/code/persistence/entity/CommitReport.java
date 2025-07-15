package net.explorviz.code.persistence.entity;

import java.util.List;
import java.util.Objects;

/**
 * A class to store the commit reports that the code-agent sends to us.
 *
 * @param commitId        As name suggest.
 * @param parentCommitId  As name suggest.
 * @param branchName      As name suggest.
 * @param files           List of fq file names.
 * @param modified        List of fq file names that were modified in this
 *                        commit.
 * @param deleted         List of fq file names that were deleted in this
 *                        commit.
 * @param added           List of fq file names that were added in this commit.
 * @param fileMetric      List of file metrics for this commit.
 * @param landscapeToken  The (initially user-given) landscape token.
 * @param fileHash        List of file hashes for this commit.
 * @param applicationName The (initially user-given) app name.
 */
public record CommitReport(String commitId, String parentCommitId, String branchName,
    List<String> files, List<String> modified, List<String> deleted,
    List<String> added,
    List<FileMetric> fileMetric,
    String landscapeToken,
    List<String> fileHash,
    String applicationName) {

  /**
   * Custom constructor that takes an existing CommitReport and a new
   * landscapeToken.
   *
   * @param original          CommitReport whose values (with the exception of the
   *                          token) are used
   * @param newLandscapeToken New Landscape Token
   */
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
        original.applicationName());
  }

  /**
   * A class for the file metrics of the modified files within a commit report.
   */
  public static class FileMetric {

    private String fileName;
    private int loc; // Lines of code incl. whitespace and comments
    private int cloc; // Comment lines of code
    private int size; // Size of the file in bytes
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

    public int getCloc() {
      return this.cloc;
    }

    public void setCloc(final int cloc) {
      this.cloc = cloc;
    }

    public int getSize() {
      return this.size;
    }

    public void setSize(final int size) {
      this.size = size;
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
