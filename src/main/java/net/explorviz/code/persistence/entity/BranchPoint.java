package net.explorviz.code.persistence.entity;

/**
 * A class for the very first commit of a branch.
 */
public record BranchPoint(String commitId, String branchName, String emergedFromBranchName,
                          String emergedFromCommitId, String landscapeToken,
                          String applicationName) {

}
