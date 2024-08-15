package net.explorviz.code.persistence.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.explorviz.code.helper.FilePathInfo;
import net.explorviz.code.persistence.entity.FileReport;

@ApplicationScoped
public class FileReportRepository implements PanacheMongoRepository<FileReport> {

  /**
   * ... * @param landscapeToken the landscape token. * @param appName the application name * @param
   * fqFileName the full qualified file name. * @param commitId the commit id. * @return the
   * FileReport that matches the above params.
   */
  public FileReport findByTokenAndAppNameAndPackageNameAndFileNameAndCommitId(// NOPMD
      final String landscapeToken,
      final String appName,
      final String fqFileName, final String commitId) {

    FilePathInfo filePathInfo = FilePathInfo.build(fqFileName);

    if (filePathInfo.getFileNameWithFileExtension() == null) {
      return null;
    }

    final List<FileReport> fileReportList = find(
        "landscapeToken = ?1 and appName = ?2 and fileName =?3 and commitId =?4",
        landscapeToken, appName, filePathInfo.getFileNameWithFileExtension(), commitId).list();

    // e.g. Options.java might be included in different packages, therefore also check for the
    // right folder path
    final List<FileReport> filterFileReportList = fileReportList.stream()
        .filter(fr -> filePathInfo.getFoldersWithDotSeparation().endsWith(fr.getPackageName()))
        .toList();

    if (filterFileReportList.size() == 1) { // NOPMD
      return filterFileReportList.getFirst();
    } else {
      return null;
    }
  }

  public List<FileReport> findByTokenAndAppNameAndFileName(
      final String landscapeToken,
      final String appName, final String fileName) {
    return find("landscapeToken = ?1 and appName = ?2 and fileName =?3",
        landscapeToken, appName, fileName).list();
  }

  /**
   * Uses Batch Requests to fetch multiple FileReports in a single query.
   *
   * @param landscapeToken   the encompassing token
   * @param appName          the encompassing appName
   * @param commitIdToFqnMap Map containing the commit identifier and the fqns that this functions
   *                         should fetch. The commit ids have to be the actual ones.
   * @return The list of fetched FileReports.
   */
  public List<FileReport> getFileReports(String landscapeToken, String appName,
      Map<String, List<String>> commitIdToFqnMap) {

    List<FileReport> fileReportList = new ArrayList<>();

    for (var entry : commitIdToFqnMap.entrySet()) {

      final String commitId = entry.getKey();

      if (commitId == null) {
        continue;
      }

      final List<String> fqFileNames = entry.getValue();

      final List<String> fileNames = new ArrayList<>();

      for (final String fqFileName : fqFileNames) {
        FilePathInfo filePathInfo = FilePathInfo.build(fqFileName);

        if ((filePathInfo != null ? filePathInfo.getFileNameWithFileExtension() : null) == null) {
          continue;
        }

        fileNames.add(filePathInfo.getFileNameWithFileExtension());
      }

      fileReportList.addAll(
          find("landscapeToken = ?1 and appName = ?2 and fileName in ?3 and commitId =?4",
              landscapeToken, appName, fileNames, commitId).list());
    }

    return fileReportList;
  }
}
