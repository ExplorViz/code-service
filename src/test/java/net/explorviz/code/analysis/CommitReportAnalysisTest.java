package net.explorviz.code.analysis;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.mongodb.MongoTestResource;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Iterator;
import net.explorviz.code.persistence.entity.FileReportTable;
import net.explorviz.code.persistence.repository.CommitReportRepository;
import net.explorviz.code.persistence.repository.FileReportTableRepository;
import net.explorviz.code.proto.CommitReportData;
import net.explorviz.code.proto.FileData;
import net.explorviz.code.testhelper.HelperMethods;
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

  @Inject
  FileDataAnalysis fileDataAnalysis;

  @Inject
  CommitReportRepository commitReportRepository;

  @Inject
  FileReportTableRepository fileReportTableRepository;

  private MongoDatabase getMongoDatabase() {
    return this.mongoClient.getDatabase(mongoDBName);
  }

  @BeforeEach
  public void setUp() {
    // Drop the database before each test to ensure test isolation
    this.getMongoDatabase().drop();
  }

  @Test
  public void testCommitWithUnknownParentAndNoFileReportTable() throws IOException {

    final CommitReportData commitReport = HelperMethods.readJsonAndConvertGrpcCommitReportData(
        "src/test/resources/CommitReport-1.json");

    for (int i = 0; i < 10; i++) {
      this.commitReportAnalysis.processCommitReport(commitReport);

      MongoCollection<Document> collection =
          this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_COMMIT_REPORT);
      Assertions.assertEquals(1, collection.countDocuments());

      Assertions.assertEquals(HelperMethods.convertCommitReportGrpcToMongo(commitReport),
          this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(
              commitReport.getLandscapeToken(),
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

    final CommitReportData commitReport = HelperMethods.readJsonAndConvertGrpcCommitReportData(
        "src/test/resources/CommitReport-1-no-parent.json");

    this.commitReportAnalysis.processCommitReport(commitReport);

    MongoCollection<Document> collection =
        this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_COMMIT_REPORT);
    Assertions.assertEquals(1, collection.countDocuments());

    Assertions.assertEquals(HelperMethods.convertCommitReportGrpcToMongo(commitReport),
        this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(
            commitReport.getLandscapeToken(),
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
  public void testFirstCommitWithFileReportTable() throws IOException {

    final FileData fileDataPersonClass =
        HelperMethods.readJsonAndConvertGrpcFileData("src/test/resources/Person-1.json");

    this.fileDataAnalysis.processFileData(fileDataPersonClass);

    FileReportTable tableBefore =
        this.fileReportTableRepository.findByTokenAndAppName(
            fileDataPersonClass.getLandscapeToken(),
            fileDataPersonClass.getApplicationName());

    Assertions.assertEquals(1, tableBefore.getCommitIdTofqnFileNameToCommitIdMap().size());

    final CommitReportData commitReport = HelperMethods.readJsonAndConvertGrpcCommitReportData(
        "src/test/resources/CommitReport-1-no-parent.json");

    this.commitReportAnalysis.processCommitReport(commitReport);

    FileReportTable tableAfter =
        this.fileReportTableRepository.findByTokenAndAppName(fileDataPersonClass.getLandscapeToken(),
            fileDataPersonClass.getApplicationName());

    Assertions.assertEquals(1, tableAfter.getCommitIdTofqnFileNameToCommitIdMap().size());

    Assertions.assertEquals(tableBefore, tableAfter);

    MongoCollection<Document> collection =
        this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_COMMIT_REPORT);
    Assertions.assertEquals(1, collection.countDocuments());

    Assertions.assertEquals(HelperMethods.convertCommitReportGrpcToMongo(commitReport),
        this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(
            commitReport.getLandscapeToken(),
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

    Assertions.assertEquals(6, iteratorCount);
  }

  @Test
  public void testCommitWithKnownParentAndNoFileReportTable() throws IOException {

    final CommitReportData commitReport0 = HelperMethods.readJsonAndConvertGrpcCommitReportData(
        "src/test/resources/CommitReport-0.json");
    this.commitReportAnalysis.processCommitReport(commitReport0);

    final CommitReportData commitReport1 = HelperMethods.readJsonAndConvertGrpcCommitReportData(
        "src/test/resources/CommitReport-1.json");
    this.commitReportAnalysis.processCommitReport(commitReport1);

    MongoCollection<Document> collection =
        this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_COMMIT_REPORT);
    Assertions.assertEquals(2, collection.countDocuments());

    Assertions.assertEquals(HelperMethods.convertCommitReportGrpcToMongo(commitReport0),
        this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(
            commitReport0.getLandscapeToken(),
            commitReport0.getApplicationName(), commitReport0.getCommitID()));

    Assertions.assertEquals(HelperMethods.convertCommitReportGrpcToMongo(commitReport1),
        this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(
            commitReport1.getLandscapeToken(),
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

  @Test
  public void testCommitWithKnownParentAndWithFileReportTable() throws IOException {

    final FileData baseEntity0 =
        HelperMethods.readJsonAndConvertGrpcFileData("src/test/resources/BaseEntity-0.json");
    this.fileDataAnalysis.processFileData(baseEntity0);

    final FileData baseEntity1 =
        HelperMethods.readJsonAndConvertGrpcFileData("src/test/resources/BaseEntity-1.json");
    this.fileDataAnalysis.processFileData(baseEntity1);

    FileReportTable tableBefore =
        this.fileReportTableRepository.findByTokenAndAppName(baseEntity1.getLandscapeToken(),
            baseEntity1.getApplicationName());

    final CommitReportData commitReport0 = HelperMethods.readJsonAndConvertGrpcCommitReportData(
        "src/test/resources/CommitReport-0-no-parent.json");
    this.commitReportAnalysis.processCommitReport(commitReport0);

    final CommitReportData commitReport1 = HelperMethods.readJsonAndConvertGrpcCommitReportData(
        "src/test/resources/CommitReport-1.json");
    this.commitReportAnalysis.processCommitReport(commitReport1);

    FileReportTable tableAfter =
        this.fileReportTableRepository.findByTokenAndAppName(baseEntity1.getLandscapeToken(),
            baseEntity1.getApplicationName());

    Assertions.assertEquals(tableBefore, tableAfter);

    MongoCollection<Document> collection =
        this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_COMMIT_REPORT);
    Assertions.assertEquals(2, collection.countDocuments());

    Assertions.assertEquals(HelperMethods.convertCommitReportGrpcToMongo(commitReport0),
        this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(
            commitReport0.getLandscapeToken(),
            commitReport0.getApplicationName(), commitReport0.getCommitID()));

    Assertions.assertEquals(HelperMethods.convertCommitReportGrpcToMongo(commitReport1),
        this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(
            commitReport1.getLandscapeToken(),
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

    Assertions.assertEquals(6, iteratorCount);

  }

}
