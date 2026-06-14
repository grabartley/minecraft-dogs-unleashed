package com.grahambartley.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class EntityUtilsTest {

  @ParameterizedTest(name = "{0} -> valid")
  @ValueSource(floats = {0.5f, 0.75f, 1.0f, 1.5f, 2.0f})
  @DisplayName("isValidScale accepts scales in [0.5, 2.0]")
  void isValidScaleAcceptsInRange(final float scale) {
    assertTrue(EntityUtils.isValidScale(scale));
  }

  @ParameterizedTest(name = "{0} -> invalid")
  @ValueSource(floats = {0.0f, 0.4f, 2.1f, 3.0f, -1.0f})
  @DisplayName("isValidScale rejects scales outside [0.5, 2.0]")
  void isValidScaleRejectsOutOfRange(final float scale) {
    assertFalse(EntityUtils.isValidScale(scale));
  }

  @ParameterizedTest(name = "{0} clamps to {1}")
  @CsvSource({"0.3f, 0.5f", "0.5f, 0.5f", "1.0f, 1.0f", "1.5f, 1.5f", "2.0f, 2.0f", "3.0f, 2.0f"})
  @DisplayName("clampScale clamps to [0.5, 2.0]")
  void clampScaleClampsToRange(final float input, final float expected) {
    assertEquals(expected, EntityUtils.clampScale(input));
  }

  @Test
  @DisplayName("getSpawnEggColor packs primary into high bits and secondary into low bits")
  void getSpawnEggColorPacksBits() {
    assertEquals((0xFFFFFF << 16) | 0x808080, EntityUtils.getSpawnEggColor(0xFFFFFF, 0x808080));
  }

  static Stream<int[]> invalidColorPairs() {
    return Stream.of(
        new int[] {0x1000000, 0x808080},
        new int[] {0xFFFFFF, -1},
        new int[] {-1, 0x808080},
        new int[] {0x1000000, -1});
  }

  @ParameterizedTest
  @MethodSource("invalidColorPairs")
  @DisplayName("getSpawnEggColor rejects colors outside [0x000000, 0xFFFFFF]")
  void getSpawnEggColorRejectsInvalid(final int[] pair) {
    assertThrows(
        IllegalArgumentException.class, () -> EntityUtils.getSpawnEggColor(pair[0], pair[1]));
  }

  @Test
  @DisplayName("getModVersion returns the pinned mod version string")
  void getModVersionReturnsPinnedString() {
    assertEquals("1.0.0", EntityUtils.getModVersion());
  }
}
