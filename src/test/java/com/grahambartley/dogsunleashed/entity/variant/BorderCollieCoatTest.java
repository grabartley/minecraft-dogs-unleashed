package com.grahambartley.dogsunleashed.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class BorderCollieCoatTest {

  static Stream<Arguments> texturePrefixes() {
    return Stream.of(
        Arguments.of(BorderCollieCoat.BLACK_WHITE, "blackwhite", "black_white"),
        Arguments.of(BorderCollieCoat.RED_MERLE, "redmerle", "red_merle"),
        Arguments.of(BorderCollieCoat.BLUE_MERLE, "bluemerle", "blue_merle"));
  }

  @ParameterizedTest(name = "{0} -> bordercollie_{1}.png")
  @MethodSource("texturePrefixes")
  @DisplayName("each coat names its texture and translation the way the assets do")
  void coatNamesItsTextureAndTranslation(
      final BorderCollieCoat coat, final String prefix, final String key) {
    assertEquals(prefix, coat.getTexturePrefix());
    assertEquals("coat.dogs-unleashed.bordercollie." + key, coat.translationKey());
  }

  @ParameterizedTest
  @EnumSource(BorderCollieCoat.class)
  @DisplayName("every coat round-trips through its ordinal")
  void coatRoundTripsThroughOrdinal(final BorderCollieCoat coat) {
    assertEquals(coat, BorderCollieCoat.fromOrdinal(coat.getOrdinal()));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 3, 99})
  @DisplayName("an out of range ordinal falls back to black and white")
  void outOfRangeOrdinalFallsBack(final int ordinal) {
    assertEquals(BorderCollieCoat.BLACK_WHITE, BorderCollieCoat.fromOrdinal(ordinal));
  }
}
