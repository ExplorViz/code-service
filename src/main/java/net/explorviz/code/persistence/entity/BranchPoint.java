package net.explorviz.code.persistence.entity;

/**
 * A class for the very first commit of a branch.
 *
 * @param commitId              Commit Id
 * @param branchName            Branch Name
 * @param emergedFromBranchName Parent branch name
 * @param emergedFromCommitId   Parent commit Id
 * @param landscapeToken        The (initially user-given) landscape token.
 * @param applicationName       The (initially user-given) app name.
 */
public record BranchPoint(String commitId, String branchName, String emergedFromBranchName,
                          String emergedFromCommitId, String landscapeToken,
                          String applicationName) {

}
