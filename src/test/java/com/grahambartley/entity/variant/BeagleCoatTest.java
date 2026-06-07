package com.grahambartley.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BeagleCoatTest {

  @Test
  @DisplayName("fromOrdinal should map every valid ordinal to the expected coat")
  void testFromOrdinalValidValues() {
    final BeagleCoat[] values = BeagleCoat.values();
    for (int i = 0; i < values.length; i++) {
      assertEquals(values[i], BeagleCoat.fromOrdinal(i));
    }
  }

  @Test
  @DisplayName("fromOrdinal should default invalid ordinals to TRI_1")
  void testFromOrdinalInvalidValues() {
    assertEquals(BeagleCoat.TRI_1, BeagleCoat.fromOrdinal(-1));
    assertEquals(BeagleCoat.TRI_1, BeagleCoat.fromOrdinal(999));
  }

  @Test
  @DisplayName("Texture prefixes should remain stable for resource lookup")
  void testTexturePrefixes() {
    final Map<BeagleCoat, String> expectedPrefixes =
        Map.ofEntries(
            Map.entry(BeagleCoat.TRI_1, "tri1"),
            Map.entry(BeagleCoat.RED_1, "red1"),
            Map.entry(BeagleCoat.TRI_2, "tri2"),
            Map.entry(BeagleCoat.TRI_3, "tri3"),
            Map.entry(BeagleCoat.RED_2, "red2"),
            Map.entry(BeagleCoat.LEMON_1, "lemon1"),
            Map.entry(BeagleCoat.BROWN_WHITE, "brownwhite"),
            Map.entry(BeagleCoat.BROWN_WHITE_TAN, "brownwhitetan"),
            Map.entry(BeagleCoat.LEMON_2, "lemon2"),
            Map.entry(BeagleCoat.BLACK_WHITE, "blackwhite"),
            Map.entry(BeagleCoat.LILAC_1, "lilac1"),
            Map.entry(BeagleCoat.LILAC_2, "lilac2"));

    for (final BeagleCoat coat : BeagleCoat.values()) {
      assertEquals(expectedPrefixes.get(coat), coat.getTexturePrefix());
    }
  }

  @Test
  @DisplayName("getOrdinal should match enum ordinal for serialization")
  void testGetOrdinal() {
    for (final BeagleCoat coat : BeagleCoat.values()) {
      assertEquals(coat.ordinal(), coat.getOrdinal());
    }
  }
}
