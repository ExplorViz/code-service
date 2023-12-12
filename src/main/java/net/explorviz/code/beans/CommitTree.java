package net.explorviz.code.beans;

import java.util.List;

/**
 * ...
 */
public class CommitTree {

  private String name;
  private List<Branch> branches;

  public CommitTree() { // NO PMD
  }

  public CommitTree(final String name, final List<Branch> branches) {
    this.name = name;
    this.branches = branches;
  }

  public String getName() {
    return this.name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public List<Branch> getBranches() {
    return this.branches;
  }

  public void setBranches(final List<Branch> branches) {
    this.branches = branches;
  }

  /**
   * ...
   */
  public static class Branch {
    private String name;
    private List<String> commits;
    private BranchPoint2 branchPoint;

    /**
     * ...
     ** @param name .
     ** @param commits .
     * *@param branchPoint .
     */
    public Branch(final String name, final List<String> commits, final BranchPoint2 branchPoint) {
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

    public BranchPoint2 getBranchPoint() {
      return this.branchPoint;
    }

    public void setBranchPoint(final BranchPoint2 branchPoint) {
      this.branchPoint = branchPoint;
    }

       
    /**
     * ...
     */
    public static class BranchPoint2 {
      private String name;
      private String commit;

      public BranchPoint2(final String name, final String commit) {
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

  }

}

