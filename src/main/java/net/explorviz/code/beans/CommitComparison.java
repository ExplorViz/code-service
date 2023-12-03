package net.explorviz.code.beans;

import java.util.List;

/**
 * ...
 */
public class CommitComparison {

  public List<String> added;
  public List<String> modified;
  public List<String> deleted;
  public List<String> missing;

  /**
   * ...
   ** @param added ...
   ** @param modified ...
   ** @param deleted ...
   ** @param missing ...
   */
  public CommitComparison(List<String> added, 
      List<String> modified, List<String> deleted, List<String> missing) {
    this.added = added;
    this.modified = modified;
    this.deleted = deleted;
    this.missing = missing;
  }

  public CommitComparison() {
  }
    
}
