package net.explorviz.code.beans;

import java.util.List;
import java.util.Map;

/**
 * ...
 */
public class Metrics {

  private List<String> files;
  private List<Map<String, String>> fileMetrics;
  private List<Map<String, Map<String, String>>> classMetrics;
  private List<Map<String, Map<String, String>>> methodMetrics;


  public List<String> getFiles() {
    return this.files;
  }

  public void setFiles(final List<String> files) {
    this.files = files;
  }

  public List<Map<String, String>> getFileMetrics() {
    return this.fileMetrics;
  }

  public void setFileMetrics(final List<Map<String, String>> fileMetrics) {
    this.fileMetrics = fileMetrics;
  }

  public List<Map<String, Map<String, String>>> getClassMetrics() {
    return this.classMetrics;
  }

  public void setClassMetrics(final List<Map<String, Map<String, String>>> classMetrics) {
    this.classMetrics = classMetrics;
  }

  public List<Map<String, Map<String, String>>> getMethodMetrics() {
    return this.methodMetrics;
  }

  public void setMethodMetrics(final List<Map<String, Map<String, String>>> methodMetrics) {
    this.methodMetrics = methodMetrics;
  }
    
}
