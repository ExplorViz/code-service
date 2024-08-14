package net.explorviz.code.helper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.explorviz.code.dto.LandscapeStructure.Node.Application.Package;
import net.explorviz.code.dto.LandscapeStructure.Node.Application.Package.Class;
import net.explorviz.code.dto.LandscapeStructure.Node.Application.Package.Class.Method;
import net.explorviz.code.persistence.entity.CommitReport;
import net.explorviz.code.persistence.entity.FileReport;
import net.explorviz.code.persistence.entity.FileReport.ClassData2;
import net.explorviz.code.persistence.entity.FileReport.ClassData2.MethodData2;
import net.explorviz.code.persistence.entity.FileReportTable;
import net.explorviz.code.persistence.repository.CommitReportRepository;
import net.explorviz.code.persistence.repository.FileReportRepository;
import net.explorviz.code.persistence.repository.FileReportTableRepository;

@ApplicationScoped
public class LandscapeStructureHelper {

  private final Map<String, FileReportTable> fileReportTableMap =
      new ConcurrentHashMap<>();

  private final CommitReportRepository commitReportRepository;
  private final FileReportRepository fileReportRepository;
  private final FileReportTableRepository fileReportTableRepository;

  @Inject
  public LandscapeStructureHelper(final CommitReportRepository commitReportRepository,
      final FileReportRepository fileReportRepository,
      final FileReportTableRepository fileReportTableRepository) {
    this.commitReportRepository = commitReportRepository;
    this.fileReportRepository = fileReportRepository;
    this.fileReportTableRepository = fileReportTableRepository;
  }

  /**
   * ... * @param landscapeToken the landscape token. * @param commitId the commit id. * @param
   * appName the application name. * @return the list of "first-order" packages of an application
   * matching above params.
   */
  public List<Package> createListOfPackages(// NOPMD
      final String landscapeToken,
      final String commitId, final String appName) {

    final CommitReport commitReport =
        this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(
            landscapeToken, appName, commitId);
    if (commitReport == null) {
      return null;
    }
    final List<String> files = commitReport.files();

    final Map<String, Package> packageNameToPackageMap =
        new HashMap<>();
    final Map<String, Class> fqnClassNameToClass =
        new HashMap<>();
    final Set<String> firstLevelPackageNames = new HashSet<>();
    final Set<String> functionFqn = new HashSet<>();

    List<FileReport> fileReports =
        this.getFileReports(landscapeToken, appName, commitId, files);

    for (final FileReport fileReport : fileReports) {
      if (fileReport == null) {
        continue;
      }

      final String fileNameWithoutFileExtension = fileReport.getFileName().split("\\.")[0];

      this.processFileReport(landscapeToken, fileNameWithoutFileExtension,
          appName, fileReport, packageNameToPackageMap, fqnClassNameToClass, firstLevelPackageNames,
          functionFqn);
    }

    final List<Package> packageList = new ArrayList<>();
    firstLevelPackageNames.forEach(name -> {
      packageList.add(packageNameToPackageMap.get(name));
    });
    return packageList;
  }

  /**
   * Get list of filereports using batch queries.
   *
   * @param landscapeToken encompassing token
   * @param appName        encompassing app name
   * @param commitId       top-level commit
   * @param fileNames      list of fqns
   * @return list of filereports
   */
  public List<FileReport> getFileReports(String landscapeToken, String appName,
      String commitId, List<String> fileNames) {
    Map<String, List<String>> actualCommitIdToFqnMap = new HashMap<>();
    for (final String file : fileNames) {
      final String[] fileAndFolders = file.split("/");
      final String fileAndFoldersWithDotSeparation = String.join(".", fileAndFolders);

      final String actualCommit =
          this.getActualCommitIdForFqnAndTargetCommit(landscapeToken, appName,
              fileAndFoldersWithDotSeparation, commitId);

      if (actualCommitIdToFqnMap.containsKey(actualCommit)) {
        actualCommitIdToFqnMap.get(actualCommit).add(fileAndFoldersWithDotSeparation);
      } else {
        actualCommitIdToFqnMap.put(actualCommit,
            new ArrayList<>(List.of(fileAndFoldersWithDotSeparation)));
      }
    }
    return this.fileReportRepository.getFileReports(landscapeToken, appName,
        actualCommitIdToFqnMap);
  }


