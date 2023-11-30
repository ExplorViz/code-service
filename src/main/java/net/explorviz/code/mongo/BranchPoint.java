package net.explorviz.code.mongo;

import io.quarkus.mongodb.panache.PanacheMongoEntity;

/**
 * ...
 */
public class BranchPoint extends PanacheMongoEntity {
  public String commitId;
  public String branchName;
  public String emergedFromBranchName;
  public String emergedFromCommitId;
  public String landscapeToken;

  public static BranchPoint findByBranchName(final String branchName) {
    return find("branchName", branchName).firstResult();
  }
}
