package net.explorviz.code.dto.commit.comparison;

import java.util.Map;

public record Metric(String entityName, Map<String, MetricValueComparison> metricMap) {
}
