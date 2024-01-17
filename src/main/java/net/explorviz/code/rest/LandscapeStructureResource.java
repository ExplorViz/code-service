package net.explorviz.code.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import net.explorviz.code.beans.LandscapeStructure;
import net.explorviz.code.beans.LandscapeStructure.Node;
import net.explorviz.code.beans.LandscapeStructure.Node.Application;
import net.explorviz.code.beans.LandscapeStructure.Node.Application.Package;
import net.explorviz.code.beans.LandscapeStructure.Node.Application.Package.Class;
import net.explorviz.code.beans.LandscapeStructure.Node.Application.Package.Class.Method;
import net.explorviz.code.helper.CommitComparisonHelper;
import net.explorviz.code.helper.LandscapeStructureHelper;
import net.explorviz.code.mongo.FileReport;
import org.jboss.resteasy.reactive.RestPath;

/**
 * ...
 */
@Path("/structure/{token}/{appName}")
public class LandscapeStructureResource {

  /**
   * ...
   ** @param token ...
   ** @param appName ...
   ** @param commit ...
   ** @return ...
   */
  @Path("{commit}")
  @GET
  public LandscapeStructure singleStructure(@RestPath final String token, 
      @RestPath final String appName, final String commit) {
    final List<Package> packages = LandscapeStructureHelper.createListOfPackages(token, 
        commit, appName);
    if (packages != null) {
      return this.buildLandscapeStructure(token, appName, packages);
    } 
    return new LandscapeStructure();
  }

  /**
   * ...
   ** @param token ...
   ** @param appName ...
   ** @param firstCommit ...
   ** @param secondCommit ...
   ** @return ...
   */
  @Path("{firstCommit}-{secondCommit}")
  @GET
  public LandscapeStructure mixedStructure(@RestPath final String token, // NOPMD
      @RestPath final String appName, final String firstCommit, 
      final String secondCommit) {
      
    final List<Package> packagesFirstSelectedCommit = LandscapeStructureHelper
        .createListOfPackages(token, firstCommit, appName);
    final List<Package> packagesSecondSelectedCommit = LandscapeStructureHelper
        .createListOfPackages(token, secondCommit, appName);

    if (packagesFirstSelectedCommit == null || packagesSecondSelectedCommit == null) {
      return new LandscapeStructure();
    }

    // deal with modified files --------------------------------------------------------------------
    final List<String> modified = CommitComparisonHelper.getComparisonModifiedFiles(
        firstCommit, 
        secondCommit, token, appName);

    final List<String> modifiedPackageFileName = new ArrayList<>();
    
    for (final String fqFileName : modified) {
      final String fqFileNameDotSeparator = fqFileName.replaceAll("/", ".");
      final FileReport fileReport = LandscapeStructureHelper.getFileReport(token, appName, 
          fqFileNameDotSeparator, secondCommit);
      if (fileReport != null) {
        modifiedPackageFileName.add(fileReport.getPackageName() + "." + fileReport.getFileName());
      }
    }

    for (final String packageFileName : modifiedPackageFileName) {
      final String[] packageFileNameSplit = packageFileName.split("\\.");
      final String className = packageFileNameSplit[packageFileNameSplit.length - 2];
      final Package packageFirstSelectedCommit = 
          getPackageFromPath(packageFileName, packagesFirstSelectedCommit);
      final Package packageSecondSelectedCommit = 
          getPackageFromPath(packageFileName, packagesSecondSelectedCommit);

 
      // packageFileName includes file extension
      final int numThree = 3;
      final String lastPackageName = packageFileNameSplit[packageFileNameSplit.length - numThree];
      if (packageFirstSelectedCommit.getName().equals(lastPackageName) 
          && 
          packageSecondSelectedCommit.getName().equals(lastPackageName)) {
        // add missing methods
        
        final Class clazzSecondSelectedCommit = this.getClassByNameFromPackage(
            className, packageSecondSelectedCommit);
        final Class clazzFirstSelectedCommit = this.getClassByNameFromPackage(
            className, packageFirstSelectedCommit);

        if (clazzSecondSelectedCommit != null & clazzFirstSelectedCommit != null) {
          for (final Method method : clazzSecondSelectedCommit.getMethods()) {
            if (clazzFirstSelectedCommit.getMethods().stream()
                .filter(m -> m.getName().equals(method.getName())).collect(Collectors.toList())
                .size() == 0) {
              clazzFirstSelectedCommit.getMethods().add(method);
            }
          }
        } else { // NOPMD
          // should not happen. Log error?
        }
      } 
    }
    // --------------------------------------------------------------------------------------------

    // deal with added files ----------------------------------------------------------------------
    final List<String> added = CommitComparisonHelper.getComparisonAddedFiles(
        firstCommit, 
        secondCommit, token, appName);

    final List<String> addedPackageFileName = new ArrayList<>();
    
    for (final String fqFileName : added) {
      final String fqFileNameDotSeparator = fqFileName.replaceAll("/", ".");
      final FileReport fileReport = LandscapeStructureHelper.getFileReport(token, appName, 
          fqFileNameDotSeparator, secondCommit);
      if (fileReport != null) {
        addedPackageFileName.add(fileReport.getPackageName() + "." + fileReport.getFileName());
      }
    }

    for (final String packageFileName : addedPackageFileName) {
      final String[] packageFileNameSplit = packageFileName.split("\\.");
      // packageFileName includes file extension
      final String className = packageFileNameSplit[packageFileNameSplit.length - 2];
      final int numThree = 3;
      final String lastPackageName = packageFileNameSplit[packageFileNameSplit.length - numThree];
      final Package packageFirstSelectedCommit = 
          getPackageFromPath(packageFileName, packagesFirstSelectedCommit);
      final Package packageSecondSelectedCommit = 
          getPackageFromPath(packageFileName, packagesSecondSelectedCommit);

      if (packageSecondSelectedCommit != null) { // NOPMD

        if (packageFirstSelectedCommit != null) { // NOPMD
          final String packageName = packageFirstSelectedCommit.getName();

          if (packageName.equals(lastPackageName)) {
            // add class
            final Class clazz = this.getClassByNameFromPackage(className, 
                packageSecondSelectedCommit);
            
            if (clazz != null) { // NOPMD
              packageFirstSelectedCommit.getClasses().add(clazz);
            } else { // NOPMD
              // should never happen. TODO: Log Error
            }
          } else {
            // add package
            final StringBuilder subPackages = new StringBuilder(""); // NOPMD
            for (int i = 0; i < packageFileNameSplit.length - numThree; i++) {
              subPackages.append(packageFileNameSplit[i] + ".");
              if (packageFileNameSplit[i].equals(packageName)) {
                subPackages.append(packageFileNameSplit[i + 1]);

                break;
              }
            }
            // TODO: subPackages.toString()
            // file name needed for technical reason. We imitate one
            final String subPackageFileName = subPackages.toString() + "." + "filename" + "."
                 + "extension";
            final Package pckg = getPackageFromPath(subPackageFileName, 
                packagesSecondSelectedCommit);
            packageFirstSelectedCommit.getSubPackages().add(pckg);
          }
        } else {
          packagesFirstSelectedCommit.add(packageSecondSelectedCommit);
        }
      } else { // NOPMD
        // should never happen. TODO: LOG error
      }
    }
    return this.buildLandscapeStructure(token, appName, packagesFirstSelectedCommit);
  }

