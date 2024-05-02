package net.explorviz.code.main;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * Main method, mainly to profile the service in IntelliJ. Otherwise, use the Gradle tasks.
 */
@QuarkusMain
public class Main {

  public static void main(String... args) {
    Quarkus.run(args);
  }
}
