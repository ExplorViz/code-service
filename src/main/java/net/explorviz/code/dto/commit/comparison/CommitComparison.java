package net.explorviz.code.dto.commit.comparison;

import java.util.List;
import java.util.Map;

public record CommitComparison(
    List<String> added,
    List<String> modified,
    List<String> deleted,
    List<String> addedPackages,
    List<String> deletedPackages,
    Map<String, Metric> metrics
) {
  
}
