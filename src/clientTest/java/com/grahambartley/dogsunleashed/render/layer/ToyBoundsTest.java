package com.grahambartley.dogsunleashed.render.layer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.render.rig.CubeGeometry.Bounds;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ToyBoundsTest {

  private static final float TOLERANCE = 1e-5f;

  @ParameterizedTest(name = "{0}")
  @CsvSource({"tennis_ball, 12, 12, 12, 6", "frisbee, 16, 2, 16, 1", "stick, 16, 2, 2, 0"})
  @DisplayName("each toy's bounds match its modelled size, centred on its axis")
  void boundsMatchTheModel(
      final String toy, final int width, final int height, final int depth, final int centreY) {
    final Bounds bounds = ToyBounds.of(ShippedRigs.model(toy));

    assertEquals(width / 16f, bounds.max().x - bounds.min().x, TOLERANCE);
    assertEquals(height / 16f, bounds.max().y - bounds.min().y, TOLERANCE);
    assertEquals(depth / 16f, bounds.max().z - bounds.min().z, TOLERANCE);
    assertEquals(0f, bounds.center().x, TOLERANCE);
    assertEquals(centreY / 16f, bounds.center().y, TOLERANCE);
    assertEquals(0f, bounds.center().z, TOLERANCE);
  }
}
