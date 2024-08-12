package net.explorviz.code.dto.commit.tree;

public class BranchPointDto {

  private String name;
  private String commit;

  public BranchPointDto(final String name, final String commit) {
    this.name = name;
    this.commit = commit;
  }

  public String getName() {
    return this.name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getCommit() {
    return this.commit;
  }

  public void setCommit(final String commit) {
    this.commit = commit;
  }


}
