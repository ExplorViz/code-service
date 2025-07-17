package net.explorviz.code.dto;

import java.util.Map;

/**
 * The class containing the metrics for every file of a commit.
 *
 * @param fileMetrics   Map file name (better: path) to metrics
 * @param classMetrics  Map full qualified name to metrics
 * @param methodMetrics Map fqn of containing class to metrics of method
 */
public record Metrics(Map<String, Map<String, String>> fileMetrics,
                      Map<String, Map<String, String>> classMetrics,
                      Map<String, Map<String, String>> methodMetrics) {

}
