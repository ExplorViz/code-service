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
  public LandscapeStructure singleStructure(@RestPath String token, 
      @RestPath String appName, String commit) {
  
    List<Package> packages = LandscapeStructureHelper.createListOfPackages(commit);
    return this.buildLandscapeStructure(token, appName, packages);
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
  public LandscapeStructure mixedStructure(@RestPath String token, 
      @RestPath String appName, String firstCommit, String secondCommit) {
      
    List<Package> packagesFirstSelectedCommit = LandscapeStructureHelper
        .createListOfPackages(firstCommit);
    List<Package> packagesSecondSelectedCommit = LandscapeStructureHelper
        .createListOfPackages(secondCommit);

    // deal with modified files --------------------------------------------------------------------
    List<String> modified = CommitComparisonHelper.getComparisonModifiedFiles(
        firstCommit, 
        secondCommit, token);

    List<String> modifiedPackageFileName = new ArrayList<>();
    
    for (final String fqFileName : modified) {
      String fqFileNameDotSeparator = fqFileName.replaceAll("/", ".");
      FileReport fileReport = LandscapeStructureHelper.getFileReport(token, appName, 
          fqFileNameDotSeparator, secondCommit);
      if (fileReport != null) {
        modifiedPackageFileName.add(fileReport.packageName + "." + fileReport.fileName);
      }
    }

    for (final String packageFileName : modifiedPackageFileName) {
      final String[] packageFileNameSplit = packageFileName.split("\\.");
      final String className = packageFileNameSplit[packageFileNameSplit.length - 2];
      final Package packageFirstSelectedCommit = 
          this.getPackageFromPath(packageFileName, packagesFirstSelectedCommit);
      final Package packageSecondSelectedCommit = 
          this.getPackageFromPath(packageFileName, packagesSecondSelectedCommit);

 
      // packageFileName includes file extension
      final String lastPackageName = packageFileNameSplit[packageFileNameSplit.length - 3];
      if (packageFirstSelectedCommit.name.equals(lastPackageName) 
          && 
          packageSecondSelectedCommit.name.equals(lastPackageName)) {
        // add missing methods
        
        final Class clazzSecondSelectedCommit = this.getClassByNameFromPackage(
            className, packageSecondSelectedCommit);
        final Class clazzFirstSelectedCommit = this.getClassByNameFromPackage(
            className, packageFirstSelectedCommit);

        if (clazzSecondSelectedCommit != null & clazzFirstSelectedCommit != null) {
          for (final Method method : clazzSecondSelectedCommit.methods) {
            if (clazzFirstSelectedCommit.methods.stream()
                .filter(m -> m.name.equals(method.name)).collect(Collectors.toList()).size() == 0) {
              clazzFirstSelectedCommit.methods.add(method);
            }
          }
        } else {
          // should not happen
        }
      } 
    }
    // --------------------------------------------------------------------------------------------

    // deal with added files ----------------------------------------------------------------------
    List<String> added = CommitComparisonHelper.getComparisonAddedFiles(
        firstCommit, 
        secondCommit, token);

    List<String> addedPackageFileName = new ArrayList<>();
    
    for (final String fqFileName : added) {
      String fqFileNameDotSeparator = fqFileName.replaceAll("/", ".");
      FileReport fileReport = LandscapeStructureHelper.getFileReport(token, appName, 
          fqFileNameDotSeparator, secondCommit);
      if (fileReport != null) {
        addedPackageFileName.add(fileReport.packageName + "." + fileReport.fileName);
      }
    }

    for (final String packageFileName : addedPackageFileName) {
      final String[] packageFileNameSplit = packageFileName.split("\\.");
      // packageFileName includes file extension
      final String className = packageFileNameSplit[packageFileNameSplit.length - 2];
      final String lastPackageName = packageFileNameSplit[packageFileNameSplit.length - 3];
      final Package packageFirstSelectedCommit = 
          this.getPackageFromPath(packageFileName, packagesFirstSelectedCommit);
      final Package packageSecondSelectedCommit = 
          this.getPackageFromPath(packageFileName, packagesSecondSelectedCommit);

      if (packageSecondSelectedCommit != null) {

        if (packageFirstSelectedCommit != null) {
          final String packageName = packageFirstSelectedCommit.name;

          if (packageName.equals(lastPackageName)) {
            // add class
            final Class clazz = this.getClassByNameFromPackage(className, 
                packageSecondSelectedCommit);
            
            if (clazz != null) {
              packageFirstSelectedCommit.classes.add(clazz);
            } else {
              // should never happen
            }
          } else {
            // add package
            String subPackages = "";
            for (int i = 0; i < packageFileNameSplit.length - 3; i++) {
              subPackages += packageFileNameSplit[i] + ".";
              if (packageFileNameSplit[i].equals(packageName)) {
                subPackages += packageFileNameSplit[i + 1];
                break;
              }
            }
            // file name needed for technical reason. We imitate one
            final String subPackageFileName = subPackages + "." + "filename" + "." + "extension";
            final Package pckg = this.getPackageFromPath(subPackageFileName, 
                packagesSecondSelectedCommit);
            packageFirstSelectedCommit.subPackages.add(pckg);
          }
        } else {
          packagesFirstSelectedCommit.add(packageSecondSelectedCommit);
        }
      } else {
        // should never happen
      }
    }
    return this.buildLandscapeStructure(token, appName, packagesFirstSelectedCommit);
  }

  private Class getClassByNameFromPackage(final String className, Package pckg) {
    for (final Class clazz : pckg.classes) {
      if (clazz.name.equals(className)) {
        return clazz;
      }
    }
    return null;
  }

  // returns the "deepest" package available matching the package structure
  private Package getPackageFromPath(final String packageFileName, List<Package> packages) {
    final String[] packageFileNameSplit = packageFileName.split("\\.");
    // packageFileName includes file extension
    final int numOfPackages = packageFileNameSplit.length - 2;


    for (final Package pckg : packages) {
      int counter = 0;
      Package currentPackage = pckg;

      while (packageFileNameSplit[counter].equals(currentPackage.name)) {

        if (numOfPackages > counter + 1) {

          Package temp = null;
          for (final Package subPackage : currentPackage.subPackages) {
            if (subPackage.name.equals(packageFileNameSplit[counter + 1])) {
              temp = subPackage;
              break;
            }
          }
          if (temp != null) {
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

  private LandscapeStructure buildLandscapeStructure(String landscapeToken, String appName, 
      List<Package> packages) {
    Node node = new Node();
    node.ipAdress = "0.0.0.0";
    node.hostName = "default-node";
    node.applications = new ArrayList<>();
    
    Application application = new Application();
    application.name = appName;
    application.language = "Java";
    application.instanceId = "0";
    application.packages = packages;
    node.applications.add(application);

    LandscapeStructure landscapeStructure = new LandscapeStructure();
    landscapeStructure.landscapeToken = landscapeToken;
    landscapeStructure.nodes = new ArrayList<>();
    landscapeStructure.nodes.add(node);

    return  landscapeStructure;
  }
    
}
