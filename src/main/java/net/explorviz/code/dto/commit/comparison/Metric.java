package net.explorviz.code.dto.commit.comparison;

import java.util.Map;

public class Metric {

  private String entityName;
  private Map<String, MetricValueComparison> metricMap;

  public String getEntityName() {
    return this.entityName;
  }

  public void setEntityName(final String entityName) {
    this.entityName = entityName;
  }

  public Map<String, MetricValueComparison> getMetricMap() {
    return this.metricMap;
  }

  public void setMetricMap(final Map<String, MetricValueComparison> metricMap) {
    this.metricMap = metricMap;
  }
}
