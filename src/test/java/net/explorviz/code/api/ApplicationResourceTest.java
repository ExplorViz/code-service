package net.explorviz.code.api;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.mongodb.MongoTestResource;
import jakarta.inject.Inject;
import java.util.List;
import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(value = MongoTestResource.class,
    initArgs = @ResourceArg(name = MongoTestResource.PORT, value = "27032"))
public class ApplicationResourceTest {

  @ConfigProperty(name = "quarkus.mongodb.database")
  /* default */ String mongoDBName; // NOCS

  @Inject
  MongoClient mongoClient;

  @Inject
  ApplicationsResource applicationResource;

  private MongoDatabase getMongoDatabase() {
    return this.mongoClient.getDatabase(mongoDBName);
  }

  @BeforeEach
  public void setUp() {
    // Drop the database before each test to ensure test isolation
    this.getMongoDatabase().drop();
  }

  @Test
  public void testUnitApplicationResourceValidToken() {
    this.getMongoDatabase().createCollection("Application");
    MongoCollection<Document> collection = this.getMongoDatabase().getCollection("Application");
    Document newApp = new Document()
        .append("applicationName", "test-name")
        .append("landscapeToken", "test-token");

    collection.insertOne(newApp);

    List<String> appNames = this.applicationResource.list("test-token");

    Assertions.assertEquals(1, collection.countDocuments());
    Assertions.assertEquals(1, appNames.size());
    Assertions.assertEquals("test-name", appNames.get(0));
  }

  @Test
  public void testUnitApplicationResourceUnknownToken() {
    this.getMongoDatabase().createCollection("Application");
    MongoCollection<Document> collection = this.getMongoDatabase().getCollection("Application");
    Document newApp = new Document()
        .append("applicationName", "test-name")
        .append("landscapeToken", "test-token");

    collection.insertOne(newApp);

    List<String> appNames = this.applicationResource.list("test-unknown-token");

    Assertions.assertEquals(1, collection.countDocuments());
    Assertions.assertTrue(appNames.isEmpty());
  }

}
