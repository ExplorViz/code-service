package net.explorviz.code.grpc;

import com.google.protobuf.Empty;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import javax.inject.Inject;
import net.explorviz.code.kafka.KafkaGateway;
import net.explorviz.code.proto.FileData;
import net.explorviz.code.proto.FileDataService;

/**
 * The basic implementation of the FileDataService, handling FileData packages.
 */
@GrpcService
public class FileDataServiceImpl implements FileDataService {

  @Inject
  /* package */ KafkaGateway kafkaGateway; // NOCS

  @Override
  public Uni<Empty> sendFileData(final FileData request) {
    kafkaGateway.processFileData(request);
    return Uni.createFrom().item(() -> Empty.newBuilder().build());
  }
}
