package com.grahambartley.dogsunleashed.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ConfigBoundsTest {

  @ParameterizedTest(name = "{0} in [{1}, {2}] is {3}")
  @CsvSource({"5, 0, 10, 5", "-1, 0, 10, 0", "11, 0, 10, 10", "0, 0, 10, 0", "10, 0, 10, 10"})
  @DisplayName("whole numbers are clamped into their range")
  void clampsInts(final int value, final int min, final int max, final int expected) {
    assertEquals(expected, ConfigBounds.clamp(value, min, max));
  }

  @ParameterizedTest(name = "{0} in [{1}, {2}] is {3}")
  @CsvSource({"0.5, 0, 2, 0.5", "-0.1, 0, 2, 0", "2.5, 0, 2, 2"})
  @DisplayName("decimals are clamped into their range")
  void clampsFloats(final float value, final float min, final float max, final float expected) {
    assertEquals(expected, ConfigBounds.clamp(value, min, max, 1.0f));
  }

  @ParameterizedTest(name = "{0} falls back")
  @ValueSource(floats = {Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY})
  @DisplayName("decimals that are not finite are refused and replaced by the fallback")
  void refusesNonFiniteFloats(final float value) {
    assertEquals(1.5f, ConfigBounds.clamp(value, 0.0f, 2.0f, 1.5f));
  }
}
