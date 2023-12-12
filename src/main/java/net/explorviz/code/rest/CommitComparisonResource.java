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
  public  CommitComparison list(@RestPath final String token, // NOPMD
      @RestPath final String appName, final String firstCommit, final String secondCommit) {


    final List<String> added = CommitComparisonHelper.getComparisonAddedFiles(
        firstCommit, 
        secondCommit, token, appName);

    final List<String> modified = CommitComparisonHelper.getComparisonModifiedFiles(
        firstCommit, 
        secondCommit, token, appName);

    final List<String> deleted = CommitComparisonHelper.getComparisonDeletedFiles(
        firstCommit, 
        secondCommit, token, appName);

    final List<Metric> metrics = new ArrayList<>();

    // add metrics from added-files
    added.forEach(fqFileName -> {
      fqFileName = fqFileName.replaceAll("\\/", ".");
      final FileReport fileReport = LandscapeStructureHelper.getFileReport(token, 
          appName, fqFileName, secondCommit);
      
      if (fileReport != null) {

        for (final Map.Entry<String, ClassData2> entry : fileReport.getClassData().entrySet()) {
          final Metric classMetric = new Metric(); // NOPMD
          classMetric.setEntityName(entry.getKey());
          final Map<String, MetricVal> classMetricMap = new HashMap<>(); // NOPMD
          final ClassData2 classData = entry.getValue();
          // add class metric
          for (final Map.Entry<String, String> classMetricEntry : 
              classData.getClassMetric().entrySet()) {
            final String key = classMetricEntry.getKey();
            final String val = classMetricEntry.getValue();
            MetricVal metricVal = new MetricVal(); // NOPMD
            metricVal.setNewValue(val);
            classMetricMap.put(key, metricVal);
          }

          classMetric.setMetricMap(classMetricMap);
          metrics.add(classMetric);
        

          // add method metric
          final Map<String, MethodData2> methodData = classData.getMethodData();

          for (final Map.Entry<String, MethodData2> methodMetricEntry : methodData.entrySet()) {
            final Metric methodMetric = new Metric(); // NOPMD
            methodMetric.setEntityName(methodMetricEntry.getKey());

            final Map<String, MetricVal> methodMetricMap = new HashMap<>(); // NOPMD
            final MethodData2 val = methodMetricEntry.getValue();

            for (final Map.Entry<String, String> methodMetricEntryEntry : val.getMetric()
                .entrySet()) {
              final String metricKey = methodMetricEntryEntry.getKey();
              final String metricVal = methodMetricEntryEntry.getValue();
              
              final MetricVal metricVal2 = new MetricVal(); // NOPMD
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
      final FileReport fileReportFirstSelectedCommit = LandscapeStructureHelper.getFileReport(
            token, appName, fqFileName, firstCommit);

      final FileReport fileReportSecondSelectedCommit = LandscapeStructureHelper.getFileReport(
            token, appName, fqFileName, secondCommit);
      
      if (fileReportFirstSelectedCommit != null && fileReportSecondSelectedCommit != null) {

        for (final Map.Entry<String, ClassData2> entry : fileReportFirstSelectedCommit
            .getClassData().entrySet()) {
          final Metric classMetric = new Metric(); // NOPMD
          classMetric.setEntityName(entry.getKey());
          final ClassData2 classDataSecondSelectedCommit = 
              fileReportSecondSelectedCommit.getClassData()
              .get(classMetric.getEntityName());
          final Map<String, MetricVal> classMetricMap = new HashMap<>(); // NOPMD
          final ClassData2 classDataFirstSelectedCommit = entry.getValue();
          // add class metric
          for (final Map.Entry<String, String> classMetricEntry : 
              classDataFirstSelectedCommit.getClassMetric().entrySet()) {
            final String key = classMetricEntry.getKey();
            final String val = classMetricEntry.getValue();
            final MetricVal metricVal = new MetricVal(); // NOPMD
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
          final Map<String, MethodData2> methodData = classDataFirstSelectedCommit.getMethodData();

          for (final Map.Entry<String, MethodData2> methodMetricEntry : methodData.entrySet()) {
            final Metric methodMetric = new Metric(); // NOPMD
            methodMetric.setEntityName(methodMetricEntry.getKey());

            final Map<String, MetricVal> methodMetricMap = new HashMap<>(); // NOPMD
            final MethodData2 val = methodMetricEntry.getValue();
            MethodData2 val2 = null;
            if (classDataSecondSelectedCommit != null) {
              val2 = classDataSecondSelectedCommit.getMethodData().get(methodMetricEntry.getKey());
            }

            for (final Map.Entry<String, String> methodMetricEntryEntry : val.getMetric()
                .entrySet()) {
              final String metricKey = methodMetricEntryEntry.getKey();
              final String metricVal = methodMetricEntryEntry.getValue();
              
              final MetricVal metricVal2 = new MetricVal(); // NOPMD
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
