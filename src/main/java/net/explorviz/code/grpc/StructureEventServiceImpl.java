package net.explorviz.code.grpc;

import com.google.protobuf.Empty;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import net.explorviz.code.proto.StructureCreateEvent;
import net.explorviz.code.proto.StructureDeleteEvent;
import net.explorviz.code.proto.StructureEventService;
import net.explorviz.code.proto.StructureModifyEvent;

/**
 * GrpcService that listens to StructureEvents. Entrypoint for this service.
 */
@GrpcService
public class StructureEventServiceImpl implements StructureEventService {

  // grpcurl -plaintext localhost:9000 list
  // grpcurl -plaintext -d '{"name": "test"}' localhost:9000 helloworld.Greeter.SayHello
  // grpcurl -plaintext localhost:9000 describe net.explorviz.code.proto.StructureEventService
  // grpcurl -plaintext -d '{"className": "test"}' localhost:9000
  // net.explorviz.code.proto.StructureEventService.sendCreateEvent

  @Override
  public Uni<Empty> sendCreateEvent(final StructureCreateEvent request) {
    return Uni.createFrom().item(() -> Empty.newBuilder().build());
  }

  @Override
  public Uni<Empty> sendDeleteEvent(final StructureDeleteEvent request) {
    return Uni.createFrom().item(() -> Empty.newBuilder().build());
  }

  @Override
  public Uni<Empty> sendModifyEvent(final StructureModifyEvent request) {
    return Uni.createFrom().item(() -> Empty.newBuilder().build());
  }

}
