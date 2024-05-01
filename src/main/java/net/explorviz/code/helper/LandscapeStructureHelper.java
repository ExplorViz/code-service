package net.explorviz.code.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.explorviz.code.beans.LandscapeStructure.Node.Application.Package;
import net.explorviz.code.beans.LandscapeStructure.Node.Application.Package.Class;
import net.explorviz.code.beans.LandscapeStructure.Node.Application.Package.Class.Method;
import net.explorviz.code.mongo.CommitReport;
import net.explorviz.code.mongo.FileReport;
import net.explorviz.code.mongo.FileReportTable;

public final class LandscapeStructureHelper {

  private LandscapeStructureHelper() {
  }

  public static List<Package> createListOfPackages(
      final String landscapeToken,
      final String commitId, final String appName) {

    final CommitReport commitReport = CommitReport.findByTokenAndApplicationNameAndCommitId(
        landscapeToken, appName, commitId);
    if (commitReport == null) {
      return null;
    }

    final FileReportTable fileReportTable =
        FileReportTable.findByTokenAndAppName(landscapeToken, appName);
    if (fileReportTable == null) {
      return null;
    }

    final List<String> files = commitReport.getFiles();
    final Map<String, Package> packageNameToPackageMap = new HashMap<>();
    final Set<String> firstLevelPackageNames = new HashSet<>();

    files.forEach(file -> processFile(file, landscapeToken, appName, commitId, fileReportTable,
        packageNameToPackageMap, firstLevelPackageNames));

    return firstLevelPackageNames.stream()
        .map(packageNameToPackageMap::get).collect(Collectors.toList());
  }

  private static void processFile(String file, String landscapeToken, String appName,
      String commitId,
      FileReportTable fileReportTable, Map<String, Package> packageNameToPackageMap,
      Set<String> firstLevelPackageNames) {
    String[] fileAndFolders = file.split("/");
    String packageName = derivePackageName(fileAndFolders);
    Package currentPackage = packageNameToPackageMap.computeIfAbsent(packageName, Package::new);

    if (firstLevelPackageNames.isEmpty()) {
      firstLevelPackageNames.add(currentPackage.getName());
    }

    FileReport fileReport =
        getFileReport(fileReportTable, landscapeToken, appName, String.join(".", fileAndFolders),
            commitId);
    if (fileReport != null) {
      updateClassesInPackage(fileReport, currentPackage, landscapeToken, appName);
    }
  }

  private static String derivePackageName(String[] fileAndFolders) {
    return String.join(".",
        fileAndFolders); // Assumes package structure is delineated in the file path.
  }

  private static void updateClassesInPackage(FileReport fileReport, Package packageToUpdate,
      String landscapeToken, String appName) {
    fileReport.getClassData().forEach((className, classData) -> {
      Class clazz = new Class();
      clazz.setName(className.substring(className.lastIndexOf('.') + 1));
      clazz.setSuperClass(classData.getSuperClass());

      classData.getMethodData().forEach((methodName, methodData) -> {
        Method method = new Method();
        method.setName(methodName);

        UUID landscapeTokenValue = UUID.fromString("7cd8a9a7-b840-4735-9ef0-2dbbfa01c039");
        if (!"mytokenvalue".equals(landscapeToken)) {
          landscapeTokenValue = UUID.fromString(landscapeToken);
        }

        String methodFqn =
            methodName + className;  // Combining method and class name for a unique hash.
        String methodHash =
            HashHelper.calculateSpanHash(landscapeTokenValue, "0.0.0.0", appName, 0, methodFqn);
        method.setMethodHash(methodHash);

        clazz.getMethods().add(method);
      });

      packageToUpdate.getClasses().add(clazz);
    });
  }

  public static FileReport getFileReport(final String landscapeToken, final String appName,
      final String fqFileName, final String commitId) {

    final FileReportTable fileReportTable =
        FileReportTable.findByTokenAndAppName(landscapeToken, appName);
    if (fileReportTable == null) {
      return null;
    }

    return getFileReport(fileReportTable, landscapeToken, appName, fqFileName, commitId);
  }


  private static FileReport getFileReport(FileReportTable fileReportTable,
      final String landscapeToken, final String appName,
      final String fqFileName, final String commitId) {
    final Map<String, String> packagesAndFileNameWithFileExtensionToCommitIdMap = fileReportTable
        .getCommitIdTofqnFileNameToCommitIdMap().get(commitId);
    if (packagesAndFileNameWithFileExtensionToCommitIdMap == null) {
      return null;
    }

    return findMatchingFileReport(fqFileName, packagesAndFileNameWithFileExtensionToCommitIdMap,
        landscapeToken, appName);
  }

  private static FileReport findMatchingFileReport(String fqFileName,
      Map<String, String> commitIdMap, String landscapeToken, String appName) {
    for (Map.Entry<String, String> entry : commitIdMap.entrySet()) {
      if (fqFileName.endsWith(entry.getKey())) {
        return FileReport.findByTokenAndAppNameAndPackageNameAndFileNameAndCommitId(
            landscapeToken, appName, fqFileName, entry.getValue());
      }
    }
    return null;
  }
}
