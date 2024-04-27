package net.explorviz.code.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import net.explorviz.code.beans.LandscapeStructure;
import net.explorviz.code.beans.LandscapeStructure.Node;
import net.explorviz.code.beans.LandscapeStructure.Node.Application;
import net.explorviz.code.beans.LandscapeStructure.Node.Application.Package;
import net.explorviz.code.beans.LandscapeStructure.Node.Application.Package.Class;
import net.explorviz.code.beans.LandscapeStructure.Node.Application.Package.Class.Method;
import net.explorviz.code.helper.CommitComparisonHelper;
import net.explorviz.code.helper.LandscapeStructureHelper;
import net.explorviz.code.mongo.FileReport;

/**
 * ...
 */
@Path("/v2/code/structure/{token}/{appName}")
public class LandscapeStructureResource {

  /**
   * Returns the "deepest" package available matching the package structure. Therefore, the deepest
   * package and the parent package chain covers a prefix of the package structure.
   *
   * @param packageFileName the package structure string
   * @param packages        list of packages to search for a match
   * @return the "deepest" package
   */
  public static Tuple2<Package, String> getPackageFromPath(final String packageFileName, // NOPMD
      final List<Package> packages) {

    final String[] packageFileNameSplit = packageFileName.split("\\.");
    // packageFileName includes file extension
    final int numOfPackages = packageFileNameSplit.length - 2;

    String packagePath = "";

    for (final Package pckg : packages) {
      int counter = 0;
      Package currentPackage = pckg;

      while (packageFileNameSplit[counter].equals(currentPackage.getName())) {

        if ("".equals(packagePath)) {
          packagePath += currentPackage.getName();
        } else {
          packagePath += "." + currentPackage.getName();
        }

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
            return new Tuple2<Package, String>(currentPackage, packagePath);
          }
        } else {
          return new Tuple2<Package, String>(currentPackage, packagePath);
        }

      }

    }
    return null;
  }

  /**
   * ... * @param token the landscape token. * @param appName the application name. * @param commit
   * the commit id. * @return the static landscape structure matching the params above.
   */
  @Path("{commit}")
  @GET
  public LandscapeStructure singleStructure(@PathParam("token") final String token,
      @PathParam("appName") final String appName, final String commit) {
    final List<Package> packages = LandscapeStructureHelper.createListOfPackages(token,
        commit, appName);
    if (packages != null) {
      return this.buildLandscapeStructure(token, appName, packages);
    }
    return new LandscapeStructure();
  }

  /**
   * ... * @param token the landscape token. * @param appName the application name. * @param
   * firstCommit the first selected commit id. * @param secondCommit the second selected commit id.
   * * @return the combination of the static landscape structures for the * first and second
   * selected commit.
   */
  @Path("{firstCommit}-{secondCommit}")
  @GET
  public LandscapeStructure mixedStructure(@PathParam("token") final String token,
      @PathParam("appName") final String appName, final String firstCommit,
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
          getPackageFromPath(packageFileName, packagesFirstSelectedCommit).getFirst();
      final Package packageSecondSelectedCommit =
          getPackageFromPath(packageFileName, packagesSecondSelectedCommit).getFirst();

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
      final int numOfPackages = packageFileNameSplit.length - 2;
      // packageFileName includes file extension
      final String className = packageFileNameSplit[packageFileNameSplit.length - 2];
      //final int numThree = 3;
      //final String lastPackageName = packageFileNameSplit[packageFileNameSplit.length - numThree];

      final Tuple2<Package, String> tuple2 = getPackageFromPath(packageFileName,
          packagesFirstSelectedCommit);

      if (tuple2 != null) { // NOPMD
        final Package packageFirstSelectedCommit = tuple2.getFirst();
        final String prefixPackageFileName = tuple2.getSecond();
        //System.out.println("====>>> " + prefixPackageFileName);
        final String[] prefixPackageFileNameSplit = prefixPackageFileName.split("\\.");
        final int numOfPackagesInPrefix = prefixPackageFileNameSplit.length;

        //final String packageName = packageFirstSelectedCommit.getName();

        if (numOfPackagesInPrefix == numOfPackages) {
          // add class
          final Tuple2<Package, String> tuple2second = getPackageFromPath(packageFileName,
              packagesSecondSelectedCommit);
          if (tuple2second == null) {
            // should never happen. TODO: Error handling
            System.out.println("passiert doch !");
          } else {
            final Package packageSecondSelectedCommit = tuple2second.getFirst();
            final Class clazz = this.getClassByNameFromPackage(className,
                packageSecondSelectedCommit);

            if (clazz != null) { // NOPMD
              //  add class only if not already existent! Indeed they could already exist if the package they are contained in was added before
              boolean isClassContained = false;
              for (final Class clazz2 : packageFirstSelectedCommit.getClasses()) {
                if (clazz2.getName().equals(clazz.getName())) {
                  isClassContained = true;
                  break;
                }
              }
              if (!isClassContained) {
                packageFirstSelectedCommit.getClasses().add(clazz);
              }
            } else { // NOPMD
              // should never happen. TODO: Error Handling
              System.out.println("passiert doch 2!");
            }
          }
        } else {
          // add missing package to existing package
          final String prefixPackageFileName2 = String.join(".",
              Arrays.asList(packageFileNameSplit).subList(0, numOfPackagesInPrefix + 1));
          final Tuple2<Package, String> tuple2second = getPackageFromPath(prefixPackageFileName2 + ".filename.extension",
              packagesSecondSelectedCommit);
          if (tuple2second == null) {
            // should never happen. TODO: Error Handling
            System.out.println("passiert doch 3!");
          } else {
            final Package packageToAdd = tuple2second.getFirst();
            packageFirstSelectedCommit.getSubPackages().add(packageToAdd);
          }
        }
      } else {
        // add first-level package to foundation
        final String firstPackageName = packageFileNameSplit[0];
        final Package firstLevelPackage = getPackageFromPath(
            firstPackageName + ".filename.extension",
            packagesSecondSelectedCommit).getFirst();
        // TODO: Refactor getPackageFromPath so the suffix is not needed
        packagesFirstSelectedCommit.add(firstLevelPackage);
      }
    }
    // ------------------------------------------------------------------------------------

    // // deal with deleted files !!! ACTUALLY NOT NEEDED SINCE WE WANT TO KEEP DELETED CLASSES IN STRUCTURE TO GIVE THEM A "-"-TEXTURE !!!

    // final List<String> deleted = CommitComparisonHelper.getComparisonDeletedFiles(firstCommit, secondCommit, token, appName);
    // final List<String> deletedPackageFileName = new ArrayList<>();

    // for (final String fqFileName : deleted) {
    //   final String fqFileNameDotSeparator = fqFileName.replaceAll("/", ".");
    //   final FileReport fileReport = LandscapeStructureHelper.getFileReport(token, appName,
    //       fqFileNameDotSeparator, secondCommit);
    //   if (fileReport != null) {
    //     deletedPackageFileName.add(fileReport.getPackageName() + "." + fileReport.getFileName());
    //   }
    // }

    // // count the deleted classes within a package and remove the whole package
    // // if the amount of deleted classes is equal to the number of contained classes
    // // AND the package does not contain any non-empty subpackages
    // final Map<String, Integer> packageToNumOfContainedClasses = new HashMap<>();
    // for (final String packageFileName : deletedPackageFileName) {

    //   final String[] packageFileNameSplit = packageFileName.split("\\.");
      
    //   final String key = String.join(".", Arrays.asList(packageFileNameSplit).subList(0, packageFileNameSplit.length - 2));
    //   if (packageToNumOfContainedClasses.containsKey(key)) {
    //     final int numOfDeletedClasses = packageToNumOfContainedClasses.get(key);
    //     packageToNumOfContainedClasses.put(key, numOfDeletedClasses + 1);
    //   } else {
    //     packageToNumOfContainedClasses.put(key, 1);
    //   }
    // }

    // final List<String> packagesWithAllClassesRemoved = new ArrayList<>();
    // for ( final Map.Entry<String, Integer> entry : packageToNumOfContainedClasses.entrySet()) {
    //   final String packageFileName = entry.getKey() + ".filename.extension"; // TODO: Refactor getPackageFromPath so the suffix .filename.extension is not needed
    //   final Package packageFirstSelectedCommit =
    //       getPackageFromPath(packageFileName, packagesFirstSelectedCommit).getFirst();

    //   final int numOfClasses = packageFirstSelectedCommit.getClasses().size();
    //   //final int numOfSubPackages = packageFirstSelectedCommit.getSubPackages().size();
    //   if (numOfClasses == entry.getValue() /*&& numOfSubPackages == 0*/) {
    //     packagesWithAllClassesRemoved.add(entry.getKey());
    //   }
    // }

    // // Sort the full qualified package names so that we begin by removing the "deepest/innermost" removable packages
    // Collections.sort(packagesWithAllClassesRemoved, Collections.reverseOrder());
    // System.out.println("Deleted Files: " + packagesWithAllClassesRemoved);
    // for (final String packageWithAllClassesRemoved : packagesWithAllClassesRemoved) {
    //   final String packageFileName = packageWithAllClassesRemoved + ".filename.extension"; // TODO: Refactor getPackageFromPath so the suffix .filename.extension is not needed
    //   final Package packageFirstSelectedCommit =
    //       getPackageFromPath(packageFileName, packagesFirstSelectedCommit).getFirst();
    //   if (packageFirstSelectedCommit.getSubPackages().size() == 0) {
    //     // remove empty packages
    //     //String deepestNonEmptyPackage = "";
    //     final String[] packageWithAllClassesRemovedSplit = packageWithAllClassesRemoved.split("\\.");
    //     if (packageWithAllClassesRemovedSplit.length == 1) {
    //       // remove top-level package
    //       packagesFirstSelectedCommit.removeIf(pckg -> pckg.getName().equals(packageWithAllClassesRemovedSplit[0]));
    //     } {
    //       // traverse the chain of parent packages to remove all empty packages
    //       for (int i = 1; i < packageWithAllClassesRemovedSplit.length; i++) {
    //         final String currentFullQualifiedPackageName = String.join(".", Arrays.asList(packageWithAllClassesRemovedSplit).subList(0, packageWithAllClassesRemovedSplit.length - i));
    //         final Package currentPackage = getPackageFromPath(currentFullQualifiedPackageName, packagesFirstSelectedCommit).getFirst();
    //         final int index = i;
    //         currentPackage.getSubPackages().removeIf(subPckg -> subPckg.getName().equals(packageWithAllClassesRemovedSplit[packageWithAllClassesRemovedSplit.length - index]));
    //         if (i == packageWithAllClassesRemovedSplit.length - 1 && currentPackage.getSubPackages().size() != 0) {
    //           // Don't forget to remove top-level package
    //           packagesFirstSelectedCommit.removeIf(pckg -> pckg.getName().equals(packageWithAllClassesRemovedSplit[0]));
    //         } else if (currentPackage.getSubPackages().size() != 0) {
    //           break;
    //         }
    //       }
    //       // done
    //     }
    //   }
    // }

    // --------------------------------------------------------------------------------



    System.out.println(" PACKAGES RESULT ");
    for (final Package resPackage : packagesFirstSelectedCommit) {
      System.out.println(resPackage);
      System.out.println("---------------------------------------");
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

    return landscapeStructure;
  }

  /**
   * Helper Class.
   *
   * @param <K> first type.
   * @param <V> second type.
   */
  public static class Tuple2<K, V> {

    private final K first;
    private final V second;

    public Tuple2(K first, V second) {
      this.first = first;
      this.second = second;
    }

    // getters
    public K getFirst() {
      return this.first;
    }

    public V getSecond() {
      return this.second;
    }
  }

}
