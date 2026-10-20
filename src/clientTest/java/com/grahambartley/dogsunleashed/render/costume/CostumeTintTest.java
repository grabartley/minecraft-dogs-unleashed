package com.grahambartley.dogsunleashed.render.costume;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CostumeTintTest {

  @ParameterizedTest(name = "dyeable={0}, colour {1} -> {2}")
  @CsvSource({
    "false, 0x112233, 0xFFFFFFFF",
    "false, 0x0, 0xFFFFFFFF",
    "true, 0x112233, 0xFF112233",
    "true, 0xFFFFFF, 0xFFFFFFFF",
    "true, 0x00000000, 0xFF000000",
    "true, 0x7F445566, 0xFF445566"
  })
  @DisplayName("a dyeable costume takes its colour fully opaque, any other stays untinted")
  void tintsOnlyDyeableCostumes(final boolean dyeable, final String rgb, final String expected) {
    assertEquals(
        Integer.parseUnsignedInt(expected.substring(2), 16),
        CostumeTint.argb(dyeable, Integer.parseUnsignedInt(rgb.substring(2), 16)));
  }
}
