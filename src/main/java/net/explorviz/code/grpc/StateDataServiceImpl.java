package net.explorviz.code.grpc;

import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import net.explorviz.code.kafka.GrpcGateway;
import net.explorviz.code.proto.StateData;
import net.explorviz.code.proto.StateDataRequest;
import net.explorviz.code.proto.StateDataService;

/**
 * The basic implementation of the StateDataService, handling StateDataRequest packages.
 */
@GrpcService
public class StateDataServiceImpl implements StateDataService {

  @Inject
  /* package */ GrpcGateway kafkaGateway; // NOCS

  @Override
  public Uni<StateData> requestStateData(final StateDataRequest request) {
    final StateData.Builder stateDataBuilder = StateData.newBuilder();
    stateDataBuilder.setBranchName(request.getBranchName());
    stateDataBuilder.setCommitID(kafkaGateway.processStateData(request));
    return Uni.createFrom().item(stateDataBuilder.build());
  }
}
