package net.explorviz.code.beans;

import java.util.List;

/**
 * ...
 */
public class CommitComparison {

  public List<String> added;
  public List<String> modified;
  public List<String> deleted;

  /**
   * ...
   ** @param added ...
   ** @param modified ...
   ** @param deleted ...
   **
   */
  public CommitComparison(List<String> added, 
      List<String> modified, List<String> deleted) {
    this.added = added;
    this.modified = modified;
    this.deleted = deleted;
  }

  public CommitComparison() {
  }
    
}
