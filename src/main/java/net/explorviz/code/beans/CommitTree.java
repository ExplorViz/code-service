package net.explorviz.code.beans;

import java.util.List;

/**
 * ...
 */
public class CommitTree {
  
  public String name;
  public List<Branch> branches;

  /**
   * ...
   */
  public static class Branch {
    public String name;
    public List<String> commits;
    public BranchPoint2 branchPoint;

    /**
     * ...
     ** @param name .
     ** @param commits .
     * *@param branchPoint .
     */
    public Branch(String name, List<String> commits, BranchPoint2 branchPoint) {
      this.name = name;
      this.commits = commits;
      this.branchPoint = branchPoint;
    }
       
    /**
     * ...
     */
    public static class BranchPoint2 {
      public String name;
      public String commit;

      public BranchPoint2(final String name, final String commit) {
        this.name = name;
        this.commit = commit;
      }
    }

  }

  public CommitTree() {
  }

  public CommitTree(String name, List<Branch> branches) {
    this.name = name;
    this.branches = branches;
  }
}

