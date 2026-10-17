package com.grahambartley.dogsunleashed.render.armour;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InflatedCubesTest {

  @ParameterizedTest(name = "size {0} grown by {1}")
  @CsvSource({"8, 0.25, 1.0625", "2, 0.5, 1.5", "0, 0.25, 1.0"})
  @DisplayName("a cube grows by the same amount on every side, and a flat side is left alone")
  void growsEvenly(final float size, final float by, final float scale) {
    assertEquals(scale, InflatedCubes.grow(size, by), 1.0e-6f);
  }
}
