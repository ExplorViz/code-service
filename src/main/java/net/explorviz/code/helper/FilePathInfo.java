package net.explorviz.code.helper;

/**
 * Helper class that takes a dot-seperated fqn and returns an object to access folders and filename,
 * respectively.
 */
public class FilePathInfo {

  private final String folders;
  private final String fileName;

  private FilePathInfo(Builder builder) {
    this.folders = builder.folders;
    this.fileName = builder.fileName;
  }

  public String getFoldersWithDotSeparation() {
    return folders;
  }

  public String getFileNameWithFileExtension() {
    return fileName;
  }

  @Override
  public String toString() {
    return "FileNameInfo{folders='" + folders + "', fileName='" + fileName + "'}";
  }

  private static class Builder {

    private String folders;
    private String fileName;

    private Builder withFolders(String folders) {
      this.folders = folders;
      return this;
    }

    private Builder withFileName(String fileName) {
      this.fileName = fileName;
      return this;
    }

    private FilePathInfo build() {
      return new FilePathInfo(this);
    }
  }

  /**
   * Static factory method for parsing.
   *
   * @param fqFileName Dot-seperated fqn
   * @return FilePathInfo
   */
  public static FilePathInfo build(String fqFileName) {
    int lastDotIndex = fqFileName.lastIndexOf('.');
    if (lastDotIndex == -1) {
      return null; // No dot found, so invalid input
    }

    int secondLastDotIndex = fqFileName.lastIndexOf('.', lastDotIndex - 1);
    if (secondLastDotIndex == -1) {
      return null; // Less than two segments, so invalid input
    }

    String fileName = fqFileName.substring(secondLastDotIndex + 1);
    String folders = fqFileName.substring(0, secondLastDotIndex);

    return new Builder()
        .withFolders(folders)
        .withFileName(fileName)
        .build();
  }
}

