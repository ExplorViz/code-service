package net.explorviz.code.kafka;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.explorviz.code.proto.FileMetricData;
import net.explorviz.code.proto.ClassData;
import net.explorviz.code.proto.ClassType;
import net.explorviz.code.proto.FieldData;
import net.explorviz.code.proto.ParameterData;
import net.explorviz.code.proto.MethodData;
import javax.enterprise.context.ApplicationScoped;
// import javax.inject.Inject;
import net.explorviz.code.grpc.FileDataServiceImpl;
import net.explorviz.code.mongo.BranchPoint;
import net.explorviz.code.mongo.CommitReport;
import net.explorviz.code.mongo.FileReport;
import net.explorviz.code.mongo.FileReport.ClassData2;
import net.explorviz.code.mongo.FileReport.ClassData2.ClassType2;
import net.explorviz.code.mongo.FileReport.ClassData2.FieldData2;
import net.explorviz.code.mongo.FileReport.ClassData2.MethodData2;
import net.explorviz.code.mongo.FileReport.ClassData2.MethodData2.ParameterData2;
import net.explorviz.code.mongo.LatestCommit;
import net.explorviz.code.mongo.CommitReport.FileMetric;
import net.explorviz.code.proto.CommitReportData;
import net.explorviz.code.proto.FileData;
import net.explorviz.code.proto.StateDataRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class functions as a gateway for the analysis data into kafka or another storage. It gets
 * called by the respective GRPC endpoints. The first time analyzation from the code-agent should 
 * always start with the main/master branch
 */
@ApplicationScoped
public class GrpcGateway {

  private static final Logger LOGGER = LoggerFactory.getLogger(GrpcGateway.class);

  /**
   * Processes a CommitReportData package. Stores the data into the local storage.
   *
   * @param commitReportData the CommitReportData to handle
   */
  public void processCommitReport(final CommitReportData commitReportData) {

    
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Received Commit report: {}", commitReportData);
    }

    final String receivedCommitReportCommitId = commitReportData.getCommitID();
    final String receivedCommitReportAncestorId = commitReportData.getParentCommitID();
    final String receivedCommitReportBranchName = commitReportData.getBranchName();
    final List<String> receivedCommitReportFiles = commitReportData.getFilesList();
    final List<String> receivedCommitReportModified = commitReportData.getModifiedList();
    final List<String> receivedCommitReportDeleted = commitReportData.getDeletedList();
    final List<String> receivedCommitReportAdded = commitReportData.getAddedList();
    final List<FileMetricData> receivedCommitReportFileMetricData = commitReportData
        .getFileMetricList();
    final List<FileMetric> receivedCommitReportFileMetric = new ArrayList<>();
    final String receivedCommitReportLandscapeToken = commitReportData.getLandscapeToken();
    final List<String> receivedCommitReportFileHash = commitReportData.getFileHashList();
    final String receivedCommitReportApplicationName = commitReportData.getApplicationName();

    for (final FileMetricData fileMetricData : receivedCommitReportFileMetricData) {
      CommitReport.FileMetric fileMetric = new CommitReport.FileMetric(); 
      fileMetric.fileName = fileMetricData.getFileName();
      fileMetric.loc = fileMetricData.getLoc();
      fileMetric.cyclomaticComplexity = fileMetricData.getCyclomaticComplexity();
      receivedCommitReportFileMetric.add(fileMetric);
    }

    final CommitReport oldReport = CommitReport.findByTokenAndApplicationNameAndCommitId(
        receivedCommitReportLandscapeToken, receivedCommitReportApplicationName, 
        receivedCommitReportCommitId);

    if (oldReport != null) {
      return;
    }

    CommitReport commitReport = new CommitReport();
    commitReport.commitId = receivedCommitReportCommitId;
    commitReport.parentCommitId = receivedCommitReportAncestorId;
    commitReport.branchName = receivedCommitReportBranchName;
    commitReport.files = receivedCommitReportFiles;
    commitReport.modified = receivedCommitReportModified;
    commitReport.deleted = receivedCommitReportDeleted;
    commitReport.added = receivedCommitReportAdded;
    commitReport.fileMetric = receivedCommitReportFileMetric;
    commitReport.landscapeToken = receivedCommitReportLandscapeToken;
    commitReport.fileHash = receivedCommitReportFileHash;
    commitReport.applicationName = receivedCommitReportApplicationName;


