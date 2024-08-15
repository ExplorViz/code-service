package net.explorviz.code.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.explorviz.code.persistence.entity.CommitReport;
import net.explorviz.code.persistence.repository.BranchPointRepository;
import net.explorviz.code.persistence.repository.CommitReportRepository;
import net.explorviz.code.persistence.repository.LatestCommitRepository;
import net.explorviz.code.testhelper.HelperMethods;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class CommitComparisonHelperTest {

  @Inject
  LatestCommitRepository latestCommitRepository;

  @Inject
  BranchPointRepository branchPointRepository;

  CommitComparisonHelper commitComparisonHelper;

  CommitReportRepository commitReportRepository;

  private static final String LANDSCAPE_TOKEN = "landscape123";
  private static final String APPLICATION_NAME = "MyApp";
  private static final String COMMIT_ID_1 = "commit1";
  private static final String COMMIT_ID_2 = "commit2";

  private CommitReport commitReport1;
  private CommitReport commitReport2;

  @BeforeEach
  public void setUp() throws IOException {

    // Cannot mock record, therefore use real instance as Mock parameter
    // However, we should be able to mock it as mockito-inline should be in this version
    // of Mockito
    final CommitReport commitReport = HelperMethods.convertCommitReportGrpcToMongo(
        HelperMethods.readJsonAndConvertGrpcCommitReportData(
            "src/test/resources/CommitReport-1.json"));

    //PanacheMock.mock(CommitReport.class);
    this.commitReport1 = Mockito.mock(CommitReport.class);
    this.commitReport2 = Mockito.mock(CommitReport.class);

    this.commitReportRepository = Mockito.mock(CommitReportRepository.class);

    when(this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN,
        APPLICATION_NAME,
        COMMIT_ID_1)).thenReturn(commitReport1);
    when(this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN,
        APPLICATION_NAME,
        COMMIT_ID_2)).thenReturn(commitReport2);

    this.commitComparisonHelper =
        new CommitComparisonHelper(latestCommitRepository, branchPointRepository,
            this.commitReportRepository);
  }

  // Added cases
  @Test
  public void testGetComparisonAddedFiles_WithDifferentFiles() {
    when(commitReport1.files()).thenReturn(Arrays.asList("file1", "file2", "file3"));
    when(commitReport2.files()).thenReturn(Arrays.asList("file2", "file4"));

    List<String> addedFiles =
        this.commitComparisonHelper.getComparisonAddedFiles(COMMIT_ID_1, COMMIT_ID_2,
            LANDSCAPE_TOKEN,
            APPLICATION_NAME);

    assertEquals(Arrays.asList("file4"), addedFiles);
  }

  @Test
  public void testGetComparisonAddedFiles_WithSameFiles() {

    when(this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN,
        APPLICATION_NAME,
        COMMIT_ID_1)).thenReturn(commitReport1);
    when(this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN,
        APPLICATION_NAME,
        COMMIT_ID_2)).thenReturn(commitReport2);

    when(commitReport1.files()).thenReturn(Arrays.asList("file1", "file2"));
    when(commitReport2.files()).thenReturn(Arrays.asList("file1", "file2"));

    List<String> addedFiles =
        this.commitComparisonHelper.getComparisonAddedFiles(COMMIT_ID_1, COMMIT_ID_2,
            LANDSCAPE_TOKEN,
            APPLICATION_NAME);

    assertEquals(Collections.emptyList(), addedFiles);
  }

  @Test
  public void testGetComparisonAddedFiles_WithOneCommitNull() {
    when(this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN,
        APPLICATION_NAME,
        COMMIT_ID_1))
        .thenReturn(null);

    List<String> addedFiles =
        this.commitComparisonHelper.getComparisonAddedFiles(COMMIT_ID_1, COMMIT_ID_2,
            LANDSCAPE_TOKEN,
            APPLICATION_NAME);

    assertEquals(Collections.emptyList(), addedFiles);
  }

  @Test
  public void testGetComparisonAddedFiles_WithBothCommitsNull() {
    when(this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN,
        APPLICATION_NAME,
        COMMIT_ID_1))
        .thenReturn(null);
    when(this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN,
        APPLICATION_NAME,
        COMMIT_ID_2))
        .thenReturn(null);

    List<String> addedFiles =
        this.commitComparisonHelper.getComparisonAddedFiles(COMMIT_ID_1, COMMIT_ID_2,
            LANDSCAPE_TOKEN,
            APPLICATION_NAME);

    assertEquals(Collections.emptyList(), addedFiles);
  }

  // Deleted cases

  @Test
  public void testGetComparisonDeletedFiles_WithDifferentFiles() {
    when(commitReport1.files()).thenReturn(Arrays.asList("file1", "file2", "file3"));
    when(commitReport2.files()).thenReturn(Arrays.asList("file2", "file4"));

    List<String> deletedFiles =
        this.commitComparisonHelper.getComparisonDeletedFiles(COMMIT_ID_1, COMMIT_ID_2,
            LANDSCAPE_TOKEN,
            APPLICATION_NAME);

    assertEquals(Arrays.asList("file1", "file3"), deletedFiles);
  }

  @Test
  public void testGetComparisonDeletedFiles_WithSameFiles() {

    when(this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN,
        APPLICATION_NAME,
        COMMIT_ID_1)).thenReturn(commitReport1);
    when(this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN,
        APPLICATION_NAME,
        COMMIT_ID_2)).thenReturn(commitReport2);

    when(commitReport1.files()).thenReturn(Arrays.asList("file1", "file2"));
    when(commitReport2.files()).thenReturn(Arrays.asList("file1", "file2"));

    List<String> deletedFiles =
        this.commitComparisonHelper.getComparisonDeletedFiles(COMMIT_ID_1, COMMIT_ID_2,
            LANDSCAPE_TOKEN,
            APPLICATION_NAME);

    assertEquals(Collections.emptyList(), deletedFiles);
  }

  @Test
  public void testGetComparisonDeletedFiles_WithOneCommitNull() {
    when(this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN,
        APPLICATION_NAME,
        COMMIT_ID_1))
        .thenReturn(null);

    List<String> deletedFiles =
        this.commitComparisonHelper.getComparisonDeletedFiles(COMMIT_ID_1, COMMIT_ID_2,
            LANDSCAPE_TOKEN,
            APPLICATION_NAME);

    assertEquals(Collections.emptyList(), deletedFiles);
  }

  @Test
  public void testGetComparisonDeletedFiles_WithBothCommitsNull() {
    when(this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN,
        APPLICATION_NAME,
        COMMIT_ID_1))
        .thenReturn(null);
    when(this.commitReportRepository.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN,
        APPLICATION_NAME,
        COMMIT_ID_2))
        .thenReturn(null);

    List<String> deletedFiles =
        this.commitComparisonHelper.getComparisonDeletedFiles(COMMIT_ID_1, COMMIT_ID_2,
            LANDSCAPE_TOKEN,
            APPLICATION_NAME);

    assertEquals(Collections.emptyList(), deletedFiles);
  }

}
