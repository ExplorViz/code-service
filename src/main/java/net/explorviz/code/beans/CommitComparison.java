package net.explorviz.code.beans;

import java.util.List;
import java.util.Map;

/**
 * ...
 */
public class CommitComparison {

  public List<String> added;
  public List<String> modified;
  public List<String> deleted;
  public List<Metric> metrics;

  public static class Metric {
    public String entityName;
    public Map<String, MetricVal> metricMap;

    public static class MetricVal {
      public String oldValue;
      public String newValue;
    }
  }

  /**
   * ...
   ** @param added ...
   ** @param modified ...
   ** @param deleted ...
   **
   */
  public CommitComparison(List<String> added, 
      List<String> modified, List<String> deleted,
      List<Metric> metrics) {
    this.added = added;
    this.modified = modified;
    this.deleted = deleted;
    this.metrics = metrics;
  }

  public CommitComparison() {
  }
    
}
