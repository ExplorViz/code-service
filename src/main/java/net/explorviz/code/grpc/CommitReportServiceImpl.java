package net.explorviz.code.grpc;

import com.google.protobuf.Empty;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import net.explorviz.code.analysis.CommitReportAnalysis;
import net.explorviz.code.proto.CommitReportData;
import net.explorviz.code.proto.CommitReportService;

/**
 * The basic implementation of the CommitReportService, handling CommitReportData packages.
 */
@GrpcService
public class CommitReportServiceImpl implements CommitReportService {

  @Inject
  /* package */ CommitReportAnalysis commitReportAnalysis; // NOCS

  @Override
  public Uni<Empty> sendCommitReport(final CommitReportData request) {
    commitReportAnalysis.processCommitReport(request);
    return Uni.createFrom().item(() -> Empty.newBuilder().build());
  }
}
