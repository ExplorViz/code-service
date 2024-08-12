package net.explorviz.code.dto.commit.tree;

import java.util.List;

public class CommitTree {

  private String name;
  private List<BranchDto> branches;

  public CommitTree(final String name, final List<BranchDto> branches) {
    this.name = name;
    this.branches = branches;
  }

  public String getName() {
    return this.name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public List<BranchDto> getBranches() {
    return this.branches;
  }

  public void setBranches(final List<BranchDto> branches) {
    this.branches = branches;
  }


}

