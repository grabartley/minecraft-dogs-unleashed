package com.grahambartley.dogsunleashed.render.costume;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.OptionalInt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CostumeTintTest {

  @Test
  @DisplayName("a costume that can't be dyed stays untinted")
  void undyeableStaysUntinted() {
    assertEquals(0xFFFFFFFF, CostumeTint.argb(OptionalInt.empty()));
  }

  @ParameterizedTest(name = "colour {0} -> {1}")
  @CsvSource({
    "0x112233, 0xFF112233",
    "0xFFFFFF, 0xFFFFFFFF",
    "0x0, 0xFF000000",
    "0x7F445566, 0xFF445566"
  })
  @DisplayName("a dyeable costume takes its colour fully opaque")
  void dyeableTakesItsColourOpaque(final String rgb, final String expected) {
    assertEquals(
        Integer.parseUnsignedInt(expected.substring(2), 16),
        CostumeTint.argb(OptionalInt.of(Integer.parseUnsignedInt(rgb.substring(2), 16))));
  }
}
