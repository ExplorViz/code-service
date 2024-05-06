package net.explorviz.code.analysis;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.explorviz.code.mongo.FileReport;
import net.explorviz.code.mongo.FileReport.ClassData2;
import net.explorviz.code.mongo.FileReport.ClassData2.ClassType2;
import net.explorviz.code.mongo.FileReport.ClassData2.FieldData2;
import net.explorviz.code.mongo.FileReport.ClassData2.MethodData2;
import net.explorviz.code.mongo.FileReport.ClassData2.MethodData2.ParameterData2;
import net.explorviz.code.mongo.FileReportTable;
import net.explorviz.code.proto.ClassData;
import net.explorviz.code.proto.FieldData;
import net.explorviz.code.proto.FileData;
import net.explorviz.code.proto.MethodData;
import net.explorviz.code.proto.ParameterData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Analysis class for every incoming FileData record.
 */
@ApplicationScoped
public class FileDataAnalysis {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileDataAnalysis.class);

  /**
   * Processes a FileData package. Stores the data into the local storage.
   *
   * @param fileData the FileData to handle
   */
  public void processFileData(final FileData fileData) { // NOPMD

    LOGGER.atTrace().addArgument(fileData).log("Received file data: {}");

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

    final FileReport fileReport = new FileReport();

    // FileReport Table ------------------------------------------------------------------
    final FileReportTable fileReportTable = FileReportTable
        .findByTokenAndAppName(receivedFileDataLandscapeToken, receivedFileDataAppName);
    final String fqFileName = receivedFileDataPackageName + "." + receivedFileDataFileName;

    if (fileReportTable == null) {
      LOGGER.atTrace().log("CREATE FILE REPORT TABLE.");
      final FileReportTable newFileReportTable = new FileReportTable();
      newFileReportTable.setLandscapeToken(receivedFileDataLandscapeToken);
      newFileReportTable.setAppName(receivedFileDataAppName);
      final Map<String, Map<String, String>> table = new HashMap<>();
      final Map<String, String> fqFileNameToCommitId = new HashMap<>();
      fqFileNameToCommitId.put(fqFileName, receivedFileDataCommitId);
      // after the table is filled we can store it
      table.put(receivedFileDataCommitId, fqFileNameToCommitId);
      newFileReportTable.setCommitIdTofqnFileNameToCommitIdMap(table);
      newFileReportTable.persist();
    } else {
      // File Report Table already exists. We only add entries by manipulating the map.

      final Map<String, Map<String, String>> table = fileReportTable
          .getCommitIdTofqnFileNameToCommitIdMap();

      final boolean keyExists = table.containsKey(receivedFileDataCommitId);

      Map<String, String> fqFileNameToCommitIdMap = new HashMap<>();
      if (!keyExists) {
        LOGGER.warn("Normally Commit Report should be received before one of its File Reports");
      } else {
        fqFileNameToCommitIdMap = table.get(receivedFileDataCommitId);
      }
      // fill missing entry for current file report (might overwrite entry copied from parent)
      fqFileNameToCommitIdMap.put(fqFileName, receivedFileDataCommitId);
      table.put(receivedFileDataCommitId, fqFileNameToCommitIdMap);
      fileReportTable.setCommitIdTofqnFileNameToCommitIdMap(table);
      fileReportTable.update();
    }

    // -----------------------------------------------------------------------------------------

    fileReport.setLandscapeToken(receivedFileDataLandscapeToken);
    fileReport.setAppName(receivedFileDataAppName);
    fileReport.setCommitId(receivedFileDataCommitId);
    fileReport.setFileName(receivedFileDataFileName);
    fileReport.setPackageName(receivedFileDataPackageName);
    fileReport.setImportName(receivedFileDataImportName);

    final Map<String, ClassData2> classData = new HashMap<>();
    for (final Map.Entry<String, ClassData> entry
        : receivedFileDataClassData.entrySet()) {
      final ClassData2 cd = new ClassData2(); // NOPMD

      switch (entry.getValue().getType()) {
        case INTERFACE:
          cd.setType(ClassType2.INTERFACE);
          break;
        case ABSTRACT_CLASS:
          cd.setType(ClassType2.ABSTRACT_CLASS);
          break;
        case CLASS:
          cd.setType(ClassType2.CLASS);
          break;
        case ENUM:
          cd.setType(ClassType2.ENUM);
          break;
        case ANONYMOUS_CLASS:
          cd.setType(ClassType2.ANONYMOUS_CLASS);
          break;
        default:
          // nothing to do
      }
      cd.setModifier(entry.getValue().getModifierList());
      cd.setIntrfc(entry.getValue().getInterfaceList());

      final List<FieldData2> field = new ArrayList<>(); // NOPMD
      for (final FieldData fd : entry.getValue().getFieldList()) {
        final FieldData2 fd2 = new FieldData2(); // NOPMD
        fd2.setName(fd.getName());
        fd2.setType(fd.getType());
        fd2.setModifier(fd.getModifierList());
        field.add(fd2);
      }
      cd.setField(field);

      cd.setInnerClass(entry.getValue().getInnerClassList());

      final List<MethodData2> constructor = new ArrayList<>(); // NOPMD
      for (final MethodData md : entry.getValue().getConstructorList()) {
        final MethodData2 md2 = new MethodData2(); // NOPMD
        md2.setReturnType(md.getReturnType());
        md2.setModifier(md.getModifierList());
        final List<ParameterData2> parameter = new ArrayList<>(); // NOPMD
        for (final ParameterData pd : md.getParameterList()) {
          final ParameterData2 pd2 = new ParameterData2(); // NOPMD
          pd2.setName(pd.getName());
          pd2.setType(pd.getType());
          pd2.setModifier(pd.getModifierList());
          parameter.add(pd2);
        }
        md2.setParameter(parameter);
        md2.setOutgoingMethodCalls(md.getOutgoingMethodCallsList());
        md2.setConstructor(md.getIsConstructor());
        md2.setAnnotation(md.getAnnotationList());
        md2.setMetric(md.getMetricMap());
        constructor.add(md2);
      }
      cd.setConstructor(constructor);

      final Map<String, MethodData2> methodData =
          new HashMap<>(); // NOPMD

      for (final Map.Entry<String, MethodData> entry2 : entry.getValue().getMethodDataMap()
          .entrySet()) {
        final MethodData2 md = new MethodData2(); // NOPMD
        md.setReturnType(entry2.getValue().getReturnType());
        md.setModifier(entry2.getValue().getModifierList());

        final List<ParameterData2> parameter = new ArrayList<>(); // NOPMD
        for (final ParameterData pd : entry2.getValue().getParameterList()) {
          final ParameterData2 pd2 = new ParameterData2(); // NOPMD
          pd2.setName(pd.getName());
          pd2.setType(pd.getType());
          pd2.setModifier(pd.getModifierList());
          parameter.add(pd2);
        }
        md.setParameter(parameter);

        md.setOutgoingMethodCalls(entry2.getValue().getOutgoingMethodCallsList());
        md.setConstructor(entry2.getValue().getIsConstructor());
        md.setAnnotation(entry2.getValue().getAnnotationList());
        md.setMetric(entry2.getValue().getMetricMap());
        methodData.put(entry2.getKey(), md);
      }
      cd.setMethodData(methodData);
      cd.setVariable(entry.getValue().getVariableList());
      cd.setSuperClass(entry.getValue().getSuperClass());
      cd.setEnumConstant(entry.getValue().getEnumConstantList());
      cd.setAnnotation(entry.getValue().getAnnotationList());
      cd.setClassMetric(entry.getValue().getMetricMap());

      classData.put(entry.getKey(), cd);
    }
    fileReport.setClassData(classData);

    fileReport.setFileMetric(receivedFileDataMetric);
    fileReport.setAuthor(receivedFileDataAuthor);
    fileReport.setModifiedLines(receivedFileDataModifiedLines);
    fileReport.setAddedLines(receivedFileDataAddedLines);
    fileReport.setDeletedLines(receivedFileDataDeletedLines);

    // a filereport for the same file in the same commit can be received more than one time
    // Thus, we make sure to update it if it existed before
    final FileReport oldReport = FileReport
        .findByTokenAndAppNameAndPackageNameAndFileNameAndCommitId(
            receivedFileDataLandscapeToken, receivedFileDataAppName,
            receivedFileDataPackageName + "." + receivedFileDataFileName,
            receivedFileDataCommitId);

    if (oldReport != null) { // NOPMD
      oldReport.setPackageName(fileReport.getPackageName());
      oldReport.setImportName(fileReport.getImportName());
      oldReport.setClassData(fileReport.getClassData());
      oldReport.setFileMetric(fileReport.getFileMetric());
      oldReport.setAuthor(fileReport.getAuthor());
      oldReport.setModifiedLines(fileReport.getModifiedLines());
      oldReport.setAddedLines(fileReport.getAddedLines());
      oldReport.setDeletedLines(fileReport.getDeletedLines());
      oldReport.update();
    } else {
      fileReport.persist();
    }
  }

}
