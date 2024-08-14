package net.explorviz.code.analysis;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.explorviz.code.persistence.FileReportTable;
import net.explorviz.code.persistence.entity.FileReport;
import net.explorviz.code.persistence.entity.FileReport.ClassData2;
import net.explorviz.code.persistence.entity.FileReport.ClassData2.ClassType2;
import net.explorviz.code.persistence.entity.FileReport.ClassData2.FieldData2;
import net.explorviz.code.persistence.entity.FileReport.ClassData2.MethodData2;
import net.explorviz.code.persistence.entity.FileReport.ClassData2.MethodData2.ParameterData2;
import net.explorviz.code.persistence.repository.FileReportRepository;
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

  private final FileReportRepository fileReportRepository;

  @Inject
  public FileDataAnalysis(final FileReportRepository fileReportRepository) {
    this.fileReportRepository = fileReportRepository;
  }

  /**
   * Processes a FileData package. Stores the data into the local storage.
   *
   * @param fileData the FileData to handle
   */
  public void processFileData(final FileData fileData) {
    LOGGER.atTrace().addArgument(fileData).log("Received file data: {}");

    FileReportTable fileReportTable = updateOrCreateFileReportTable(fileData);
    FileReport fileReport = createFileReport(fileData, fileReportTable);
    updateFileReport(fileData, fileReport);
  }

  private FileReportTable updateOrCreateFileReportTable(FileData fileData) {
    final String receivedFileDataLandscapeToken = fileData.getLandscapeToken();
    final String receivedFileDataAppName = fileData.getApplicationName();
    final String receivedFileDataCommitId = fileData.getCommitID();
    final String receivedFileDataPackageName = fileData.getPackageName();
    final String receivedFileDataFileName = fileData.getFileName();
    final String fqFileName = receivedFileDataPackageName + "." + receivedFileDataFileName;

    FileReportTable fileReportTable = FileReportTable
        .findByTokenAndAppName(receivedFileDataLandscapeToken, receivedFileDataAppName);

    if (fileReportTable == null) {
      LOGGER.atTrace().log("CREATE FILE REPORT TABLE.");
      fileReportTable =
          createNewFileReportTable(receivedFileDataLandscapeToken, receivedFileDataAppName,
              receivedFileDataCommitId, fqFileName);
    } else {
      updateExistingFileReportTable(fileReportTable, receivedFileDataCommitId, fqFileName);
    }
    return fileReportTable;
  }

  private FileReportTable createNewFileReportTable(String token, String appName, String commitId,
      String fqFileName) {
    final FileReportTable newFileReportTable = new FileReportTable();
    newFileReportTable.setLandscapeToken(token);
    newFileReportTable.setAppName(appName);
    final Map<String, Map<String, String>> table = new HashMap<>();
    final Map<String, String> fqFileNameToCommitId = new HashMap<>();
    fqFileNameToCommitId.put(fqFileName, commitId);
    table.put(commitId, fqFileNameToCommitId);
    newFileReportTable.setCommitIdTofqnFileNameToCommitIdMap(table);
    newFileReportTable.persist();
    return newFileReportTable;
  }

  private void updateExistingFileReportTable(FileReportTable fileReportTable, String commitId,
      String fqFileName) {
    final Map<String, Map<String, String>> table =
        fileReportTable.getCommitIdTofqnFileNameToCommitIdMap();
    Map<String, String> fqFileNameToCommitIdMap = table.getOrDefault(commitId, new HashMap<>());
    fqFileNameToCommitIdMap.put(fqFileName, commitId);
    table.put(commitId, fqFileNameToCommitIdMap);
    fileReportTable.setCommitIdTofqnFileNameToCommitIdMap(table);
    fileReportTable.update();
  }

  private FileReport createFileReport(FileData fileData, FileReportTable fileReportTable) {
    FileReport fileReport = new FileReport();
    fillFileReportBasicInfo(fileReport, fileData);
    fillClassData(fileReport, fileData.getClassDataMap());
    return fileReport;
  }

  private void fillFileReportBasicInfo(FileReport fileReport, FileData fileData) {
    fileReport.setLandscapeToken(fileData.getLandscapeToken());
    fileReport.setAppName(fileData.getApplicationName());
    fileReport.setCommitId(fileData.getCommitID());
    fileReport.setFileName(fileData.getFileName());
    fileReport.setPackageName(fileData.getPackageName());
    fileReport.setImportName(fileData.getImportNameList());
    fileReport.setFileMetric(fileData.getMetricMap());
    fileReport.setAuthor(fileData.getAuthor());
    fileReport.setModifiedLines(fileData.getModifiedLines());
    fileReport.setAddedLines(fileData.getAddedLines());
    fileReport.setDeletedLines(fileData.getDeletedLines());
  }

  private void fillClassData(FileReport fileReport,
      Map<String, ClassData> receivedFileDataClassData) {
    final Map<String, ClassData2> classData = new HashMap<>();
    for (Map.Entry<String, ClassData> entry : receivedFileDataClassData.entrySet()) {
      classData.put(entry.getKey(), transformClassData(entry.getValue()));
    }
    fileReport.setClassData(classData);
  }

  private ClassData2 transformClassData(ClassData data) {
    ClassData2 cd = new ClassData2();
    switch (data.getType()) {
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
    cd.setModifier(data.getModifierList());
    cd.setIntrfc(data.getInterfaceList());
    cd.setField(transformFields(data.getFieldList()));
    cd.setInnerClass(data.getInnerClassList());
    cd.setConstructor(transformMethods(data.getConstructorList()));
    cd.setMethodData(transformMethodData(data.getMethodDataMap()));
    cd.setVariable(data.getVariableList());
    cd.setSuperClass(data.getSuperClass());
    cd.setEnumConstant(data.getEnumConstantList());
    cd.setAnnotation(data.getAnnotationList());
    cd.setClassMetric(data.getMetricMap());
    return cd;
  }

  private List<FieldData2> transformFields(List<FieldData> fields) {
    List<FieldData2> transformedFields = new ArrayList<>();
    for (FieldData fd : fields) {
      FieldData2 fd2 = new FieldData2();
      fd2.setName(fd.getName());
      fd2.setType(fd.getType());
      fd2.setModifier(fd.getModifierList());
      transformedFields.add(fd2);
    }
    return transformedFields;
  }

  private List<MethodData2> transformMethods(List<MethodData> methods) {
    List<MethodData2> transformedMethods = new ArrayList<>();
    for (MethodData md : methods) {
      transformedMethods.add(transformMethod(md));
    }
    return transformedMethods;
  }

  private MethodData2 transformMethod(MethodData md) {
    MethodData2 md2 = new MethodData2();
    md2.setReturnType(md.getReturnType());
    md2.setModifier(md.getModifierList());
    md2.setParameter(transformParameters(md.getParameterList()));
    md2.setOutgoingMethodCalls(md.getOutgoingMethodCallsList());
    md2.setConstructor(md.getIsConstructor());
    md2.setAnnotation(md.getAnnotationList());
    md2.setMetric(md.getMetricMap());
    return md2;
  }

  private List<ParameterData2> transformParameters(List<ParameterData> parameters) {
    List<ParameterData2> transformedParameters = new ArrayList<>();
    for (ParameterData pd : parameters) {
      ParameterData2 pd2 = new ParameterData2();
      pd2.setName(pd.getName());
      pd2.setType(pd.getType());
      pd2.setModifier(pd.getModifierList());
      transformedParameters.add(pd2);
    }
    return transformedParameters;
  }

  private Map<String, MethodData2> transformMethodData(Map<String, MethodData> methodDataMap) {
    Map<String, MethodData2> methodData = new HashMap<>();
    for (Map.Entry<String, MethodData> entry : methodDataMap.entrySet()) {
      methodData.put(entry.getKey(), transformMethod(entry.getValue()));
    }
    return methodData;
  }

  private void updateFileReport(FileData fileData, FileReport fileReport) {
    final String fqFileName = fileData.getPackageName() + "." + fileData.getFileName();
    final String commitId = fileData.getCommitID();
    final String token = fileData.getLandscapeToken();
    final String appName = fileData.getApplicationName();
    final String packageName = fileData.getPackageName();

    FileReport oldReport = this.fileReportRepository
        .findByTokenAndAppNameAndPackageNameAndFileNameAndCommitId(
            token, appName, packageName + "." + fqFileName, commitId);

    if (oldReport != null) {
      updateExistingFileReport(oldReport, fileReport);
    } else {
      this.fileReportRepository.persist(fileReport);
    }
  }

  private void updateExistingFileReport(FileReport oldReport, FileReport newReport) {
    oldReport.setPackageName(newReport.getPackageName());
    oldReport.setImportName(newReport.getImportName());
    oldReport.setClassData(newReport.getClassData());
    oldReport.setFileMetric(newReport.getFileMetric());
    oldReport.setAuthor(newReport.getAuthor());
    oldReport.setModifiedLines(newReport.getModifiedLines());
    oldReport.setAddedLines(newReport.getAddedLines());
    oldReport.setDeletedLines(newReport.getDeletedLines());
    this.fileReportRepository.update(oldReport);
  }
}
