package net.explorviz.code.dto.commit.tree;

import java.util.List;

public record BranchDto(String name, List<String> commits, BranchPointDto branchPoint) {

}
