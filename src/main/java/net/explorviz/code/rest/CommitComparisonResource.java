package net.explorviz.code.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.jboss.resteasy.reactive.RestPath;

import net.explorviz.code.beans.CommitComparison;
import net.explorviz.code.beans.CommitComparison.Metric;
import net.explorviz.code.beans.CommitComparison.Metric.MetricVal;
import net.explorviz.code.helper.CommitComparisonHelper;
import net.explorviz.code.helper.LandscapeStructureHelper;
import net.explorviz.code.mongo.FileReport;
import net.explorviz.code.mongo.FileReport.ClassData2;
import net.explorviz.code.mongo.FileReport.ClassData2.MethodData2;

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

        for (final Map.Entry<String, ClassData2> entry : fileReport.classData.entrySet()) {
          System.out.println("classData2 KEY: " + entry.getKey());
          Metric classMetric = new Metric();
          classMetric.entityName = entry.getKey();
          final Map<String, MetricVal> classMetricMap = new HashMap<>();
          final ClassData2 classData = entry.getValue();
          // add class metric
          for (final Map.Entry<String, String> classMetricEntry : 
              classData.classMetric.entrySet()) {
            final String key = classMetricEntry.getKey();
            final String val = classMetricEntry.getValue();
            MetricVal metricVal = new MetricVal();
            //metricVal.oldValue = "";
            metricVal.newValue = val;
            classMetricMap.put(key, metricVal);
          }

          classMetric.metricMap = classMetricMap;
          metrics.add(classMetric);
        

          // add method metric
          Map<String, MethodData2> methodData = classData.methodData;

          for (final Map.Entry<String, MethodData2> methodMetricEntry : methodData.entrySet()) {
            final Metric methodMetric = new Metric();
            methodMetric.entityName = methodMetricEntry.getKey();

            final Map<String, MetricVal> methodMetricMap = new HashMap<>();
            final MethodData2 val = methodMetricEntry.getValue();

            for (final Map.Entry<String, String> methodMetricEntryEntry : val.metric.entrySet()) {
              final String metricKey = methodMetricEntryEntry.getKey();
              final String metricVal = methodMetricEntryEntry.getValue();
              
              final MetricVal metricVal2 = new MetricVal();
              //metricVal.oldValue = "";
              metricVal2.newValue = metricVal;
              methodMetricMap.put(metricKey, metricVal2);
            }
            methodMetric.metricMap = methodMetricMap;
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
            .classData.entrySet()) {
          Metric classMetric = new Metric();
          classMetric.entityName = entry.getKey();
          ClassData2 classDataSecondSelectedCommit = fileReportSecondSelectedCommit.classData
              .get(classMetric.entityName);
          final Map<String, MetricVal> classMetricMap = new HashMap<>();
          final ClassData2 classDataFirstSelectedCommit = entry.getValue();
          // add class metric
          for (final Map.Entry<String, String> classMetricEntry : 
              classDataFirstSelectedCommit.classMetric.entrySet()) {
            final String key = classMetricEntry.getKey();
            final String val = classMetricEntry.getValue();
            MetricVal metricVal = new MetricVal();
            metricVal.oldValue = val;

            if (classDataSecondSelectedCommit != null) {
              final String newVal = classDataSecondSelectedCommit.classMetric.get(key);
              metricVal.newValue = newVal;
            }
            classMetricMap.put(key, metricVal);
          }

          classMetric.metricMap = classMetricMap;
          metrics.add(classMetric);
        

          // add method metric
          Map<String, MethodData2> methodData = classDataFirstSelectedCommit.methodData;

          for (final Map.Entry<String, MethodData2> methodMetricEntry : methodData.entrySet()) {
            final Metric methodMetric = new Metric();
            methodMetric.entityName = methodMetricEntry.getKey();

            final Map<String, MetricVal> methodMetricMap = new HashMap<>();
            final MethodData2 val = methodMetricEntry.getValue();
            MethodData2 val2 = null;
            if (classDataSecondSelectedCommit != null) {
              val2 = classDataSecondSelectedCommit.methodData.get(methodMetricEntry.getKey());
            }

            for (final Map.Entry<String, String> methodMetricEntryEntry : val.metric.entrySet()) {
              final String metricKey = methodMetricEntryEntry.getKey();
              final String metricVal = methodMetricEntryEntry.getValue();
              
              final MetricVal metricVal2 = new MetricVal();
              metricVal2.oldValue = metricVal;
              if (val2 != null) {
                metricVal2.newValue = val2.metric.get(metricKey);
              }
              methodMetricMap.put(metricKey, metricVal2);
            }
            methodMetric.metricMap = methodMetricMap;
            metrics.add(methodMetric);
          }
        }
      }
    }); 

    return new CommitComparison(added, modified, deleted, metrics);
  }
    
}
