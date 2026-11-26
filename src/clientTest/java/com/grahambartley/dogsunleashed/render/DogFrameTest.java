package com.grahambartley.dogsunleashed.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogFrameTest {

  private static final float EPSILON = 1.0e-4f;

  @ParameterizedTest(name = "scale {0}, yaw offset {1} -> {2}, {3}, {4}")
  @CsvSource({
    "1.0, 0, 0, 0.5, -0.3",
    "1.5, 0, 0, 0.75, -0.45",
    "0.75, 0, 0, 0.375, -0.225",
    "1.3, 180, 0, 0.65, 0.39"
  })
  @DisplayName("a model point lands where the renderer draws it on the dog")
  void aModelPointLandsWhereTheRendererDrawsIt(
      final float scale, final float yawOffset, final float x, final float y, final float z) {
    final Vector3f point =
        new DogFrame(scale, yawOffset, null).toEntity(new Vector3f(0, 0.5f, -0.3f));
    assertEquals(x, point.x, EPSILON);
    assertEquals(y, point.y, EPSILON);
    assertEquals(z, point.z, EPSILON);
  }
}
