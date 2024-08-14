package net.explorviz.code.testhelper;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.explorviz.code.persistence.entity.CommitReport;
import net.explorviz.code.persistence.entity.CommitReport.FileMetric;
import net.explorviz.code.proto.CommitReportData;
import net.explorviz.code.proto.FileData;
import net.explorviz.code.proto.FileMetricData;

public class HelperMethods {

  public static String readJsonFileAsString(String path) throws IOException {
    return Files.readString(Paths.get(path));
  }

  public static CommitReportData jsonToGrpcCommitReportData(String json)
      throws InvalidProtocolBufferException {
    CommitReportData.Builder builder = CommitReportData.newBuilder();
    JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
    return builder.build();
  }

  public static CommitReportData readJsonAndConvertGrpcCommitReportData(String path)
      throws IOException {
    return jsonToGrpcCommitReportData(readJsonFileAsString(path));
  }

  public static FileData jsonToGrpcFileData(String json) throws InvalidProtocolBufferException {
    FileData.Builder builder = FileData.newBuilder();
    JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
    return builder.build();
  }

  public static FileData readJsonAndConvertGrpcFileData(String path)
      throws IOException {
    return jsonToGrpcFileData(readJsonFileAsString(path));
  }

  public static CommitReport convertCommitReportGrpcToMongo(
      final CommitReportData commitReportData) {

    final List<FileMetric> receivedCommitReportFileMetric = new ArrayList<>();

    for (final FileMetricData fileMetricData : commitReportData.getFileMetricList()) {
      final CommitReport.FileMetric fileMetric = new CommitReport.FileMetric(); // NOPMD
      fileMetric.setFileName(fileMetricData.getFileName());
      fileMetric.setLoc(fileMetricData.getLoc());
      fileMetric.setCyclomaticComplexity(fileMetricData.getCyclomaticComplexity());
      fileMetric.setNumberOfMethods(fileMetricData.getNumberOfMethods());
      receivedCommitReportFileMetric.add(fileMetric);
    }

    final CommitReport commitReport =
        new CommitReport(commitReportData.getCommitID(), commitReportData.getParentCommitID(),
            commitReportData.getBranchName(), commitReportData.getFilesList(),
            commitReportData.getModifiedList(), commitReportData.getDeletedList(),
            commitReportData.getAddedList(), receivedCommitReportFileMetric,
            commitReportData.getLandscapeToken(), commitReportData.getFileHashList(),
            commitReportData.getApplicationName());
    return commitReport;
  }

}
