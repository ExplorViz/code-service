package net.explorviz.code.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.explorviz.code.beans.LandscapeStructure.Node.Application.Package;
import net.explorviz.code.beans.LandscapeStructure.Node.Application.Package.Class;
import net.explorviz.code.beans.LandscapeStructure.Node.Application.Package.Class.Method;
import net.explorviz.code.mongo.CommitReport;
import net.explorviz.code.mongo.FileReport;
import net.explorviz.code.mongo.FileReport.ClassData2;
import net.explorviz.code.mongo.FileReport.ClassData2.MethodData2;
import net.explorviz.code.mongo.FileReportTable;


/**
 * ...
 */
public final class LandscapeStructureHelper {

  private LandscapeStructureHelper() {
  }

  /**
   * ... * @param landscapeToken the landscape token. * @param commitId the commit id. * @param
   * appName the application name. * @return the list of "first-order" packages of an application
   * matching above params.
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
        if (i == 0) {
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
        // because a class file could theoretically (but shouldn't practically)
        // have multiple first level classes defined
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
          final StringBuilder methodFqn = new StringBuilder(); // NOPMD
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
   * ... * @param landscapeToken the landscape token. * @param appName the application name. *
   * @param fqFileName the full qualified file name. * @param commitId the commit id. * @return the
   * file report matching the params above. * If there is no file report for the given commitId *
   * find the most recent file report before the given commitId
   */
  public static FileReport getFileReport(final String landscapeToken, final String appName, // NOPMD
      final String fqFileName, final String commitId) {

    final FileReportTable fileReportTable = FileReportTable.findByTokenAndAppName(landscapeToken,
        appName);

    if (fileReportTable == null) {
      return null;
    }

    final Map<String, Map<String, String>> table = fileReportTable.getCommitIdTofqnFileNameToCommitIdMap(); // fqnFileName with no non-package prefix
    final Map<String, String> packagesAndFileNameWithFileExtensionToCommitIdMap = table.get(
        commitId); // packagesAndFileNameWithFileExtension is suffix of fqFileName

    if (packagesAndFileNameWithFileExtensionToCommitIdMap == null) {
      return null;
    }

    final Set<String> keySet = packagesAndFileNameWithFileExtensionToCommitIdMap.keySet();

    // find key that is the longest suffix of fqFileName
    // this technicality is not needed when we can be sure that fqFileName begins with a package name and not with
    // a folder structure like src.main.java

    int startsAtIndex = fqFileName.length();
    String actualKey = "";
    for (final String key : keySet) {
      if (fqFileName.endsWith(key)) {
        final int startIndex = fqFileName.lastIndexOf(key);
        if (startsAtIndex > startIndex) {
          startsAtIndex = startIndex;
          actualKey = key;
        }
      }
    }

    final String actualCommitId = packagesAndFileNameWithFileExtensionToCommitIdMap.get(actualKey);

    if (actualCommitId == null) {
      return null;
    }

    final FileReport fileReport = FileReport.findByTokenAndAppNameAndPackageNameAndFileNameAndCommitId(
        landscapeToken, appName, fqFileName, actualCommitId);
    return fileReport;
  }
}
