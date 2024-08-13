package net.explorviz.code.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.explorviz.code.persistence.CommitReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class CommitComparisonHelperTest {

  private static final String LANDSCAPE_TOKEN = "landscape123";
  private static final String APPLICATION_NAME = "MyApp";
  private static final String COMMIT_ID_1 = "commit1";
  private static final String COMMIT_ID_2 = "commit2";

  private CommitReport commitReport1;
  private CommitReport commitReport2;

  @BeforeEach
  public void setUp() {
    PanacheMock.mock(CommitReport.class);
    commitReport1 = Mockito.mock(CommitReport.class);
    commitReport2 = Mockito.mock(CommitReport.class);

    when(CommitReport.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN, APPLICATION_NAME,
        COMMIT_ID_1)).thenReturn(commitReport1);
    when(CommitReport.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN, APPLICATION_NAME,
        COMMIT_ID_2)).thenReturn(commitReport2);
  }

  // Added cases
  @Test
  public void testGetComparisonAddedFiles_WithDifferentFiles() {
    when(commitReport1.getFiles()).thenReturn(Arrays.asList("file1", "file2", "file3"));
    when(commitReport2.getFiles()).thenReturn(Arrays.asList("file2", "file4"));

    List<String> addedFiles =
        CommitComparisonHelper.getComparisonAddedFiles(COMMIT_ID_1, COMMIT_ID_2, LANDSCAPE_TOKEN,
            APPLICATION_NAME);

    assertEquals(Arrays.asList("file4"), addedFiles);
  }

  @Test
  public void testGetComparisonAddedFiles_WithSameFiles() {

    when(CommitReport.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN, APPLICATION_NAME,
        COMMIT_ID_1)).thenReturn(commitReport1);
    when(CommitReport.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN, APPLICATION_NAME,
        COMMIT_ID_2)).thenReturn(commitReport2);

    when(commitReport1.getFiles()).thenReturn(Arrays.asList("file1", "file2"));
    when(commitReport2.getFiles()).thenReturn(Arrays.asList("file1", "file2"));

    List<String> addedFiles =
        CommitComparisonHelper.getComparisonAddedFiles(COMMIT_ID_1, COMMIT_ID_2, LANDSCAPE_TOKEN,
            APPLICATION_NAME);

    assertEquals(Collections.emptyList(), addedFiles);
  }

  @Test
  public void testGetComparisonAddedFiles_WithOneCommitNull() {
    when(CommitReport.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN, APPLICATION_NAME,
        COMMIT_ID_1))
        .thenReturn(null);

    List<String> addedFiles =
        CommitComparisonHelper.getComparisonAddedFiles(COMMIT_ID_1, COMMIT_ID_2, LANDSCAPE_TOKEN,
            APPLICATION_NAME);

    assertEquals(Collections.emptyList(), addedFiles);
  }

  @Test
  public void testGetComparisonAddedFiles_WithBothCommitsNull() {
    when(CommitReport.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN, APPLICATION_NAME,
        COMMIT_ID_1))
        .thenReturn(null);
    when(CommitReport.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN, APPLICATION_NAME,
        COMMIT_ID_2))
        .thenReturn(null);

    List<String> addedFiles =
        CommitComparisonHelper.getComparisonAddedFiles(COMMIT_ID_1, COMMIT_ID_2, LANDSCAPE_TOKEN,
            APPLICATION_NAME);

    assertEquals(Collections.emptyList(), addedFiles);
  }

  // Deleted cases

  @Test
  public void testGetComparisonDeletedFiles_WithDifferentFiles() {
    when(commitReport1.getFiles()).thenReturn(Arrays.asList("file1", "file2", "file3"));
    when(commitReport2.getFiles()).thenReturn(Arrays.asList("file2", "file4"));

    List<String> deletedFiles =
        CommitComparisonHelper.getComparisonDeletedFiles(COMMIT_ID_1, COMMIT_ID_2, LANDSCAPE_TOKEN,
            APPLICATION_NAME);

    assertEquals(Arrays.asList("file1", "file3"), deletedFiles);
  }

  @Test
  public void testGetComparisonDeletedFiles_WithSameFiles() {

    when(CommitReport.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN, APPLICATION_NAME,
        COMMIT_ID_1)).thenReturn(commitReport1);
    when(CommitReport.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN, APPLICATION_NAME,
        COMMIT_ID_2)).thenReturn(commitReport2);

    when(commitReport1.getFiles()).thenReturn(Arrays.asList("file1", "file2"));
    when(commitReport2.getFiles()).thenReturn(Arrays.asList("file1", "file2"));

    List<String> deletedFiles =
        CommitComparisonHelper.getComparisonDeletedFiles(COMMIT_ID_1, COMMIT_ID_2, LANDSCAPE_TOKEN,
            APPLICATION_NAME);

    assertEquals(Collections.emptyList(), deletedFiles);
  }

  @Test
  public void testGetComparisonDeletedFiles_WithOneCommitNull() {
    when(CommitReport.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN, APPLICATION_NAME,
        COMMIT_ID_1))
        .thenReturn(null);

    List<String> deletedFiles =
        CommitComparisonHelper.getComparisonDeletedFiles(COMMIT_ID_1, COMMIT_ID_2, LANDSCAPE_TOKEN,
            APPLICATION_NAME);

    assertEquals(Collections.emptyList(), deletedFiles);
  }

  @Test
  public void testGetComparisonDeletedFiles_WithBothCommitsNull() {
    when(CommitReport.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN, APPLICATION_NAME,
        COMMIT_ID_1))
        .thenReturn(null);
    when(CommitReport.findByTokenAndApplicationNameAndCommitId(LANDSCAPE_TOKEN, APPLICATION_NAME,
        COMMIT_ID_2))
        .thenReturn(null);

    List<String> deletedFiles =
        CommitComparisonHelper.getComparisonDeletedFiles(COMMIT_ID_1, COMMIT_ID_2, LANDSCAPE_TOKEN,
            APPLICATION_NAME);

    assertEquals(Collections.emptyList(), deletedFiles);
  }

}
