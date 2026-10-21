package com.grahambartley.dogsunleashed.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class GermanShepherdCoatTest {

  static Stream<Arguments> texturePrefixes() {
    return Stream.of(
        Arguments.of(GermanShepherdCoat.SADDLE, "saddle", "saddle"),
        Arguments.of(GermanShepherdCoat.SABLE, "sable", "sable"),
        Arguments.of(GermanShepherdCoat.BLACK, "black", "black"));
  }

  @ParameterizedTest(name = "{0} -> germanshepherd_{1}.png")
  @MethodSource("texturePrefixes")
  @DisplayName("each coat names its texture and translation the way the assets do")
  void coatNamesItsTextureAndTranslation(
      final GermanShepherdCoat coat, final String prefix, final String key) {
    assertEquals(prefix, coat.getTexturePrefix());
    assertEquals("coat.dogs-unleashed.germanshepherd." + key, coat.translationKey());
  }

  @ParameterizedTest
  @EnumSource(GermanShepherdCoat.class)
  @DisplayName("every coat round-trips through its ordinal")
  void coatRoundTripsThroughOrdinal(final GermanShepherdCoat coat) {
    assertEquals(coat, GermanShepherdCoat.fromOrdinal(coat.getOrdinal()));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 3, 99})
  @DisplayName("an out of range ordinal falls back to saddle")
  void outOfRangeOrdinalFallsBack(final int ordinal) {
    assertEquals(GermanShepherdCoat.SADDLE, GermanShepherdCoat.fromOrdinal(ordinal));
  }
}
