package net.explorviz.code.grpc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Empty;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import net.explorviz.code.analysis.FileDataAnalysis;
import net.explorviz.code.persistence.entity.FileReportTable;
import net.explorviz.code.persistence.repository.FileReportTableRepository;
import net.explorviz.code.proto.FileData;
import net.explorviz.code.proto.FileRequest;
import net.explorviz.code.proto.FileResponse;
import net.explorviz.code.proto.FileDataService;

/**
 * The basic implementation of the FileDataService, handling FileData packages.
 */
@GrpcService
public class FileDataServiceImpl implements FileDataService {

  @Inject
  /* package */ FileDataAnalysis fileDataAnalysis; // NOCS

  @Inject 
  FileReportTableRepository fileReportTableRepository; // NOCS

  @Override
  public Uni<Empty> sendFileData(final FileData request) {
    fileDataAnalysis.processFileData(request);
    return Uni.createFrom().item(() -> Empty.newBuilder().build());
  }

  @Override
    public Uni<FileResponse> getFileNames(FileRequest request) {
        String commitId = request.getCommitID();
        String landscapeToken = request.getLandscapeToken();
        String applicationName = request.getApplicationName();

        final FileReportTable filreReportTable = fileReportTableRepository.findByTokenAndAppName(landscapeToken, applicationName);
        Map<String, String> fileNamesToCommitIdMap = new HashMap<>();
        if(filreReportTable != null) {
          fileNamesToCommitIdMap = filreReportTable.getCommitIdTofqnFileNameToCommitIdMap()
                .getOrDefault(commitId, new HashMap<>());
        }

        List<String> fileNames = fileNamesToCommitIdMap.keySet().stream().toList();

        FileResponse response = FileResponse.newBuilder()
                .addAllFileName(fileNames)
                .build();

        return Uni.createFrom().item(response);
    }
}
