package net.explorviz.code.persistence.entity;

/**
 * The class for the most recent commit of a branch w.r.t the application and landscape token.
 */
public record LatestCommit(String commitId, String branchName, String landscapeToken,
                           String applicationName) {

}
