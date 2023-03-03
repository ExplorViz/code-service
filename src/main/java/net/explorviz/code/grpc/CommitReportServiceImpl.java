package net.explorviz.code.grpc;

import com.google.protobuf.Empty;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import javax.inject.Inject;
import net.explorviz.code.kafka.KafkaGateway;
import net.explorviz.code.proto.CommitReportData;
import net.explorviz.code.proto.CommitReportService;

/**
 * The basic implementation of the CommitReportService, handling CommitReportData packages.
 */
@GrpcService
public class CommitReportServiceImpl implements CommitReportService {

  @Inject
  /* package */ KafkaGateway kafkaGateway; // NOCS

  @Override
  public Uni<Empty> sendCommitReport(final CommitReportData request) {
    kafkaGateway.processCommitReport(request);
    return Uni.createFrom().item(() -> Empty.newBuilder().build());
  }
}
