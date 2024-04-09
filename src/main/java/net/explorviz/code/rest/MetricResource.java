package net.explorviz.code.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import net.explorviz.code.beans.Metrics;
import net.explorviz.code.helper.LandscapeStructureHelper;
import net.explorviz.code.mongo.CommitReport;
import net.explorviz.code.mongo.FileReport;
import net.explorviz.code.mongo.FileReport.ClassData2;
import net.explorviz.code.mongo.FileReport.ClassData2.MethodData2;

/**
 * ...
 */
@Path("/metrics")
public class MetricResource {

  /**
   * ...
   ** @param token the landscape token
   ** @param appName the application name.
   ** @param commit the commit id.
   ** @return the metrics and meta-data for all of a commit's files.
   */
  @Path("{token}/{appName}/{commit}")
  @GET
  public Metrics list(final String token, // NOPMD
      final String appName, final String commit) {

    final CommitReport commitReport = CommitReport.findByTokenAndApplicationNameAndCommitId(token, 
        appName, commit);

    final Metrics metrics = new Metrics();
    metrics.setFiles(commitReport.getFiles());

    final List<Map<String, String>> fileMetrics = new ArrayList<>();
    final List<Map<String, Map<String, String>>> classMetrics = new ArrayList<>();
    final List<Map<String, Map<String, String>>> methodMetrics = new ArrayList<>();

    for (final String fileName : commitReport.getFiles()) {
      final String fullQualifiedFileName = fileName.replaceAll("/", ".");
      final FileReport fileReport = LandscapeStructureHelper.getFileReport(token, appName, 
          fullQualifiedFileName, commit);

      if (fileReport == null) {
        fileMetrics.add(null);
        classMetrics.add(null);
        methodMetrics.add(null);
        continue;
      }
      

      final Map<String, String> fileMetric = fileReport.getFileMetric();
      if (fileMetric == null) {
        fileMetrics.add(null);
      } else {
        fileMetrics.add(fileMetric);
      }

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

    metrics.setFileMetrics(fileMetrics);
    metrics.setClassMetrics(classMetrics);
    metrics.setMethodMetrics(methodMetrics);
    return metrics;
  }
}
