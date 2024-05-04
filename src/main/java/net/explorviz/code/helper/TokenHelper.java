package net.explorviz.code.helper;

/**
 * Use this for the handling of the dummy token.
 */
public class TokenHelper {

  /**
   * Has to be used on the entry points, i.e., gRPC and HTTP, due to the way we handle the dummy
   * token.
   *
   * @param token The landscape token.
   * @return The same token value or the one for the dummy token.
   */
  public static String handlePotentialDummyToken(final String token) {
    String landscapeToken = "7cd8a9a7-b840-4735-9ef0-2dbbfa01c039";

    if (!"mytokenvalue".equals(token)) {
      landscapeToken = token;
    }
    return landscapeToken;
  }

}
