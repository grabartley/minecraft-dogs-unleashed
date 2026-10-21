package com.grahambartley.dogsunleashed.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class PinscherCoatTest {

  static Stream<Arguments> coatNames() {
    return Stream.of(
        Arguments.of(PinscherCoat.BLACK_TAN, "blacktan", "black_tan"),
        Arguments.of(PinscherCoat.RED, "red", "red"),
        Arguments.of(PinscherCoat.CHOCOLATE_TAN, "chocolatetan", "chocolate_tan"));
  }

  @ParameterizedTest(name = "{0} -> pinscher_{1}.png")
  @MethodSource("coatNames")
  @DisplayName("each coat names its texture and translation the way the assets do")
  void coatNamesItsTextureAndTranslation(
      final PinscherCoat coat, final String prefix, final String translationSuffix) {
    assertEquals(prefix, coat.getTexturePrefix());
    assertEquals("coat.dogs-unleashed.pinscher." + translationSuffix, coat.translationKey());
  }

  @ParameterizedTest
  @EnumSource(PinscherCoat.class)
  @DisplayName("every coat round-trips through its ordinal")
  void coatRoundTripsThroughOrdinal(final PinscherCoat coat) {
    assertEquals(coat, PinscherCoat.fromOrdinal(coat.getOrdinal()));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 3, 99})
  @DisplayName("an out of range ordinal falls back to black and tan")
  void outOfRangeOrdinalFallsBack(final int ordinal) {
    assertEquals(PinscherCoat.BLACK_TAN, PinscherCoat.fromOrdinal(ordinal));
  }
}
