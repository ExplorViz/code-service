package net.explorviz.code.mongo;

import java.util.List;

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
  public String applicationName;

  public static BranchPoint findByTokenAndBranchName(final String landscapeToken, 
      final String branchName) {
    return find("landscapeToken = ?1 and branchName = ?2", landscapeToken, 
        branchName).firstResult();
  }

  public static List<BranchPoint> findByTokenAndApplicationName(final String landscapeToken, 
      final String applicationName) {
    return list("landscapeToken = ?1 and applicationName = ?2", landscapeToken, applicationName);
  }
}
