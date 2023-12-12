package net.explorviz.code.beans;

import java.util.List;
import java.util.Map;

/**
 * ...
 */
public class CommitComparison {
 
  private List<String> added;
  private List<String> modified;
  private List<String> deleted;
  private List<Metric> metrics;

  /**
   * ...
   ** @param added ...
   ** @param modified ...
   ** @param deleted ...
   **
   */
  public CommitComparison(final List<String> added, 
      final List<String> modified, final List<String> deleted,
      final List<Metric> metrics) {
    this.added = added;
    this.modified = modified;
    this.deleted = deleted;
    this.metrics = metrics;
  }

  public CommitComparison() { // NO PMD
  }

  public List<String> getAdded() {
    return this.added;
  }

  public void setAdded(final List<String> added) {
    this.added = added;
  }

  public List<String> getModified() {
    return this.modified;
  }

  public void setModified(final List<String> modified) {
    this.modified = modified;
  }

  public List<String> getDeleted() {
    return this.deleted;
  }

  public void setDeleted(final List<String> deleted) {
    this.deleted = deleted;
  }

  public List<Metric> getMetrics() {
    return this.metrics;
  }

  public void setMetrics(final List<Metric> metrics) {
    this.metrics = metrics;
  }


  /**
   * ...
   */
  public static class Metric {

    private String entityName;
    private Map<String, MetricVal> metricMap;

    public String getEntityName() {
      return this.entityName;
    }

    public void setEntityName(final String entityName) {
      this.entityName = entityName;
    }

    public Map<String, MetricVal> getMetricMap() {
      return this.metricMap;
    }

    public void setMetricMap(final Map<String, MetricVal> metricMap) {
      this.metricMap = metricMap;
    }

    /**
     * ...
     */
    public static class MetricVal {

      private String oldValue;
      private String newValue;

      public String getOldValue() {
        return this.oldValue;
      }

      public void setOldValue(final String oldValue) {
        this.oldValue = oldValue;
      }

      public String getNewValue() {
        return this.newValue;
      }

      public void setNewValue(final String newValue) {
        this.newValue = newValue;
      }
    }
  }
}
