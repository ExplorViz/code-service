package net.explorviz.code.dto.commit.tree;

import java.util.List;

public class BranchDto {

  private String name;
  private List<String> commits;
  private BranchPointDto branchPoint;

  public BranchDto(final String name, final List<String> commits,
      final BranchPointDto branchPoint) {
    this.name = name;
    this.commits = commits;
    this.branchPoint = branchPoint;
  }

  public String getName() {
    return this.name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public List<String> getCommits() {
    return this.commits;
  }

  public void setCommits(final List<String> commits) {
    this.commits = commits;
  }

  public BranchPointDto getBranchPoint() {
    return this.branchPoint;
  }

  public void setBranchPoint(final BranchPointDto branchPoint) {
    this.branchPoint = branchPoint;
  }

}
