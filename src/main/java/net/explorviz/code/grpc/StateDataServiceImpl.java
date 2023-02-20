package net.explorviz.code.grpc;

import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import javax.inject.Inject;
import net.explorviz.code.kafka.KafkaGateway;
import net.explorviz.code.proto.StateData;
import net.explorviz.code.proto.StateDataRequest;
import net.explorviz.code.proto.StateDataService;

@GrpcService
public class StateDataServiceImpl implements StateDataService {

  @Inject
  /* package */ KafkaGateway kafkaGateway; // NOCS

  @Override
  public Uni<StateData> requestStateData(StateDataRequest request) {
    StateData.Builder stateDataBuilder = StateData.newBuilder();
    stateDataBuilder.setBranchName(request.getBranchName());
    stateDataBuilder.setCommitID(kafkaGateway.processStateData(request));
    return Uni.createFrom().item(stateDataBuilder.build());
  }
}