  private void processFileReport(String landscapeToken, String fileNameWithoutFileExtension,
      String appName, FileReport fileReport, Map<String, Package> packageNameToPackageMap,
      Map<String, Class> fqnClassNameToClass, Set<String> firstLevelPackageNames,
      Set<String> functionFqn) {

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
        id.append(".").append(packages[j + 1]);
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

      final Package currentPackageFinal =
          currentPackage; // needed for next code line so there is no compile time error
      if (parentPackage != null && parentPackage.getSubPackages().stream()
          .filter(subPckg -> subPckg.getName().equals(currentPackageFinal.getName()))
          .toList().isEmpty()) {
        parentPackage.getSubPackages().add(currentPackage);
        // System.out.println("A D D " + parentPackage.getName() + " ====> "
        // + currentPackage.getName());
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
        (classData = fileReport.getClassData().get(id)) == null) {
      // TODO: walk through classData entry set instead
      // because a class file could theoretically (but shouldn't practically)
      // have multiple first level classes defined
      return;
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
          methodFqn.append(name).append(".");
        }
        methodFqn.append(methodName);
        //System.out.println("method FQN: " + methodFqn);

        UUID landscapeTokenValue = UUID.fromString("7cd8a9a7-b840-4735-9ef0-2dbbfa01c039");

        if (!"mytokenvalue".equals(landscapeToken)) {
          landscapeTokenValue = UUID.fromString(landscapeToken);
        }

        // functionFqn really needed? Only if we want to "prevent" overloaded functions
        if (!functionFqn.contains(methodFqn.toString())) {
          // entry.getKey() instead of methodFqn? Otherwise we might miss overloaded functions
          final Method method = new Method(); // NOPMD
          method.setName(methodName); // include parameter list due to overloaded functions?
          final String methodHash = HashHelper.calculateSpanHash(landscapeTokenValue,
              "0.0.0.0", appName, 0, methodFqn.toString());
          method.setMethodHash(methodHash);
          clazz.getMethods().add(method);
          functionFqn.add(methodFqn.toString());
        }
      }

    }

    if (currentPackage != null) {
      currentPackage.getClasses().add(clazz);
    }
  }

  /**
   * Return the actual commitId for a fqn that was in the repo at the time of the target commitId.
   *
   * @param landscapeToken encompassing token
   * @param appName        encompassing appName
   * @param fqFileName     target fqn
   * @param targetCommitId target commit id, e.g., the one that is initially requested by the
   *                       frontend, but in reality the target fqn initially occured in a different
   *                       commit
   * @return the actual commit id for the target fqn and commitId
   */
  public String getActualCommitIdForFqnAndTargetCommit(final String landscapeToken,
      final String appName,
      final String fqFileName, final String targetCommitId) {

    // Use cache key as combination of landscapeToken and appName
    String cacheKey = landscapeToken + ":" + appName;
    FileReportTable fileReportTable = fileReportTableMap.get(cacheKey);

    // Only fetch from DB if not present in cache
    if (fileReportTable == null) {
      fileReportTable =
          this.fileReportTableRepository.findByTokenAndAppName(landscapeToken, appName);
      if (fileReportTable != null) {
        fileReportTableMap.put(cacheKey, fileReportTable);
      }
    }

    if (fileReportTable == null) {
      return null;
    }

    final Map<String, Map<String, String>> table = fileReportTable
        .getCommitIdTofqnFileNameToCommitIdMap(); // fqnFileName with no non-package prefix
    final Map<String, String> packagesAndFileNameWithFileExtensionToCommitIdMap = table
        .get(targetCommitId); // packagesAndFileNameWithFileExtension is suffix of fqFileName

    if (packagesAndFileNameWithFileExtensionToCommitIdMap == null) {
      return null;
    }

    final Set<String> keySet = packagesAndFileNameWithFileExtensionToCommitIdMap.keySet();

    // find key that is the longest suffix of fqFileName
    // this technicality is not needed when we can be sure that fqFileName begins
    // with a package name and not with
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

    return packagesAndFileNameWithFileExtensionToCommitIdMap.get(actualKey);
  }

  /**
   * ...
   *
   * @param landscapeToken the landscape token.
   * @param appName        the application name.
   * @param fqFileName     the full qualified file name.
   * @param commitId       the commit id
   * @return the file report matching the params above or most recent report before given commitid
   */
  public FileReport getFileReport(final String landscapeToken, final String appName, // NOPMD
      final String fqFileName, final String commitId) {

    final String actualCommitId =
        this.getActualCommitIdForFqnAndTargetCommit(landscapeToken, appName,
            fqFileName, commitId);

    if (actualCommitId == null) {
      return null;
    }

    return this.fileReportRepository.findByTokenAndAppNameAndPackageNameAndFileNameAndCommitId(
        landscapeToken, appName, fqFileName, actualCommitId);
  }
}
