package net.explorviz.code.grpc;

import com.google.protobuf.Empty;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import net.explorviz.code.proto.StructureEventService;
import net.explorviz.code.proto.StructureFileEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GrpcService that listens to StructureEvents. Entrypoint for this service.
 */
@GrpcService
public class StructureEventServiceImpl implements StructureEventService {

  private static final Logger LOGGER = LoggerFactory.getLogger(StructureEventServiceImpl.class);

  // grpcurl -plaintext localhost:9000 list
  // grpcurl -plaintext -d '{"name": "test"}' localhost:9000 helloworld.Greeter.SayHello
  // grpcurl -plaintext localhost:9000 describe net.explorviz.code.proto.StructureEventService
  // grpcurl -plaintext -d '{"className": "test"}' localhost:9000
  // net.explorviz.code.proto.StructureEventService.sendCreateEvent

  @Override
  public Uni<Empty> sendStructureFileEvent(StructureFileEvent request) {
    LOGGER.trace("Received message: {}", request);
    return Uni.createFrom().item(() -> Empty.newBuilder().build());
  }
}
