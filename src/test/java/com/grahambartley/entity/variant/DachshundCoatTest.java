package com.grahambartley.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DachshundCoatTest {

  @Test
  @DisplayName("fromOrdinal should map every valid ordinal to the expected coat")
  void testFromOrdinalValidValues() {
    final DachshundCoat[] values = DachshundCoat.values();
    for (int i = 0; i < values.length; i++) {
      assertEquals(values[i], DachshundCoat.fromOrdinal(i));
    }
  }

  @Test
  @DisplayName("fromOrdinal should default invalid ordinals to BLACK_TAN")
  void testFromOrdinalInvalidValues() {
    assertEquals(DachshundCoat.BLACK_TAN, DachshundCoat.fromOrdinal(-1));
    assertEquals(DachshundCoat.BLACK_TAN, DachshundCoat.fromOrdinal(999));
  }

  @Test
  @DisplayName("Texture prefixes should remain stable for resource lookup")
  void testTexturePrefixes() {
    final Map<DachshundCoat, String> expectedPrefixes =
        Map.ofEntries(
            Map.entry(DachshundCoat.BLACK_TAN, "blacktan"),
            Map.entry(DachshundCoat.RED, "red"),
            Map.entry(DachshundCoat.CHOCOLATE_TAN, "chocolatetan"),
            Map.entry(DachshundCoat.CHOCOLATE_CREAM, "chocolatecream"),
            Map.entry(DachshundCoat.BLACK_CREAM, "blackcream"),
            Map.entry(DachshundCoat.RED_PIEBALD, "redpiebald"),
            Map.entry(DachshundCoat.BLACK_TAN_PIEBALD, "blacktanpiebald"),
            Map.entry(DachshundCoat.BLUE_TAN, "bluetan"),
            Map.entry(DachshundCoat.ALBINO, "albino"));

    for (final DachshundCoat coat : DachshundCoat.values()) {
      assertEquals(expectedPrefixes.get(coat), coat.getTexturePrefix());
    }
  }

  @Test
  @DisplayName("getOrdinal should match enum ordinal for serialization")
  void testGetOrdinal() {
    for (final DachshundCoat coat : DachshundCoat.values()) {
      assertEquals(coat.ordinal(), coat.getOrdinal());
    }
  }
}