    if (!receivedCommitReportAncestorId.equals("NONE")) {
      if (CommitReport.findByTokenAndApplicationNameAndCommitId(receivedCommitReportLandscapeToken, 
          receivedCommitReportApplicationName, receivedCommitReportAncestorId) != null) {
        // no missing reports
        commitReport.persist();
        LatestCommit latestCommit = LatestCommit
            .findByBranchNameAndLandscapeToken(receivedCommitReportBranchName, 
                                               receivedCommitReportLandscapeToken);
        if (latestCommit == null) {
          // commit of a new branch
          latestCommit = new LatestCommit();
          latestCommit.branchName = receivedCommitReportBranchName;
          latestCommit.commitId = receivedCommitReportCommitId;
          latestCommit.landscapeToken = receivedCommitReportLandscapeToken;
          latestCommit.persist();

          BranchPoint branchPoint = new BranchPoint();
          branchPoint.branchName = receivedCommitReportBranchName;
          branchPoint.commitId = receivedCommitReportCommitId;
          branchPoint.landscapeToken = receivedCommitReportLandscapeToken;
          branchPoint.applicationName = receivedCommitReportApplicationName;
          CommitReport ancestorCommitReport = CommitReport
               .findByTokenAndApplicationNameAndCommitId(receivedCommitReportLandscapeToken, 
               receivedCommitReportApplicationName, receivedCommitReportAncestorId);
          branchPoint.emergedFromCommitId = ancestorCommitReport.commitId;
          branchPoint.emergedFromBranchName = ancestorCommitReport.branchName; 
          branchPoint.persist();
        } else {
          latestCommit.commitId = receivedCommitReportCommitId;
          latestCommit.update();
        }
      } else {
        // missing reports. Do nothing until we get the missing reports. Analyzer has to rerun
      }
    } else {
      // first commit ever
      commitReport.persist();
      LatestCommit latestCommit = new LatestCommit();
      latestCommit.branchName = receivedCommitReportBranchName;
      latestCommit.commitId = receivedCommitReportCommitId;
      latestCommit.landscapeToken = receivedCommitReportLandscapeToken;
      latestCommit.persist();
      BranchPoint branchPoint = new BranchPoint();
      branchPoint.branchName = receivedCommitReportBranchName;
      branchPoint.commitId = receivedCommitReportCommitId;
      branchPoint.landscapeToken = receivedCommitReportLandscapeToken;
      branchPoint.applicationName = receivedCommitReportApplicationName;
      branchPoint.emergedFromBranchName = "NONE";
      branchPoint.emergedFromCommitId = "";
      branchPoint.persist();
    }
  }

  /**
   * Processes a FileData package. Stores the data into the local storage.
   *
   * @param fileData the FileData to handle
   */
  public void processFileData(final FileData fileData) {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Received file data: {}", fileData);

      final String receivedFileDataLandscapeToken = fileData.getLandscapeToken();
      final String receivedFileDataAppName = fileData.getApplicationName();
      final String receivedFileDataCommitId = fileData.getCommitID();
      final String receivedFileDataFileName = fileData.getFileName();
      final String receivedFileDataPackageName = fileData.getPackageName();
      final List<String> receivedFileDataImportName = fileData.getImportNameList();
      final Map<String, ClassData> receivedFileDataClassData = fileData.getClassDataMap();
      final Map<String, String> receivedFileDataMetric = fileData.getMetricMap();
      final String receivedFileDataAuthor = fileData.getAuthor();
      final String receivedFileDataModifiedLines = fileData.getModifiedLines();
      final String receivedFileDataAddedLines = fileData.getAddedLines();
      final String receivedFileDataDeletedLines = fileData.getDeletedLines();
      
      FileReport fileReport = new FileReport();

      fileReport.landscapeToken = receivedFileDataLandscapeToken;
      fileReport.appName = receivedFileDataAppName;
      fileReport.commitId = receivedFileDataCommitId;
      fileReport.fileName = receivedFileDataFileName;
      fileReport.packageName = receivedFileDataPackageName;
      fileReport.importName = receivedFileDataImportName;


      Map<String, ClassData2> classData = new HashMap<>();
      for (Map.Entry<String, ClassData> entry 
          : receivedFileDataClassData.entrySet()) {
        final ClassData2 cd = new ClassData2();

        switch (entry.getValue().getType()) {
          case INTERFACE:
            cd.type = ClassType2.INTERFACE;
            break;
          case ABSTRACT_CLASS:
            cd.type = ClassType2.ABSTRACT_CLASS;
            break;
          case CLASS:
            cd.type = ClassType2.CLASS;
            break;
          case ENUM:
            cd.type = ClassType2.ENUM;
            break;
          case ANONYMOUS_CLASS:
            cd.type = ClassType2.ANONYMOUS_CLASS;
            break;
          default:
            // nothing to do
        }
        cd.modifier = entry.getValue().getModifierList();
        cd.intrfc = entry.getValue().getInterfaceList();
        
        List<FieldData2> field = new ArrayList<>();
        for (FieldData fd : entry.getValue().getFieldList()) {
          FieldData2 fd2 = new FieldData2();
          fd2.name = fd.getName();
          fd2.type = fd.getType();
          fd2.modifier = fd.getModifierList();
          field.add(fd2);
        }
        cd.field = field;


        cd.innerClass = entry.getValue().getInnerClassList();

        List<MethodData2> constructor = new ArrayList<>();
        for (MethodData md : entry.getValue().getConstructorList()) {
          MethodData2 md2 = new MethodData2();
          md2.returnType = md.getReturnType();
          md2.modifier = md.getModifierList();
          List<ParameterData2> parameter = new ArrayList<>();
          for (ParameterData pd : md.getParameterList()) {
            ParameterData2 pd2 = new ParameterData2();
            pd2.name = pd.getName();
            pd2.type = pd.getType();
            pd2.modifier = pd.getModifierList();
            parameter.add(pd2);
          }
          md2.parameter = parameter;
          md2.outgoingMethodCalls = md.getOutgoingMethodCallsList();
          md2.isConstructor = md.getIsConstructor();
          md2.annotation = md.getAnnotationList();
          md2.metric = md.getMetricMap();
          constructor.add(md2);
        }
        cd.constructor = constructor;

        Map<String, MethodData2> methodData =
             new HashMap<>();
        
        for (Map.Entry<String, MethodData> entry2 : entry.getValue().getMethodDataMap()
            .entrySet()) {
          MethodData2 md = new MethodData2();
          md.returnType = entry2.getValue().getReturnType();
          md.modifier = entry2.getValue().getModifierList();

          List<ParameterData2> parameter = new ArrayList<>();
          for (ParameterData pd : entry2.getValue().getParameterList()) {
            ParameterData2 pd2 = new ParameterData2();
            pd2.name = pd.getName();
            pd2.type = pd.getType();
            pd2.modifier = pd.getModifierList();
            parameter.add(pd2);
          }
          md.parameter = parameter;

          md.outgoingMethodCalls = entry2.getValue().getOutgoingMethodCallsList();
          md.isConstructor = entry2.getValue().getIsConstructor();
          md.annotation = entry2.getValue().getAnnotationList();
          md.metric = entry2.getValue().getMetricMap();
          methodData.put(entry2.getKey(), md);
        }
        cd.methodData = methodData;
        cd.variable = entry.getValue().getVariableList();
        cd.superClass = entry.getValue().getSuperClass();
        cd.enumConstant = entry.getValue().getEnumConstantList();
        cd.annotation = entry.getValue().getAnnotationList();
        cd.classMetric = entry.getValue().getMetricMap();

        classData.put(entry.getKey(), cd);
      }
      fileReport.classData = classData;


      fileReport.fileMetric = receivedFileDataMetric;
      fileReport.author = receivedFileDataAuthor;
      fileReport.modifiedLines = receivedFileDataModifiedLines;
      fileReport.addedLines = receivedFileDataAddedLines;
      fileReport.deletedLines = receivedFileDataDeletedLines;

      // a filereport for the same file in the same commit can be received more than one time
      // Thus, we make sure to update it if it existed before
      FileReport oldReport = FileReport.findByTokenAndAppNameAndPackageNameAndFileNameAndCommitId(
            receivedFileDataLandscapeToken, receivedFileDataAppName, 
            receivedFileDataPackageName + "." + receivedFileDataFileName, receivedFileDataCommitId);

      if (oldReport != null) {
        oldReport.packageName = fileReport.packageName;
        oldReport.importName = fileReport.importName;
        oldReport.classData = fileReport.classData;
        oldReport.fileMetric = fileReport.fileMetric;
        oldReport.author = fileReport.author;
        oldReport.modifiedLines = fileReport.modifiedLines;
        oldReport.addedLines = fileReport.addedLines;
        oldReport.deletedLines = fileReport.deletedLines;
        oldReport.update();
      } else {
        fileReport.persist();
      }
    }
  }

  /**
   * Processes a stateDataRequest. Looks into the used storage and returns the branch's last
   * commit.
   *
   * @param stateDataRequest the StateDataRequest to handle
   * @return the current commit's sha1
   */
  public String processStateData(final StateDataRequest stateDataRequest) {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Request for state - upstream: {}, branch: {}, token: {}, secret: {}",
          stateDataRequest.getUpstreamName(),
          stateDataRequest.getBranchName(),
          stateDataRequest.getLandscapeToken(),
          stateDataRequest.getLandscapeSecret());
    }

    final String branchName = stateDataRequest.getBranchName();
    final String landscapeToken = stateDataRequest.getLandscapeToken();
    LatestCommit latestCommit = LatestCommit
        .findByBranchNameAndLandscapeToken(branchName, landscapeToken);

    // Send the empty string if the state of the branch is unknown, otherwise the SHA1 of
    // the branch's last commit
    if (latestCommit != null) {
      return latestCommit.commitId;
    } 
    return "";
  }

}
