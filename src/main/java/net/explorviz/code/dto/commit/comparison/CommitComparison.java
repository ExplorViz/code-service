package net.explorviz.code.dto.commit.comparison;

import java.util.List;

public record CommitComparison(
    List<String> added,
    List<String> modified,
    List<String> deleted,
    List<String> addedPackages,
    List<String> deletedPackages,
    List<Metric> metrics
) {

}
