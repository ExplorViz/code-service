package net.explorviz.code.analysis.util;

import java.util.List;

public final class FqnCalculator {

  private FqnCalculator() {
    // utility class
  }

  public static String calculateFqnBasedOnImport(final List<String> importNames,
      final String searchString) {
    for (final String s : importNames) {
      if (s.contains(searchString)) {
        return s + "." + searchString;
      }
    }
    return "unknown" + "." + searchString;
  }

}
