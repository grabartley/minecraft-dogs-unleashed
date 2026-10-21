package com.grahambartley.dogsunleashed.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FittedGeoItemRendererTest {

  @ParameterizedTest(name = "{0}, {1} pixels at its longest")
  @CsvSource({"sled, 47", "dog_house, 32", "rope_toy, 26", "frisbee, 16"})
  @DisplayName("a model's longest side, rotations included, is fitted into one block")
  void fitsTheLongestSide(final String model, final float longestPixels) {
    final ModelFit fit = FittedGeoItemRenderer.fitOf(ShippedRigs.model(model), ModelFit::intoBlock);
    assertEquals(16.0f / longestPixels, fit.scale(), 1.0e-4f);
  }

  @ParameterizedTest(name = "{0}, {1} pixels at its longest")
  @CsvSource({"rope_toy, 26"})
  @DisplayName("a held toy's longest side is fitted across the block's diagonal")
  void fitsTheLongestSideAcrossTheDiagonal(final String model, final float longestPixels) {
    final ModelFit fit =
        FittedGeoItemRenderer.fitOf(ShippedRigs.model(model), ModelFit::acrossBlock);
    assertEquals(16.0f * (float) Math.sqrt(2.0) / longestPixels, fit.scale(), 1.0e-4f);
  }
}
