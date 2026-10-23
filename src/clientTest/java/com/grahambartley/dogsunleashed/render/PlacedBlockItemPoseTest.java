package com.grahambartley.dogsunleashed.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PlacedBlockItemPoseTest {

  @ParameterizedTest(name = "scale {0}, model point ({1},{2},{3})")
  @CsvSource({
    "1.0, 0, 0, 0",
    "2.0, -0.26875, 0, -0.19375",
    "2.0, 0.26875, 0.1625, 0.28125",
    "1.285, -0.28125, 0.75, 0.09375",
    "1.285, 0.28125, 0, -0.14375"
  })
  @DisplayName("a model point lands where the placed block entity renderer draws it")
  void matchesThePlacedBlock(final float scale, final float x, final float y, final float z) {
    final Matrix4f placed =
        new Matrix4f()
            .translate(0.5f, 0.0f, 0.5f)
            .scale(scale)
            .translate(-0.5f, 0.0f, -0.5f)
            .translate(0.5f, 0.0f, 0.5f);
    final Vector3f expected = placed.transformPosition(new Vector3f(x, y, z));
    final Vector3f actual = PlacedBlockItemPose.of(scale).transformPosition(new Vector3f(x, y, z));
    assertEquals(expected.x, actual.x, 1.0e-6f);
    assertEquals(expected.y, actual.y, 1.0e-6f);
    assertEquals(expected.z, actual.z, 1.0e-6f);
  }

  @ParameterizedTest(name = "scale {0}")
  @CsvSource({"1.0", "2.0", "1.285"})
  @DisplayName("the model origin sits on the floor in the middle of the block")
  void putsTheOriginOnTheFloorCentre(final float scale) {
    final Vector3f origin = PlacedBlockItemPose.of(scale).transformPosition(new Vector3f());
    assertEquals(0.5f, origin.x, 1.0e-6f);
    assertEquals(0.0f, origin.y, 1.0e-6f);
    assertEquals(0.5f, origin.z, 1.0e-6f);
  }
}
