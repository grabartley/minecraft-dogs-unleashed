package com.grahambartley.dogsunleashed.render.sled;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SledItemRendererTest {

  @Test
  @DisplayName("the sled's three block length is fitted into one block")
  void fitsTheShippedSled() {
    final ModelFit fit = SledItemRenderer.fitOf(ShippedRigs.model("sled"));
    assertEquals(16.0f / 47.0f, fit.scale(), 1.0e-4f);
  }
}
