package com.grahambartley.dogsunleashed.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class DireWolfCoatTest {

  static Stream<Arguments> texturePrefixes() {
    return Stream.of(
        Arguments.of(DireWolfCoat.SMOKEGREY, "smokegrey"),
        Arguments.of(DireWolfCoat.ASHGREY, "ashgrey"),
        Arguments.of(DireWolfCoat.FROSTGREY, "frostgrey"),
        Arguments.of(DireWolfCoat.SILVER, "silver"),
        Arguments.of(DireWolfCoat.BLACK, "black"),
        Arguments.of(DireWolfCoat.WHITE, "white"));
  }

  @ParameterizedTest(name = "{0} -> direwolf_{1}.png")
  @MethodSource("texturePrefixes")
  @DisplayName("each coat names its texture and translation the way the assets do")
  void coatNamesItsTextureAndTranslation(final DireWolfCoat coat, final String prefix) {
    assertEquals(prefix, coat.getTexturePrefix());
    assertEquals("coat.dogs-unleashed.direwolf." + prefix, coat.translationKey());
  }

  @ParameterizedTest
  @EnumSource(DireWolfCoat.class)
  @DisplayName("every coat round-trips through its ordinal")
  void coatRoundTripsThroughOrdinal(final DireWolfCoat coat) {
    assertEquals(coat, DireWolfCoat.fromOrdinal(coat.getOrdinal()));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 6, 99})
  @DisplayName("an out of range ordinal falls back to smoke grey")
  void outOfRangeOrdinalFallsBack(final int ordinal) {
    assertEquals(DireWolfCoat.SMOKEGREY, DireWolfCoat.fromOrdinal(ordinal));
  }
}
