package net.explorviz.code.mongo;

import io.quarkus.mongodb.panache.PanacheMongoEntity;

/**
 * ...
 */
public class LatestCommit extends PanacheMongoEntity {
  public String commitId;
  public String branchName;
  public String landscapeToken;

  public static LatestCommit findByBranchNameAndLandscapeToken(final String branchName, 
      final String landscapeToken) {
    return find("branchName = ?1 and landscapeToken = ?2", branchName, landscapeToken)
        .firstResult();
  }
}
