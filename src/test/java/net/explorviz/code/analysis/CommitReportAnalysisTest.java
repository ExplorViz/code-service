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
import java.util.Iterator;
import net.explorviz.code.proto.CommitReportData;
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

}
