package net.explorviz.code.analysis;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.mongodb.MongoTestResource;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.explorviz.code.mongo.CommitReport;
import net.explorviz.code.mongo.CommitReport.FileMetric;
import net.explorviz.code.proto.CommitReportData;
import net.explorviz.code.proto.FileMetricData;
import net.explorviz.code.testhelper.TestConstants;
import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(value = MongoTestResource.class,
    initArgs = @ResourceArg(name = MongoTestResource.PORT, value = "27032"))
public class CommitReportAnalysisTest {

  @ConfigProperty(name = "quarkus.mongodb.database")
  /* default */ String mongoDBName; // NOCS

  @Inject
  MongoClient mongoClient;

  @Inject
  CommitReportAnalysis commitReportAnalysis;

  private MongoDatabase getMongoDatabase() {
    return this.mongoClient.getDatabase(mongoDBName);
  }

  private String readJsonFileAsString(String path) throws IOException {
    return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
  }

  private CommitReport convertCommitReportGrpcToMongo(final CommitReportData commitReportData) {
    final CommitReport commitReport = new CommitReport();
    commitReport.setCommitId(commitReportData.getCommitID());
    commitReport.setParentCommitId(commitReportData.getParentCommitID());
    commitReport.setBranchName(commitReportData.getBranchName());
    commitReport.setFiles(commitReportData.getFilesList());
    commitReport.setModified(commitReportData.getModifiedList());
    commitReport.setDeleted(commitReportData.getDeletedList());
    commitReport.setAdded(commitReportData.getAddedList());
    commitReport.setLandscapeToken(commitReportData.getLandscapeToken());
    commitReport.setFileHash(commitReportData.getFileHashList());
    commitReport.setApplicationName(commitReportData.getApplicationName());

    final List<FileMetric> receivedCommitReportFileMetric = new ArrayList<>();

    for (final FileMetricData fileMetricData : commitReportData.getFileMetricList()) {
      final CommitReport.FileMetric fileMetric = new CommitReport.FileMetric(); // NOPMD
      fileMetric.setFileName(fileMetricData.getFileName());
      fileMetric.setLoc(fileMetricData.getLoc());
      fileMetric.setCyclomaticComplexity(fileMetricData.getCyclomaticComplexity());
      fileMetric.setNumberOfMethods(fileMetricData.getNumberOfMethods());
      receivedCommitReportFileMetric.add(fileMetric);
    }
    commitReport.setFileMetric(receivedCommitReportFileMetric);

    return commitReport;
  }

  private CommitReportData jsonToGrpcCommitReportData(String json)
      throws InvalidProtocolBufferException {
    CommitReportData.Builder builder = CommitReportData.newBuilder();
    JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
    return builder.build();
  }

  @BeforeEach
  public void setUp() {
    // Drop the database before each test to ensure test isolation
    this.getMongoDatabase().drop();
  }

  @Test
  public void testCommitWithUnknownParentAndNoFileReportTable() throws IOException {
    final String jsonCommitReport =
        this.readJsonFileAsString("src/test/resources/CommitReport-1.json");

    final CommitReportData commitReport = this.jsonToGrpcCommitReportData(jsonCommitReport);

    for (int i = 0; i < 10; i++) {
      this.commitReportAnalysis.processCommitReport(commitReport);

      MongoCollection<Document> collection =
          this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_COMMIT_REPORT);
      Assertions.assertEquals(1, collection.countDocuments());

      Assertions.assertEquals(convertCommitReportGrpcToMongo(commitReport),
          CommitReport.findByTokenAndApplicationNameAndCommitId(commitReport.getLandscapeToken(),
              commitReport.getApplicationName(), commitReport.getCommitID()));

      collection =
          this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_BRANCH_POINT);
      Assertions.assertEquals(1, collection.countDocuments());

      collection =
          this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_APPLICATION);
      Assertions.assertEquals(1, collection.countDocuments());

      collection =
          this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_LATEST_COMMIT);
      Assertions.assertEquals(1, collection.countDocuments());

      int iteratorCount = 0;

      final Iterator<String> iterator = this.getMongoDatabase().listCollectionNames().iterator();

      while (iterator.hasNext()) {
        iterator.next();
        iteratorCount++;
      }

      Assertions.assertEquals(4, iteratorCount);
    }
  }

  @Test
  public void testFirstCommitAndNoFileReportTable() throws IOException {
    final String jsonCommitReport =
        this.readJsonFileAsString("src/test/resources/CommitReport-1-no-parent.json");

    final CommitReportData commitReport = this.jsonToGrpcCommitReportData(jsonCommitReport);

    this.commitReportAnalysis.processCommitReport(commitReport);

    MongoCollection<Document> collection =
        this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_COMMIT_REPORT);
    Assertions.assertEquals(1, collection.countDocuments());

    Assertions.assertEquals(convertCommitReportGrpcToMongo(commitReport),
        CommitReport.findByTokenAndApplicationNameAndCommitId(commitReport.getLandscapeToken(),
            commitReport.getApplicationName(), commitReport.getCommitID()));

    collection =
        this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_BRANCH_POINT);
    Assertions.assertEquals(1, collection.countDocuments());

    collection =
        this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_APPLICATION);
    Assertions.assertEquals(1, collection.countDocuments());

    collection =
        this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_LATEST_COMMIT);
    Assertions.assertEquals(1, collection.countDocuments());

    int iteratorCount = 0;

    final Iterator<String> iterator = this.getMongoDatabase().listCollectionNames().iterator();

    while (iterator.hasNext()) {
      iterator.next();
      iteratorCount++;
    }

    Assertions.assertEquals(4, iteratorCount);

  }

  @Test
  public void testCommitWithKnownParentAndNoFileReportTable() throws IOException {
    String jsonCommitReport =
        this.readJsonFileAsString("src/test/resources/CommitReport-0.json");
    final CommitReportData commitReport0 = this.jsonToGrpcCommitReportData(jsonCommitReport);
    this.commitReportAnalysis.processCommitReport(commitReport0);

    jsonCommitReport =
        this.readJsonFileAsString("src/test/resources/CommitReport-1.json");
    final CommitReportData commitReport1 = this.jsonToGrpcCommitReportData(jsonCommitReport);
    this.commitReportAnalysis.processCommitReport(commitReport1);

    MongoCollection<Document> collection =
        this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_COMMIT_REPORT);
    Assertions.assertEquals(2, collection.countDocuments());

    Assertions.assertEquals(convertCommitReportGrpcToMongo(commitReport0),
        CommitReport.findByTokenAndApplicationNameAndCommitId(commitReport0.getLandscapeToken(),
            commitReport0.getApplicationName(), commitReport0.getCommitID()));

    Assertions.assertEquals(convertCommitReportGrpcToMongo(commitReport1),
        CommitReport.findByTokenAndApplicationNameAndCommitId(commitReport1.getLandscapeToken(),
            commitReport1.getApplicationName(), commitReport1.getCommitID()));

    collection =
        this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_BRANCH_POINT);
    Assertions.assertEquals(1, collection.countDocuments());

    collection =
        this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_APPLICATION);
    Assertions.assertEquals(1, collection.countDocuments());

    collection =
        this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_LATEST_COMMIT);
    Assertions.assertEquals(1, collection.countDocuments());

    int iteratorCount = 0;

    final Iterator<String> iterator = this.getMongoDatabase().listCollectionNames().iterator();

    while (iterator.hasNext()) {
      iterator.next();
      iteratorCount++;
    }

    Assertions.assertEquals(4, iteratorCount);

  }

}
