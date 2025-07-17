package net.explorviz.code.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.explorviz.code.dto.Metrics;
import net.explorviz.code.helper.LandscapeStructureHelper;
import net.explorviz.code.persistence.entity.CommitReport;
import net.explorviz.code.persistence.entity.FileReport;
import net.explorviz.code.persistence.entity.FileReport.ClassData2;
import net.explorviz.code.persistence.entity.FileReport.ClassData2.MethodData2;
import net.explorviz.code.persistence.repository.CommitReportRepository;

/**
 * ...
 */
@Path("/v2/code/metrics")
public class MetricResource {

  private final LandscapeStructureHelper landscapeStructureHelper;
  private final CommitReportRepository commitReportRepository;

  @Inject
  public MetricResource(final LandscapeStructureHelper landscapeStructureHelper,
      final CommitReportRepository commitReportRepository) {
    this.landscapeStructureHelper = landscapeStructureHelper;
    this.commitReportRepository = commitReportRepository;
  }

  /**
   * Retrieves the metrics and metadata for all of a commit's files.
   *
   * @param token The landscape token.
   * @param appName The application name.
   * @param commit The commit ID.
   * @return The metrics and metadata for all of a commit's files.
   */
  @Path("{token}/{appName}/{commit}")
  @GET
  public Metrics list(final String token, // NOPMD
      final String appName, final String commit) {

    final CommitReport commitReport =
        this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(token,
            appName, commit);

    if (commitReport == null) {
      return new Metrics(new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    final Map<String, Map<String, String>> fileMetrics = new HashMap<>();
    final Map<String, Map<String, String>> classMetrics = new HashMap<>();
    final Map<String, Map<String, String>> methodMetrics = new HashMap<>();

    final List<FileReport> relatedFileReports =
        this.landscapeStructureHelper.getFileReports(token, appName, commit,
            commitReport.files());

    for (final FileReport fileReport : relatedFileReports) {
      if (fileReport == null) {
        continue;
      }

      final Map<String, String> fileMetric = fileReport.getFileMetric();
      fileMetrics.put(fileReport.getFileName(), fileMetric);

      final Map<String, ClassData2> classFqnToClassData = fileReport.getClassData();

      for (final Map.Entry<String, ClassData2> entry : classFqnToClassData.entrySet()) {
        final String className = entry.getKey();
        final ClassData2 classData = entry.getValue();
        final Map<String, String> classMetricMap = classData.getClassMetric();
        classMetrics.put(className, classMetricMap);

        final Map<String, MethodData2> methodNameToMethodData = classData.getMethodData();
        for (final Map.Entry<String, MethodData2> entry2 : methodNameToMethodData.entrySet()) {
          final Map<String, String> methodMetricMap = entry2.getValue().getMetric();
          methodMetrics.put(className, methodMetricMap);
        }
      }
    }

    return new Metrics(fileMetrics, classMetrics, methodMetrics);
  }
}