  private Class getClassByNameFromPackage(final String className, final Package pckg) {
    for (final Class clazz : pckg.getClasses()) {
      if (clazz.getName().equals(className)) {
        return clazz;
      }
    }
    return null;
  }

 
  /**
   * Returns the "deepest" package available matching the package structure. Therefore, the 
   * deepest package and the parent package chain covers a prefix of the package structure.
   ** @param packageFileName the package structure string
   ** @param packages list of packages to search for a match
   ** @return the "deepest" package
   */
  public static Package getPackageFromPath(final String packageFileName, // NOPMD
      final List<Package> packages) {
    final String[] packageFileNameSplit = packageFileName.split("\\.");
    // packageFileName includes file extension
    final int numOfPackages = packageFileNameSplit.length - 2;


    for (final Package pckg : packages) {
      int counter = 0;
      Package currentPackage = pckg;

      while (packageFileNameSplit[counter].equals(currentPackage.getName())) {

        if (numOfPackages > counter + 1) {

          Package temp = null;
          for (final Package subPackage : currentPackage.getSubPackages()) {
            if (subPackage.getName().equals(packageFileNameSplit[counter + 1])) {
              temp = subPackage;
              break;
            }
          }
          if (temp != null) { // NOPMD
            currentPackage = temp;
            counter++;
          } else {
            return currentPackage;
          }
        } else {
          return currentPackage;
        }

      }

    }
    return null;
  }

  private LandscapeStructure buildLandscapeStructure(final String landscapeToken, 
      final String appName, final List<Package> packages) {
    final Node node = new Node();
    node.setIpAdress("0.0.0.0"); // NOPMD
    node.setHostName("default-node");
    node.setApplications(new ArrayList<>());
    
    final Application application = new Application();
    application.setName(appName);
    application.setLanguage("java");
    application.setInstanceId("0");
    application.setPackages(packages);
    node.getApplications().add(application);

    final LandscapeStructure landscapeStructure = new LandscapeStructure();
    landscapeStructure.setLandscapeToken(landscapeToken);
    landscapeStructure.setNodes(new ArrayList<>());
    landscapeStructure.getNodes().add(node);

    return  landscapeStructure;
  }
    
}
