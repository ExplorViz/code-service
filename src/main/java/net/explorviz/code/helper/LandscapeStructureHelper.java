package net.explorviz.code.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import net.explorviz.code.beans.LandscapeStructure;
import net.explorviz.code.beans.LandscapeStructure.Node;
import net.explorviz.code.beans.LandscapeStructure.Node.Application;
import net.explorviz.code.beans.LandscapeStructure.Node.Application.Package;
import net.explorviz.code.beans.LandscapeStructure.Node.Application.Package.Class;
import net.explorviz.code.beans.LandscapeStructure.Node.Application.Package.Class.Method;
import net.explorviz.code.mongo.CommitReport;
import net.explorviz.code.mongo.FileReport;
import net.explorviz.code.mongo.FileReport.ClassData2;
import net.explorviz.code.mongo.FileReport.ClassData2.MethodData2;
import net.explorviz.code.helper.CommitComparisonHelper;

/**
 * ...
 */
public class LandscapeStructureHelper {
    
  /**
   * ...
   ** @param commitId
   ** @return ...
   */
  public static List<LandscapeStructure.Node.Application.Package> createListOfPackages(
        //final String landscapeToken,
        final String commitId 
  /*,final String appName*/) { // TODO: app name
    final String landscapeToken = "default-token";
    final String appName = "default-app-name";

    CommitReport commitReport = CommitReport.findByCommitId(commitId); // TODO: and AppName
    List<String> files = commitReport.getFiles();

    final Map<String, LandscapeStructure.Node.Application.Package> packageNameToPackageMap = 
        new HashMap<>();
    final Map<String, LandscapeStructure.Node.Application.Package.Class> fqnClassNameToClass =
         new HashMap<>();
    final Set<String> firstLevelPackageNames = new HashSet<>();
    final Set<String> functionFqn = new HashSet<>();


    for (final String file : files) {
      final String[] fileAndFolders = file.split("/");
      final String fileAndFoldersWithDotSeparation = String.join(".", fileAndFolders);
      final String fileName = fileAndFolders[fileAndFolders.length - 1];
      final String fileNameWithoutFileExtension = fileName.split("\\.")[0];


      FileReport fileReport = getFileReport(landscapeToken, appName, 
          fileAndFoldersWithDotSeparation, commitId);

      if (fileReport == null) {
        continue;
      }

      final String packageName = fileReport.packageName;
      final String[] packages = packageName.split("\\.");
      Package parentPackage = null;
      Package currentPackage = null;
      for (int i = 0; i < packages.length; i++) {
        final String currentPackageName = packages[i];
        if (i == 0 && !firstLevelPackageNames.contains(currentPackageName)) {
          firstLevelPackageNames.add(currentPackageName);
        }

        String id = packages[0];
        for (int j = 0; j < i; j++) {
          id += "." + packages[j + 1];
        }

        // use full qualified name as id to avoid name clashes
        currentPackage = packageNameToPackageMap.get(id);
        if (currentPackage == null) {
          Package pckg = new Package();
          pckg.name = currentPackageName;
          pckg.subPackages = new ArrayList<>();
          pckg.classes = new ArrayList<>();
          currentPackage = pckg;
          packageNameToPackageMap.put(id, currentPackage);
        }


        if (parentPackage != null) {
          if (!parentPackage.subPackages.contains(currentPackage)) {
            parentPackage.subPackages.add(currentPackage);
          }
        }

        parentPackage = currentPackage;
      }

      String id = packageName + "." + fileNameWithoutFileExtension;
      Class clazz = fqnClassNameToClass.get(id);
      if (clazz == null) {
        clazz = new Class();
        clazz.name = fileNameWithoutFileExtension;
        clazz.methods = new ArrayList<>();
        fqnClassNameToClass.put(id, clazz);
      }

      // // fill clazz with methods
      ClassData2 classData;
      if (
          (classData = fileReport.classData.get(id)) == null /*TODO: walk through classData 
                                                                      entry set instead? */
      ) {
        continue;
      }

      final String superClass = classData.superClass;
      if (superClass != null) {
        clazz.superClass = superClass;
      }

      final Map<String, MethodData2> methodData = classData.methodData;
      
      if (methodData != null) {

        for (final Map.Entry<String, MethodData2> entry : methodData.entrySet()) {
          final String[] temp = entry.getKey().split("\\.");
          final String[] temp2 = temp[temp.length - 1].split("#");
          final String[] prefixFqn = Arrays.copyOfRange(temp, 0, temp.length - 1);
          final String methodName = temp2[0]; // TODO: if methodName is constructor we write <init>
          String methodFqn = "";
          for (String name : prefixFqn) {
            methodFqn += name + ".";
          }
          methodFqn += methodName; 

          // functionFqn really needed? Only if we want to "prevent" overloaded functions
          if (!functionFqn.contains(methodFqn)) { /* entry.getKey() instead of methodFqn? 
                                                     Otherwise we might miss overloaded functions */
            final Method method = new Method();
            method.name = methodName; // include parameter list due to overloaded functions?
            method.hashCode = "default-hash-code"; // TODO: real hash code
            clazz.methods.add(method);
            functionFqn.add(methodFqn);
          }
        }

      }

      //if (!currentPackage.classes.contains(clazz)) {
      currentPackage.classes.add(clazz);
      //}

    }

    List<Package> packageList = new ArrayList<>();
    firstLevelPackageNames.forEach(name -> {
      packageList.add(packageNameToPackageMap.get(name));
    });
    return packageList;
  }

  /**
   * ...
   ** @param landscapeToken ...
   ** @param appName ...
   ** @param fqFileName ...
   ** @param commitId ...
   ** @return ...
   */
  public static FileReport getFileReport(final String landscapeToken, final String appName, 
      final String fqFileName, final String commitId) {
    final String[] temp = fqFileName.split("\\.");
    final String fileName = temp[temp.length - 2] + "." + temp[temp.length - 1];
    FileReport fileReport = FileReport
          .findByTokenAndAppNameAndPackageNameAndFileNameAndCommitId(landscapeToken, appName, 
          fqFileName, commitId);

    if (fileReport == null) { // older commit has provided file report
      // TODO: use also packageName
      List<FileReport> fileReportList = FileReport.findByTokenAndAppNameAndFileName(
            landscapeToken, appName, fileName);

      List<FileReport> candidateFileReportList = fileReportList.stream().filter(fr -> 
              !CommitComparisonHelper.getLatestCommonCommitId(fr.commitId, 
              commitId, landscapeToken).equals(commitId) // get rid of file reports that
              // happen later than our commit
              &&
               CommitComparisonHelper.getLatestCommonCommitId(fr.commitId,
               commitId, landscapeToken).equals(fr.commitId) // get rid of file reports that 
              // have unordered commits w.r.t our commit
              &&
              fqFileName
              .contains(fr.packageName)).collect(Collectors.toList()); // get rid of file
      // reports that do not involve our file of interest

      Collections.sort(candidateFileReportList, (fr1, fr2) -> {
        if (CommitComparisonHelper.getLatestCommonCommitId(fr1.commitId, fr2.commitId, 
              landscapeToken).equals(fr1.commitId)) {
          return 1;
        } else { // no return of 0 necessary since no two commit id's will be the same in our list
          return -1;
        }
      });

      if (candidateFileReportList.size() > 0) {
        fileReport = candidateFileReportList.get(0);
      } else {
        fileReport = null;
      }
    }
    return fileReport;
  }
}
