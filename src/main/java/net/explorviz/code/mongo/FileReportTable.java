package net.explorviz.code.mongo;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import java.util.Map;
import java.util.Objects;

/**
 * ...
 */

public class FileReportTable extends PanacheMongoEntity {

  private String landscapeToken;
  private String appName;
  // finds the commit id for the corresponding file report
  private Map<String, Map<String, String>> commitIdTofqnFileNameToCommitIdMap;

  @Override
  public int hashCode() {
    return Objects.hash(landscapeToken, appName, commitIdTofqnFileNameToCommitIdMap);
  }

  /**
   * Finds FileReportTable for passed token and appName.
   *
   * @param landscapeToken the landscape token.
   * @param appName        the application name
   * @return the FileReportTable that matches the above params.
   */
  public static FileReportTable findByTokenAndAppName(// NOPMD
      final String landscapeToken,
      final String appName) {

    final FileReportTable fileReportTable = find(
        "landscapeToken = ?1 and appName = ?2",
        landscapeToken, appName).firstResult();

    return fileReportTable;
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
