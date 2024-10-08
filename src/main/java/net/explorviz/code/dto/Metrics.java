package net.explorviz.code.dto;

import java.util.List;
import java.util.Map;

/**
 * The class containing the metrics for every file of a commit.
 *
 * @param files         list of fq filenames
 * @param fileMetrics   todo
 * @param classMetrics  todo
 * @param methodMetrics todo
 */
public record Metrics(List<String> files, List<Map<String, String>> fileMetrics,
                      List<Map<String, Map<String, String>>> classMetrics,
                      List<Map<String, Map<String, String>>> methodMetrics) {

}
