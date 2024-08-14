package net.explorviz.code.persistence.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import net.explorviz.code.persistence.FileReportTable;

@ApplicationScoped
public class FileReportTableRepository implements PanacheMongoRepository<FileReportTable> {


  /**
   * Finds FileReportTable for passed token and appName.
   *
   * @param landscapeToken the landscape token.
   * @param appName        the application name
   * @return the FileReportTable that matches the above params.
   */
  public FileReportTable findByTokenAndAppName(// NOPMD
      final String landscapeToken,
      final String appName) {

    return find(
        "landscapeToken = ?1 and appName = ?2",
        landscapeToken, appName).firstResult();
  }
}
