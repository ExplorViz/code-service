package net.explorviz.code.dto.commit.tree;

import java.util.List;

public record CommitTree(String name, List<BranchDto> branches) {

}

