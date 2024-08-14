package net.explorviz.code.persistence.entity;

/**
 * The class for the most recent commit of a branch w.r.t the application and landscape token.
 *
 * @param commitId        Name suggest.
 * @param branchName      Name suggest.
 * @param landscapeToken  The (initially user-given) landscape token.
 * @param applicationName The (initially user-given) app name.
 */
public record LatestCommit(String commitId, String branchName, String landscapeToken,
                           String applicationName) {

}
