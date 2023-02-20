package net.explorviz.code.grpc;

import com.google.protobuf.Empty;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import javax.inject.Inject;
import net.explorviz.code.kafka.KafkaGateway;
import net.explorviz.code.proto.CommitReportData;
import net.explorviz.code.proto.CommitReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class CommitReportServiceImpl implements CommitReportService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommitReportServiceImpl.class);

  @Inject
  /* package */ KafkaGateway kafkaGateway; // NOCS

  @Override
  public Uni<Empty> sendCommitReport(CommitReportData request) {
    kafkaGateway.processCommitReport(request);
    return Uni.createFrom().item(() -> Empty.newBuilder().build());
  }
}
