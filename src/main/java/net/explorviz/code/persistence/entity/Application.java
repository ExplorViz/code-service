package net.explorviz.code.persistence.entity;

/**
 *  Entity class for applications that the code-agent analyzed.
 *
 * @param applicationName It's user-given name.
 * @param landscapeToken It's user-given token.
 */
public record Application(String applicationName, String landscapeToken) {


}
