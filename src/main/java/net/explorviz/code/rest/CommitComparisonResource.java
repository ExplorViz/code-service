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
import net.explorviz.code.beans.LandscapeStructure.Node.Application.Package;
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
   ** @param token the landscape token.
   ** @param appName the application name.
   ** @param firstCommit the commit id of the first selected commit.
   ** @param secondCommit the commit id of the second selected commit.
   ** @return the commit comparison matching the params.
   */
  @Path("{firstCommit}-{secondCommit}")
  @GET
  public  CommitComparison list(@RestPath final String token, // NOPMD
      @RestPath final String appName,  String firstCommit,  String secondCommit) {

    if (CommitComparisonHelper.getLatestCommonCommitId(firstCommit, secondCommit, token, appName)
        .equals(secondCommit)) {
      final String temp = firstCommit;
      firstCommit = secondCommit; // NOPMD
      secondCommit = temp; // NOPMD
    } 


    final List<String> added = CommitComparisonHelper.getComparisonAddedFiles(
        firstCommit, 
        secondCommit, token, appName);

    final List<String> addedPackages = new ArrayList<>();

    final List<String> modified = CommitComparisonHelper.getComparisonModifiedFiles(
        firstCommit, 
        secondCommit, token, appName);

    final List<String> deleted = CommitComparisonHelper.getComparisonDeletedFiles(
        firstCommit, 
        secondCommit, token, appName);

    final List<String> deletedPackages = new ArrayList<>();

    final List<Metric> metrics = new ArrayList<>();



    final List<Package> packagesFirstSelectedCommit = LandscapeStructureHelper
        .createListOfPackages(token, firstCommit, appName);
    final List<Package> packagesSecondSelectedCommit = LandscapeStructureHelper
        .createListOfPackages(token, secondCommit, appName);

    // fill addedPackages with the packages that are added
    for (final String fqFileName : added) {
      final String fqFileNameDotSeparator = fqFileName.replaceAll("/", ".");
      final FileReport fileReport = LandscapeStructureHelper.getFileReport(token, appName, 
          fqFileNameDotSeparator, secondCommit);
      if (fileReport != null) { // NOPMD
        final String packageFileName = fileReport.getPackageName() + "." + fileReport.getFileName();
        final String[] packageFileNameSplit = packageFileName.split("\\.");
        final int numThree = 3;
        final String lastPackageName = packageFileNameSplit[packageFileNameSplit.length - numThree];
        final Package packageFirstSelectedCommit = 
            LandscapeStructureResource
            .getPackageFromPath(packageFileName, packagesFirstSelectedCommit);
        final Package packageSecondSelectedCommit = 
            LandscapeStructureResource
            .getPackageFromPath(packageFileName, packagesSecondSelectedCommit);

        if (packageSecondSelectedCommit != null) { // NOPMD

          if (packageFirstSelectedCommit != null) { // NOPMD
            final String packageName = packageFirstSelectedCommit.getName();
      
            if (packageName.equals(lastPackageName)) {
              // no new package added
              addedPackages.add("");
            } else {
              // add package
              boolean timeToAdd = false;
              final StringBuilder subPackages = new StringBuilder(""); // NOPMD
              for (int i = 0; i <= packageFileNameSplit.length - numThree; i++) {
                if (timeToAdd) {
                  subPackages.append(packageFileNameSplit[i] + ".");
                }
                if (packageFileNameSplit[i].equals(packageName)) {
                  timeToAdd = true;
                }
              }

              if (subPackages.toString().length() > 0) { // NOPMD
                addedPackages.add(subPackages.toString().substring(0, 
                    subPackages.toString().length() - 1));
              } else { // shouldn't happen
                addedPackages.add("");
              }
            }
          } else {
            // every package is new
            addedPackages.add(fileReport.getPackageName());
          }
        } else { // NOPMD
          // should never happen. TODO: LOG error
          addedPackages.add("");
        }
      } else {
        addedPackages.add(""); // couldn't be resolved. 
        // Adds empty string to create a mapping between added files and its added packages
      }
    }

    // fill deletedPackages with the packages that are deleted
    for (final String fqFileName : deleted) {
      final String fqFileNameDotSeparator = fqFileName.replaceAll("/", ".");
      final FileReport fileReport = LandscapeStructureHelper.getFileReport(token, appName, 
          fqFileNameDotSeparator, firstCommit);
      if (fileReport != null) { // NOPMD
        final String packageFileName = fileReport.getPackageName() + "." + fileReport.getFileName();
        final String[] packageFileNameSplit = packageFileName.split("\\.");
        final int numThree = 3;
        final String lastPackageName = packageFileNameSplit[packageFileNameSplit.length - numThree];
        final Package packageFirstSelectedCommit = 
            LandscapeStructureResource
            .getPackageFromPath(packageFileName, packagesFirstSelectedCommit);
        final Package packageSecondSelectedCommit = 
            LandscapeStructureResource
            .getPackageFromPath(packageFileName, packagesSecondSelectedCommit);

        if (packageSecondSelectedCommit != null) { // NOPMD

          if (packageFirstSelectedCommit != null) { // NOPMD
            final String packageName = packageSecondSelectedCommit.getName();
      
            if (packageName.equals(lastPackageName)) {
              // no package deleted
              deletedPackages.add("");
            } else {
              // deleted packages
              boolean timeToAdd = false;
              final StringBuilder subPackages = new StringBuilder(""); // NOPMD
              for (int i = 0; i <= packageFileNameSplit.length - numThree; i++) {
                if (timeToAdd) {
                  subPackages.append(packageFileNameSplit[i] + ".");
                }
                if (packageFileNameSplit[i].equals(packageName)) {
                  timeToAdd = true;
                }
              }

              if (subPackages.toString().length() > 0) {
                deletedPackages.add(subPackages.toString().substring(0, 
                    subPackages.toString().length() - 1));
              } else { // shouldn't happen
                deletedPackages.add("");
              }
            }
          } else {
            // should not happen
            deletedPackages.add("");
          }
        } else { // NOPMD
          // every package is deleted
          deletedPackages.add(fileReport.getPackageName());
        }
      } else {
        deletedPackages.add(""); // couldn't be resolved. 
        // Adds empty string to create a mapping between deleted files and its deleted packages
      }
    }




    // add metrics from added-files
    final String secondCommitFinal = secondCommit;
    added.forEach(fqFileName -> {
      fqFileName = fqFileName.replaceAll("\\/", ".");
      final FileReport fileReport = LandscapeStructureHelper.getFileReport(token, 
          appName, fqFileName, secondCommitFinal);
      
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
    final String firstCommitFinal = firstCommit;
    modified.forEach(fqFileName -> {
      fqFileName = fqFileName.replaceAll("\\/", ".");
      final FileReport fileReportFirstSelectedCommit = LandscapeStructureHelper.getFileReport(
            token, appName, fqFileName, firstCommitFinal);

      final FileReport fileReportSecondSelectedCommit = LandscapeStructureHelper.getFileReport(
            token, appName, fqFileName, secondCommitFinal);
      
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

    return new CommitComparison(added, modified, deleted, addedPackages, deletedPackages, metrics);
  }
    
}
