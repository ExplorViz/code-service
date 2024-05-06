package net.explorviz.code.grpc;

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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.explorviz.code.mongo.FileReportTable;
import net.explorviz.code.proto.FileData;
import net.explorviz.code.testhelper.TestConstants;
import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(value = MongoTestResource.class,
    initArgs = @ResourceArg(name = MongoTestResource.PORT, value = "27032"))
public class GrpcGatewayTest {

  @ConfigProperty(name = "quarkus.mongodb.database")
  /* default */ String mongoDBName; // NOCS

  @Inject
  MongoClient mongoClient;

  @Inject
  GrpcGateway grpcGateway;

  private MongoDatabase getMongoDatabase() {
    return this.mongoClient.getDatabase(mongoDBName);
  }

  private String readJsonFileAsString(String path) throws IOException {
    return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
  }

  private FileData jsonToGrpcFileData(String json) throws InvalidProtocolBufferException {
    FileData.Builder builder = FileData.newBuilder();
    JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
    return builder.build();
  }

  @BeforeEach
  public void setUp() {
    // Drop the database before each test to ensure test isolation
    this.getMongoDatabase().drop();
  }

  @Test
  public void testGrpcGatewayFileDataSingle() throws IOException {
    final String jsonPersonFromCodeAgent =
        this.readJsonFileAsString("src/test/resources/Person-1.json");

    final FileData fileDataPersonClass = this.jsonToGrpcFileData(jsonPersonFromCodeAgent);

    this.grpcGateway.processFileData(fileDataPersonClass);

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
  public void testGrpcGatewayFileDataMultipleDataForSameFile() throws IOException {
    String jsonPersonFromCodeAgent =
        this.readJsonFileAsString("src/test/resources/Person-1.json");
    FileData fileDataPerson1Class = this.jsonToGrpcFileData(jsonPersonFromCodeAgent);
    this.grpcGateway.processFileData(fileDataPerson1Class);

    jsonPersonFromCodeAgent =
        this.readJsonFileAsString("src/test/resources/Person-2.json");
    FileData fileDataPerson2Class = this.jsonToGrpcFileData(jsonPersonFromCodeAgent);
    this.grpcGateway.processFileData(fileDataPerson2Class);

    jsonPersonFromCodeAgent =
        this.readJsonFileAsString("src/test/resources/Person-3.json");
    FileData fileDataPerson3Class = this.jsonToGrpcFileData(jsonPersonFromCodeAgent);
    this.grpcGateway.processFileData(fileDataPerson3Class);

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
