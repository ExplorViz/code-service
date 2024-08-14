package net.explorviz.code.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.explorviz.code.dto.Metrics;
import net.explorviz.code.helper.LandscapeStructureHelper;
import net.explorviz.code.persistence.CommitReport;
import net.explorviz.code.persistence.FileReport;
import net.explorviz.code.persistence.FileReport.ClassData2;
import net.explorviz.code.persistence.FileReport.ClassData2.MethodData2;

/**
 * ...
 */
@Path("/v2/code/metrics")
public class MetricResource {

  private final LandscapeStructureHelper landscapeStructureHelper;

  @Inject
  public MetricResource(final LandscapeStructureHelper landscapeStructureHelper) {
    this.landscapeStructureHelper = landscapeStructureHelper;
  }

  /**
   * ... * @param token the landscape token * @param appName the application name. * @param commit
   * the commit id. * @return the metrics and meta-data for all of a commit's files.
   */
  @Path("{token}/{appName}/{commit}")
  @GET
  public Metrics list(final String token, // NOPMD
      final String appName, final String commit) {

    final CommitReport commitReport = CommitReport.findByTokenAndApplicationNameAndCommitId(token,
        appName, commit);

    if (commitReport == null) {
      return new Metrics(List.of(), List.of(), List.of(), List.of());
    }

    final List<Map<String, String>> fileMetrics = new ArrayList<>();
    final List<Map<String, Map<String, String>>> classMetrics = new ArrayList<>();
    final List<Map<String, Map<String, String>>> methodMetrics = new ArrayList<>();

    final List<FileReport> relatedFileReports =
        this.landscapeStructureHelper.getFileReports(token, appName, commit,
            commitReport.getFiles());

    for (final FileReport fileReport : relatedFileReports) {
      if (fileReport == null) {
        fileMetrics.add(null);
        classMetrics.add(null);
        methodMetrics.add(null);
        continue;
      }

      final Map<String, String> fileMetric = fileReport.getFileMetric();
      fileMetrics.add(fileMetric);

      final Map<String, Map<String, String>> fqClassNameToMetricsMap = new HashMap<>(); // NOPMD
      final Map<String, Map<String, String>> fqMethodNameToMetricsMap = new HashMap<>(); // NOPMD
      final Map<String, ClassData2> classFqnToClassData = fileReport.getClassData();

      for (final Map.Entry<String, ClassData2> entry : classFqnToClassData.entrySet()) {
        final String className = entry.getKey();
        final ClassData2 classData = entry.getValue();
        final Map<String, String> classMetricMap = classData.getClassMetric();
        fqClassNameToMetricsMap.put(className, classMetricMap);

        final Map<String, MethodData2> methodNameToMethodData = classData.getMethodData();
        for (final Map.Entry<String, MethodData2> entry2 : methodNameToMethodData.entrySet()) {
          final String methodName = entry2.getKey();
          final Map<String, String> methodMetricMap = entry2.getValue().getMetric();
          fqMethodNameToMetricsMap.put(methodName, methodMetricMap);
        }


      }

      if (!fqClassNameToMetricsMap.isEmpty()) { // NOPMD
        classMetrics.add(fqClassNameToMetricsMap);
        if (!fqMethodNameToMetricsMap.isEmpty()) { // NOPMD
          methodMetrics.add(fqMethodNameToMetricsMap);
        } else {
          methodMetrics.add(null);
        }
      } else {
        classMetrics.add(null);
        methodMetrics.add(null);
      }

    }

    return new Metrics(commitReport.getFiles(), fileMetrics, classMetrics, methodMetrics);
  }
}
