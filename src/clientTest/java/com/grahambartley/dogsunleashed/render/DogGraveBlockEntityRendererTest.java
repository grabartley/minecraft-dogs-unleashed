package com.grahambartley.dogsunleashed.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogGraveBlockEntityRendererTest {

  @ParameterizedTest(name = "axis {0} spans {1}..{2}")
  @CsvSource({"0, 0.1385, 0.8615", "1, 0.0, 0.9638", "2, 0.2785, 0.6205"})
  @DisplayName("the placed grave stands inside one block, on its floor")
  void standsInsideOneBlock(final int axis, final float low, final float high) {
    final Vector3f[] footprint =
        PlacedFootprint.of("dog_grave", DogGraveBlockEntityRenderer.GRAVE_SCALE);
    assertEquals(low, footprint[0].get(axis), 1.0e-3f);
    assertEquals(high, footprint[1].get(axis), 1.0e-3f);
  }
}
