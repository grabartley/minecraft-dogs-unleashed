package com.grahambartley.dogsunleashed.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class DachshundCoatTest {

  @ParameterizedTest(name = "{0} round-trips through ordinal {0}")
  @EnumSource(DachshundCoat.class)
  @DisplayName("fromOrdinal returns the coat with the matching ordinal")
  void fromOrdinalReturnsCoatWithMatchingOrdinal(final DachshundCoat coat) {
    assertEquals(coat, DachshundCoat.fromOrdinal(coat.ordinal()));
  }

  @ParameterizedTest(name = "ordinal {0}")
  @ValueSource(ints = {-1, -100, Integer.MIN_VALUE, 100, Integer.MAX_VALUE})
  @DisplayName("fromOrdinal falls back to BLACK_TAN for out-of-range ordinals")
  void fromOrdinalFallsBackToBlackTanOnOutOfRange(final int ordinal) {
    assertEquals(DachshundCoat.BLACK_TAN, DachshundCoat.fromOrdinal(ordinal));
  }

  static Stream<Arguments> texturePrefixes() {
    return Stream.of(
        Arguments.of(DachshundCoat.BLACK_TAN, "blacktan"),
        Arguments.of(DachshundCoat.RED, "red"),
        Arguments.of(DachshundCoat.CHOCOLATE_TAN, "chocolatetan"),
        Arguments.of(DachshundCoat.CHOCOLATE_CREAM, "chocolatecream"),
        Arguments.of(DachshundCoat.BLACK_CREAM, "blackcream"),
        Arguments.of(DachshundCoat.RED_PIEBALD, "redpiebald"),
        Arguments.of(DachshundCoat.BLACK_TAN_PIEBALD, "blacktanpiebald"),
        Arguments.of(DachshundCoat.BLUE_TAN, "bluetan"),
        Arguments.of(DachshundCoat.ALBINO, "albino"),
        Arguments.of(DachshundCoat.LIGHT_BLACK_CREAM, "lightblackcream"));
  }

  @ParameterizedTest(name = "{0} -> \"{1}\"")
  @MethodSource("texturePrefixes")
  @DisplayName("getTexturePrefix returns the resource-lookup name for each coat")
  void getTexturePrefixReturnsExpectedName(final DachshundCoat coat, final String expected) {
    assertEquals(expected, coat.getTexturePrefix());
  }

  @ParameterizedTest
  @EnumSource(DachshundCoat.class)
  @DisplayName("getOrdinal returns the enum ordinal for serialization")
  void getOrdinalMatchesEnumOrdinal(final DachshundCoat coat) {
    assertEquals(coat.ordinal(), coat.getOrdinal());
  }
}
