package net.explorviz.code.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.explorviz.code.beans.LandscapeStructure;
import net.explorviz.code.mongo.CommitReport;

/**
 * ...
 */
public class LandscapeStructureHelper {
    
  /**
   * ...
   ** @param commitId
   ** @return ...
   */
  public static List<Package> createListOfPackages(final String commitId 
  /*,final String appName*/) { // TODO: app name

    CommitReport commitReport = CommitReport.findByCommitId(commitId); // TODO: and AppName
    List<String> files = commitReport.getFiles();


    final Map<String, Package> packageNameToPackageMap = new HashMap<>();
    final Map<String, LandscapeStructure.Node.Application.Package.Class> fqnClassNameToClass =
         new HashMap<>();
    final Set<String> firstLevelPackageNames = new HashSet<>();
    final Set<String> functionFqn = new HashSet<>();


    for (final String file : files) {
      final String[] fileAndFolders = file.split("/");
      final String fileName = fileAndFolders[fileAndFolders.length - 1];
      final String fileNameWithoutFileExtension = fileName.split(".")[0];


    }


    return null;
  }
}
