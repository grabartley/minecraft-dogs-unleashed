package com.grahambartley.dogsunleashed.render.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SaddlebagsFitTest {

  @ParameterizedTest(name = "torso {0} px, armour sides {1} px -> scale {2}")
  @CsvSource({"8, 0, 1.0", "6, 0, 0.75", "12, 0, 1.5", "8, 0.5, 1.125", "6, 1, 1.0"})
  @DisplayName("the bags scale from an eight unit back to the torso, widened by armour")
  void scale(final float torsoPixels, final float sidePixels, final float expected) {
    assertEquals(expected, SaddlebagsFit.scale(torsoPixels / 16.0f, sidePixels / 16.0f), 1.0e-5f);
  }

  @ParameterizedTest(name = "armoured {0}, armour top {1} px -> lift {2} px")
  @CsvSource({"false, 0.25, 0", "true, 0.25, 0.35", "true, 0, 0.1"})
  @DisplayName("bags sit on bare backs and rest just above armour")
  void lift(final boolean armoured, final float topPixels, final float expectedPixels) {
    assertEquals(expectedPixels / 16.0f, SaddlebagsFit.lift(armoured, topPixels / 16.0f), 1.0e-6f);
  }
}
