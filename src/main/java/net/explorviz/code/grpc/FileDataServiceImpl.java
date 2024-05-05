package net.explorviz.code.grpc;

import com.google.protobuf.Empty;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import net.explorviz.code.proto.FileData;
import net.explorviz.code.proto.FileDataService;

/**
 * The basic implementation of the FileDataService, handling FileData packages.
 */
@GrpcService
public class FileDataServiceImpl implements FileDataService {

  @Inject
  /* package */ GrpcGateway grpcGateway; // NOCS

  @Override
  public Uni<Empty> sendFileData(final FileData request) {
    grpcGateway.processFileData(request);
    return Uni.createFrom().item(() -> Empty.newBuilder().build());
  }
}
