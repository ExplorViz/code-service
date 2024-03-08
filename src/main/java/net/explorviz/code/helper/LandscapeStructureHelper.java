package net.explorviz.code.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.explorviz.code.beans.LandscapeStructure.Node.Application.Package;
import net.explorviz.code.beans.LandscapeStructure.Node.Application.Package.Class;
import net.explorviz.code.beans.LandscapeStructure.Node.Application.Package.Class.Method;
import net.explorviz.code.mongo.CommitReport;
import net.explorviz.code.mongo.FileReport;
import net.explorviz.code.mongo.FileReport.ClassData2;
import net.explorviz.code.mongo.FileReport.ClassData2.MethodData2;


/**
 * ...
 */
public final class LandscapeStructureHelper {

  private LandscapeStructureHelper() {
  }
    
  /**
   * ...
   ** @param landscapeToken the landscape token.
   ** @param commitId the commit id.
   ** @param appName the application name.
   ** @return the list of "first-order" packages of an application matching above params.
   */
  public static List<Package> createListOfPackages(// NOPMD
        final String landscapeToken,
        final String commitId, final String appName) {

    final CommitReport commitReport = CommitReport.findByTokenAndApplicationNameAndCommitId(
        landscapeToken, appName, commitId);
    if (commitReport == null) {
      return null;
    }
    final List<String> files = commitReport.getFiles();

    final Map<String, Package> packageNameToPackageMap = 
        new HashMap<>();
    final Map<String, Class> fqnClassNameToClass =
         new HashMap<>();
    final Set<String> firstLevelPackageNames = new HashSet<>();
    final Set<String> functionFqn = new HashSet<>();


    for (final String file : files) {
      final String[] fileAndFolders = file.split("/");
      final String fileAndFoldersWithDotSeparation = String.join(".", fileAndFolders); // NOPMD
      final String fileName = fileAndFolders[fileAndFolders.length - 1];
      final String fileNameWithoutFileExtension = fileName.split("\\.")[0]; // NOPMD


      final FileReport fileReport = getFileReport(landscapeToken, appName, 
          fileAndFoldersWithDotSeparation, commitId);

      if (fileReport == null) {
        continue;
      }

      final String packageName = fileReport.getPackageName();
      final String[] packages = packageName.split("\\.");
      Package parentPackage = null;
      Package currentPackage = null;
      for (int i = 0; i < packages.length; i++) {
        final String currentPackageName = packages[i];
        if (i == 0 && !firstLevelPackageNames.contains(currentPackageName)) {
          firstLevelPackageNames.add(currentPackageName);
        }

        final StringBuilder id = new StringBuilder(packages[0]); // NOPMD
        for (int j = 0; j < i; j++) {
          id.append("." + packages[j + 1]); 
        }

        // use full qualified name as id to avoid name clashes
        currentPackage = packageNameToPackageMap.get(id.toString());
        if (currentPackage == null) {
          final Package pckg = new Package(); // NOPMD
          pckg.setName(currentPackageName);
          pckg.setSubPackages(new ArrayList<>()); // NOPMD
          pckg.setClasses(new ArrayList<>()); // NOPMD
          currentPackage = pckg;
          packageNameToPackageMap.put(id.toString(), currentPackage);
        }



        if (parentPackage != null && !parentPackage.getSubPackages().contains(currentPackage)) {
          parentPackage.getSubPackages().add(currentPackage);
        }
        

        parentPackage = currentPackage;
      }

      final String id = packageName + "." + fileNameWithoutFileExtension;
      Class clazz = fqnClassNameToClass.get(id);
      if (clazz == null) {
        clazz = new Class();  // NOPMD
        clazz.setName(fileNameWithoutFileExtension);
        clazz.setMethods(new ArrayList<>()); // NOPMD
        fqnClassNameToClass.put(id, clazz);
      }

      // // fill clazz with methods
      ClassData2 classData;
      if (
          (classData = fileReport.getClassData().get(id)) == null // NOPMD /
      // TODO: walk through classData entry set instead 
      // because a class file could (but shouldn't) have multiple first level classes defined
      ) {
        continue;
      }

      final String superClass = classData.getSuperClass();
      if (superClass != null) {
        clazz.setSuperClass(superClass);
      }

      final Map<String, MethodData2> methodData = classData.getMethodData();
      
      if (methodData != null) {

        for (final Map.Entry<String, MethodData2> entry : methodData.entrySet()) {
          final String[] temp = entry.getKey().split("\\.");
          final String[] temp2 = temp[temp.length - 1].split("#");
          final String[] prefixFqn = Arrays.copyOfRange(temp, 0, temp.length - 1);
          final String methodName = temp2[0]; // TODO: if methodName is constructor we write <init>
          final StringBuilder methodFqn = new StringBuilder(""); // NOPMD
          for (final String name : prefixFqn) {
            methodFqn.append(name + ".");
          }
          methodFqn.append(methodName); 

          // functionFqn really needed? Only if we want to "prevent" overloaded functions
          if (!functionFqn.contains(methodFqn.toString())) { 
            /* entry.getKey() instead of methodFqn? 
            Otherwise we might miss overloaded functions */
            final Method method = new Method(); // NOPMD
            method.setName(methodName); // include parameter list due to overloaded functions?
            method.setMethodHash(methodName + "defaulthashcode"); // TODO: real hash code
            clazz.getMethods().add(method);
            functionFqn.add(methodFqn.toString());
          }
        }

      }

      //if (!currentPackage.classes.contains(clazz)) {
      currentPackage.getClasses().add(clazz);
      //}

    }

    final List<Package> packageList = new ArrayList<>();
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
  public static FileReport getFileReport(final String landscapeToken, final String appName, // NOPMD
      final String fqFileName, final String commitId) {
    final String[] temp = fqFileName.split("\\.");
    final String fileName;
    try {
      fileName = temp[temp.length - 2] + "." + temp[temp.length - 1];
    } catch (ArrayIndexOutOfBoundsException e) {
      return null;
    }
    
    FileReport fileReport = FileReport
          .findByTokenAndAppNameAndPackageNameAndFileNameAndCommitId(landscapeToken, appName, 
          fqFileName, commitId);

    if (fileReport == null) { // older commit has provided file report

      final List<FileReport> fileReportList = FileReport.findByTokenAndAppNameAndFileName(
            landscapeToken, appName, fileName);

      final List<FileReport> candidateFileReportList = fileReportList.stream().filter(fr -> 
              !CommitComparisonHelper.getLatestCommonCommitId(fr.getCommitId(), 
              commitId, landscapeToken, appName).equals(commitId) // get rid of file reports that
              // happen later than our commit
              &&
               CommitComparisonHelper.getLatestCommonCommitId(fr.getCommitId(),
               commitId, landscapeToken, appName)
                   .equals(fr.getCommitId()) // get rid of file reports that 
              // have unordered commits w.r.t our commit
              &&
              fqFileName
              .contains(fr.getPackageName())).collect(Collectors.toList()); // get rid of file
      // reports that do not involve our file of interest

      Collections.sort(candidateFileReportList, (fr1, fr2) -> {
        if (CommitComparisonHelper.getLatestCommonCommitId(fr1.getCommitId(), fr2.getCommitId(), 
              landscapeToken, appName).equals(fr1.getCommitId())) {
          return 1;
        } else { // no return of 0 necessary since no two commit id's will be the same in our list
          return -1;
        }
      });

      fileReport = null; // NOPMD
      if (!candidateFileReportList.isEmpty()) {
        fileReport = candidateFileReportList.get(0);
      }
    }
    return fileReport;
  }
}
