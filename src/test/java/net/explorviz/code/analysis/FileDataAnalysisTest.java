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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.explorviz.code.mongo.FileReportTable;
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
public class FileDataAnalysisTest {

  @ConfigProperty(name = "quarkus.mongodb.database")
  /* default */ String mongoDBName; // NOCS

  @Inject
  MongoClient mongoClient;

  @Inject
  FileDataAnalysis fileDataAnalysis;

  private MongoDatabase getMongoDatabase() {
    return this.mongoClient.getDatabase(mongoDBName);
  }

  @BeforeEach
  public void setUp() {
    // Drop the database before each test to ensure test isolation
    this.getMongoDatabase().drop();
  }

  @Test
  public void testFileDataAnalysisSingle() throws IOException {

    final FileData fileDataPersonClass =
        HelperMethods.readJsonAndConvertGrpcFileData("src/test/resources/Person-1.json");

    this.fileDataAnalysis.processFileData(fileDataPersonClass);

    MongoCollection<Document> collection =
        this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_FILE_REPORT_TABLE);
    Assertions.assertEquals(1, collection.countDocuments());

    collection = this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_FILE_REPORT);
    Assertions.assertEquals(1, collection.countDocuments());

    int iteratorCount = 0;

    final Iterator<String> iterator = this.getMongoDatabase().listCollectionNames().iterator();

    while (iterator.hasNext()) {
      iterator.next();
      iteratorCount++;
    }

    Assertions.assertEquals(2, iteratorCount);
  }

  @Test
  public void testFileDataAnalysisMultipleDataForSameFile() throws IOException {

    FileData fileDataPerson1Class =
        HelperMethods.readJsonAndConvertGrpcFileData("src/test/resources/Person-1.json");
    this.fileDataAnalysis.processFileData(fileDataPerson1Class);

    FileData fileDataPerson2Class =
        HelperMethods.readJsonAndConvertGrpcFileData("src/test/resources/Person-2.json");
    this.fileDataAnalysis.processFileData(fileDataPerson2Class);

    FileData fileDataPerson3Class =
        HelperMethods.readJsonAndConvertGrpcFileData("src/test/resources/Person-3.json");
    this.fileDataAnalysis.processFileData(fileDataPerson3Class);

    MongoCollection<Document> collection =
        this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_FILE_REPORT_TABLE);
    Assertions.assertEquals(1, collection.countDocuments());

    final FileReportTable fileReportTable =
        FileReportTable.findByTokenAndAppName("mytokenvalue", "petclinic");

    Assertions.assertEquals("petclinic", fileReportTable.getAppName());
    Assertions.assertEquals("mytokenvalue", fileReportTable.getLandscapeToken());
    Assertions.assertEquals(3, fileReportTable.getCommitIdTofqnFileNameToCommitIdMap().size());

    List<FileData>
        persons = Arrays.asList(fileDataPerson1Class, fileDataPerson2Class, fileDataPerson3Class);

    for (FileData person : persons) {
      Map<String, String> fqnToCommitId =
          fileReportTable.getCommitIdTofqnFileNameToCommitIdMap().get(person.getCommitID());
      String fqFileName = person.getPackageName() + "." + person.getFileName();

      Assertions.assertTrue(fileReportTable.getCommitIdTofqnFileNameToCommitIdMap()
          .containsKey(person.getCommitID()));
      Assertions.assertTrue(fqnToCommitId.containsKey(fqFileName),
          "The map should contain the fully qualified file name.");
      Assertions.assertEquals(person.getCommitID(), fqnToCommitId.get(fqFileName),
          "The commit IDs should match.");
    }

    collection = this.getMongoDatabase().getCollection(TestConstants.MONGO_COLLECTION_FILE_REPORT);
    Assertions.assertEquals(3, collection.countDocuments());

    int iteratorCount = 0;

    final Iterator<String> iterator = this.getMongoDatabase().listCollectionNames().iterator();

    while (iterator.hasNext()) {
      iterator.next();
      iteratorCount++;
    }

    Assertions.assertEquals(2, iteratorCount);
  }


}
