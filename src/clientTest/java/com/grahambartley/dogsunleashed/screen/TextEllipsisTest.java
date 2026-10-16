package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TextEllipsisTest {

  private static final ToIntFunction<String> ONE_PER_CHAR = String::length;

  static Stream<Arguments> cases() {
    return Stream.of(
        Arguments.of("fits exactly", "Husky", 5, "Husky"),
        Arguments.of("fits with room", "Husky", 20, "Husky"),
        Arguments.of("too long", "Golden Retriever-Husky Mix", 12, "Golden Re..."),
        Arguments.of("cut lands on a space", "Golden Retriever", 10, "Golden..."),
        Arguments.of("only room for the ellipsis", "Beagle", 3, "..."),
        Arguments.of("no room even for the ellipsis", "Beagle", 2, ""),
        Arguments.of("never splits an emoji", "Rex\uD83D\uDC36Buddy", 6, "Rex..."));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("cases")
  @DisplayName("text wider than the space is cut and ends in an ellipsis")
  void fitsTextToWidth(
      final String label, final String text, final int maxWidth, final String expected) {
    final String fitted = TextEllipsis.fit(text, maxWidth, ONE_PER_CHAR);
    assertEquals(expected, fitted);
    assertTrue(fitted.length() <= maxWidth);
  }
}
