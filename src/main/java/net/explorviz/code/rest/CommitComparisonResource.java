package net.explorviz.code.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import net.explorviz.code.beans.CommitComparison;
import net.explorviz.code.beans.CommitComparison.Metric;
import net.explorviz.code.beans.CommitComparison.Metric.MetricVal;
import net.explorviz.code.helper.CommitComparisonHelper;
import net.explorviz.code.helper.LandscapeStructureHelper;
import net.explorviz.code.mongo.FileReport;
import net.explorviz.code.mongo.FileReport.ClassData2;
import net.explorviz.code.mongo.FileReport.ClassData2.MethodData2;
import org.jboss.resteasy.reactive.RestPath;


/**
 * ...
 */

@Path("/commit-comparison/{token}/{appName}")
public class CommitComparisonResource {

  /**
   * ...
   ** @return ...
   */
  @Path("{firstCommit}-{secondCommit}")
  @GET
  public  CommitComparison list(@RestPath String token, @RestPath String appName, 
      String firstCommit, String secondCommit) {
    final String firstSelectedCommitId = firstCommit;
    final String secondSelectedCommitId = secondCommit;
    final String landscapeToken = token;
    final String applicationName = appName;

    List<String> added = CommitComparisonHelper.getComparisonAddedFiles(
        firstSelectedCommitId, 
        secondSelectedCommitId, landscapeToken, applicationName);

    List<String> modified = CommitComparisonHelper.getComparisonModifiedFiles(
        firstSelectedCommitId, 
        secondSelectedCommitId, landscapeToken, applicationName);

    List<String> deleted = CommitComparisonHelper.getComparisonDeletedFiles(
        firstSelectedCommitId, 
        secondSelectedCommitId, landscapeToken, applicationName);

    List<Metric> metrics = new ArrayList<>();

    // add metrics from added-files
    added.forEach(fqFileName -> {
      System.out.println("added fqFileName: " + fqFileName);
      fqFileName = fqFileName.replaceAll("\\/", ".");
      FileReport fileReport = LandscapeStructureHelper.getFileReport(landscapeToken, 
          applicationName, fqFileName, secondSelectedCommitId);
      
      if (fileReport != null) {
        System.out.println("NOT NULL");

        for (final Map.Entry<String, ClassData2> entry : fileReport.getClassData().entrySet()) {
          System.out.println("classData2 KEY: " + entry.getKey());
          Metric classMetric = new Metric();
          classMetric.setEntityName(entry.getKey());
          final Map<String, MetricVal> classMetricMap = new HashMap<>();
          final ClassData2 classData = entry.getValue();
          // add class metric
          for (final Map.Entry<String, String> classMetricEntry : 
              classData.getClassMetric().entrySet()) {
            final String key = classMetricEntry.getKey();
            final String val = classMetricEntry.getValue();
            MetricVal metricVal = new MetricVal();
            metricVal.setNewValue(val);
            classMetricMap.put(key, metricVal);
          }

          classMetric.setMetricMap(classMetricMap);
          metrics.add(classMetric);
        

          // add method metric
          Map<String, MethodData2> methodData = classData.getMethodData();

          for (final Map.Entry<String, MethodData2> methodMetricEntry : methodData.entrySet()) {
            final Metric methodMetric = new Metric();
            methodMetric.setEntityName(methodMetricEntry.getKey());

            final Map<String, MetricVal> methodMetricMap = new HashMap<>();
            final MethodData2 val = methodMetricEntry.getValue();

            for (final Map.Entry<String, String> methodMetricEntryEntry : val.getMetric()
                .entrySet()) {
              final String metricKey = methodMetricEntryEntry.getKey();
              final String metricVal = methodMetricEntryEntry.getValue();
              
              final MetricVal metricVal2 = new MetricVal();
              //metricVal.oldValue = "";
              metricVal2.setNewValue(metricVal);
              methodMetricMap.put(metricKey, metricVal2);
            }
            methodMetric.setMetricMap(methodMetricMap);
            metrics.add(methodMetric);
          }
        }
      }
    });

    // add metrics from modified-files
    modified.forEach(fqFileName -> {
      fqFileName = fqFileName.replaceAll("\\/", ".");
      FileReport fileReportFirstSelectedCommit = LandscapeStructureHelper.getFileReport(
            landscapeToken, applicationName, fqFileName, firstSelectedCommitId);

      FileReport fileReportSecondSelectedCommit = LandscapeStructureHelper.getFileReport(
            landscapeToken, applicationName, fqFileName, secondSelectedCommitId);
      
      if (fileReportFirstSelectedCommit != null && fileReportSecondSelectedCommit != null) {

        for (final Map.Entry<String, ClassData2> entry : fileReportFirstSelectedCommit
            .getClassData().entrySet()) {
          Metric classMetric = new Metric();
          classMetric.setEntityName(entry.getKey());
          ClassData2 classDataSecondSelectedCommit = fileReportSecondSelectedCommit.getClassData()
              .get(classMetric.getEntityName());
          final Map<String, MetricVal> classMetricMap = new HashMap<>();
          final ClassData2 classDataFirstSelectedCommit = entry.getValue();
          // add class metric
          for (final Map.Entry<String, String> classMetricEntry : 
              classDataFirstSelectedCommit.getClassMetric().entrySet()) {
            final String key = classMetricEntry.getKey();
            final String val = classMetricEntry.getValue();
            MetricVal metricVal = new MetricVal();
            metricVal.setOldValue(val);

            if (classDataSecondSelectedCommit != null) {
              final String newVal = classDataSecondSelectedCommit.getClassMetric().get(key);
              metricVal.setNewValue(newVal);
            }
            classMetricMap.put(key, metricVal);
          }

          classMetric.setMetricMap(classMetricMap);
          metrics.add(classMetric);
        

          // add method metric
          Map<String, MethodData2> methodData = classDataFirstSelectedCommit.getMethodData();

          for (final Map.Entry<String, MethodData2> methodMetricEntry : methodData.entrySet()) {
            final Metric methodMetric = new Metric();
            methodMetric.setEntityName(methodMetricEntry.getKey());

            final Map<String, MetricVal> methodMetricMap = new HashMap<>();
            final MethodData2 val = methodMetricEntry.getValue();
            MethodData2 val2 = null;
            if (classDataSecondSelectedCommit != null) {
              val2 = classDataSecondSelectedCommit.getMethodData().get(methodMetricEntry.getKey());
            }

            for (final Map.Entry<String, String> methodMetricEntryEntry : val.getMetric()
                .entrySet()) {
              final String metricKey = methodMetricEntryEntry.getKey();
              final String metricVal = methodMetricEntryEntry.getValue();
              
              final MetricVal metricVal2 = new MetricVal();
              metricVal2.setOldValue(metricVal);
              if (val2 != null) {
                metricVal2.setNewValue(val2.getMetric().get(metricKey));
              }
              methodMetricMap.put(metricKey, metricVal2);
            }
            methodMetric.setMetricMap(methodMetricMap);
            metrics.add(methodMetric);
          }
        }
      }
    }); 

    return new CommitComparison(added, modified, deleted, metrics);
  }
    
}
