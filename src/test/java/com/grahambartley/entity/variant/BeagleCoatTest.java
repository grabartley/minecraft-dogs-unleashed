package com.grahambartley.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class BeagleCoatTest {

  @ParameterizedTest(name = "{0} round-trips through ordinal {0}")
  @EnumSource(BeagleCoat.class)
  @DisplayName("fromOrdinal returns the coat with the matching ordinal")
  void fromOrdinalReturnsCoatWithMatchingOrdinal(final BeagleCoat coat) {
    assertEquals(coat, BeagleCoat.fromOrdinal(coat.ordinal()));
  }

  @ParameterizedTest(name = "ordinal {0}")
  @ValueSource(ints = {-1, -100, Integer.MIN_VALUE, 100, Integer.MAX_VALUE})
  @DisplayName("fromOrdinal falls back to TRI_1 for out-of-range ordinals")
  void fromOrdinalFallsBackToTri1OnOutOfRange(final int ordinal) {
    assertEquals(BeagleCoat.TRI_1, BeagleCoat.fromOrdinal(ordinal));
  }

  static Stream<Arguments> texturePrefixes() {
    return Stream.of(
        Arguments.of(BeagleCoat.TRI_1, "tri1"),
        Arguments.of(BeagleCoat.RED_1, "red1"),
        Arguments.of(BeagleCoat.TRI_2, "tri2"),
        Arguments.of(BeagleCoat.TRI_3, "tri3"),
        Arguments.of(BeagleCoat.RED_2, "red2"),
        Arguments.of(BeagleCoat.LEMON_1, "lemon1"),
        Arguments.of(BeagleCoat.BROWN_WHITE, "brownwhite"),
        Arguments.of(BeagleCoat.BROWN_WHITE_TAN, "brownwhitetan"),
        Arguments.of(BeagleCoat.LEMON_2, "lemon2"),
        Arguments.of(BeagleCoat.BLACK_WHITE, "blackwhite"),
        Arguments.of(BeagleCoat.LILAC_1, "lilac1"),
        Arguments.of(BeagleCoat.LILAC_2, "lilac2"));
  }

  @ParameterizedTest(name = "{0} -> \"{1}\"")
  @MethodSource("texturePrefixes")
  @DisplayName("getTexturePrefix returns the resource-lookup name for each coat")
  void getTexturePrefixReturnsExpectedName(final BeagleCoat coat, final String expected) {
    assertEquals(expected, coat.getTexturePrefix());
  }

  @ParameterizedTest
  @EnumSource(BeagleCoat.class)
  @DisplayName("getOrdinal returns the enum ordinal for serialization")
  void getOrdinalMatchesEnumOrdinal(final BeagleCoat coat) {
    assertEquals(coat.ordinal(), coat.getOrdinal());
  }
}
