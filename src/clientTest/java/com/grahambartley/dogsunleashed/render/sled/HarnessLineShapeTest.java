package com.grahambartley.dogsunleashed.render.sled;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HarnessLineShapeTest {

  @ParameterizedTest(name = "span ({0}, {1}, {2}) segment {3} -> ({4}, {5}, {6})")
  @CsvSource({
    "4, 1, 0, 0, 0, 0, 0",
    "4, 1, 0, 24, 4, 1, 0",
    "4, 1, 0, 12, 2, 0.25, 0",
    "4, -1, 2, 12, 2, -0.75, 1",
    "0, 0, 6, 6, 0, 0, 1.5"
  })
  @DisplayName("the line runs end to end and sags toward its lower end like a lead")
  void pointAt(
      final float x,
      final float y,
      final float z,
      final int segment,
      final float ex,
      final float ey,
      final float ez) {
    final Vector3f point = HarnessLineShape.pointAt(new Vector3f(x, y, z), segment);
    assertEquals(ex, point.x, 1.0e-6f);
    assertEquals(ey, point.y, 1.0e-6f);
    assertEquals(ez, point.z, 1.0e-6f);
  }
}
