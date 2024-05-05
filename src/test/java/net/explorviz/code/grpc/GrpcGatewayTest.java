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
import java.util.Iterator;
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
        this.readJsonFileAsString("src/test/resources/Person.json");

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


}
