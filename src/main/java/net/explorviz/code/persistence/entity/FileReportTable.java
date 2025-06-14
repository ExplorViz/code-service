package net.explorviz.code.persistence.entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import java.util.Map;
import java.util.Objects;

/**
 * The idea behind the FileReportTable is to determine, from the perspective of a given "standpoint" commit,
 * the most recent commit (possibly earlier than the current one) in which a file report was generated.
 *
 * A file report is only created for files that were added or modified in a commit.
 * From the standpoint of a given commit, the relevant file report is always associated
 * with the latest commit in which that file was changed. This means the most recent
 * possible commit for a file report is the standpoint commit itself (if it modified the file).
 */

public class FileReportTable extends PanacheMongoEntity {

  private String landscapeToken;
  private String appName;
  // finds the commit id for the corresponding file report
  private Map<String, Map<String, String>> commitIdTofqnFileNameToCommitIdMap; // standpoint commit id -> Map<file name, commit id>

  @Override
  public int hashCode() {
    return Objects.hash(landscapeToken, appName, commitIdTofqnFileNameToCommitIdMap);
  }

  public String getLandscapeToken() {
    return this.landscapeToken;
  }

  public void setLandscapeToken(final String landscapeToken) {
    this.landscapeToken = landscapeToken;
  }

  public String getAppName() {
    return this.appName;
  }

  public void setAppName(final String appName) {
    this.appName = appName;
  }

  public Map<String, Map<String, String>> getCommitIdTofqnFileNameToCommitIdMap() {
    return this.commitIdTofqnFileNameToCommitIdMap;
  }

  public void setCommitIdTofqnFileNameToCommitIdMap(
      final Map<String, Map<String, String>> commitIdTofqnFileNameToCommitIdMap) {
    this.commitIdTofqnFileNameToCommitIdMap = commitIdTofqnFileNameToCommitIdMap;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FileReportTable that = (FileReportTable) o;
    return Objects.equals(landscapeToken, that.landscapeToken) && Objects.equals(
        appName, that.appName) && Objects.equals(commitIdTofqnFileNameToCommitIdMap,
        that.commitIdTofqnFileNameToCommitIdMap);
  }

}
