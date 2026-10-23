package com.grahambartley.dogsunleashed.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogBedBlockEntityRendererTest {

  @ParameterizedTest(name = "axis {0} spans {1}..{2}")
  @CsvSource({"0, -0.0125, 1.0125", "1, 0.0, 0.325", "2, 0.1125, 0.85"})
  @DisplayName("the placed bed fills about one block across and stays low")
  void fillsAboutOneBlock(final int axis, final float low, final float high) {
    final Vector3f[] footprint = PlacedFootprint.of("dog_bed", DogBedBlockEntityRenderer.BED_SCALE);
    assertEquals(low, footprint[0].get(axis), 1.0e-3f);
    assertEquals(high, footprint[1].get(axis), 1.0e-3f);
  }
}
