package com.grahambartley.dogsunleashed.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ModelFitTest {

  @ParameterizedTest(name = "({0},{1},{2})..({3},{4},{5})")
  @CsvSource({
    "0, 0, 0, 1, 1, 1",
    "-0.5625, 0, -1.68, 0.5625, 1.3125, 1.375",
    "-2, 1, -2, 2, 2, 2",
    "0, 0, 0, 0.25, 0.5, 0.125"
  })
  @DisplayName("the longest side fills one block, centred on x and z and standing on the floor")
  void fitsIntoOneBlock(
      final float minX,
      final float minY,
      final float minZ,
      final float maxX,
      final float maxY,
      final float maxZ) {
    final ModelFit fit =
        ModelFit.intoBlock(new Vector3f(minX, minY, minZ), new Vector3f(maxX, maxY, maxZ));
    final Vector3f low = new Vector3f(minX, minY, minZ).mul(fit.scale()).add(fit.offset());
    final Vector3f high = new Vector3f(maxX, maxY, maxZ).mul(fit.scale()).add(fit.offset());
    assertEquals(1.0f, Math.max(high.x - low.x, Math.max(high.y - low.y, high.z - low.z)), 1.0e-5f);
    assertEquals(0.5f, (low.x + high.x) / 2.0f, 1.0e-5f);
    assertEquals(0.5f, (low.z + high.z) / 2.0f, 1.0e-5f);
    assertEquals(0.0f, low.y, 1.0e-5f);
  }

  @ParameterizedTest(name = "({0},{1},{2})..({3},{4},{5})")
  @CsvSource({
    "-0.8125, -0.09375, -0.21875, 0.8125, 0.34375, 0.21875",
    "0, 0, 0, 1, 1, 1",
    "-2, 1, -2, 2, 2, 2"
  })
  @DisplayName(
      "across the block, the longest side spans the diagonal and the model sits in the centre")
  void fitsAcrossTheBlock(
      final float minX,
      final float minY,
      final float minZ,
      final float maxX,
      final float maxY,
      final float maxZ) {
    final ModelFit fit =
        ModelFit.acrossBlock(new Vector3f(minX, minY, minZ), new Vector3f(maxX, maxY, maxZ));
    final Vector3f low = new Vector3f(minX, minY, minZ).mul(fit.scale()).add(fit.offset());
    final Vector3f high = new Vector3f(maxX, maxY, maxZ).mul(fit.scale()).add(fit.offset());
    assertEquals(
        1.41421f, Math.max(high.x - low.x, Math.max(high.y - low.y, high.z - low.z)), 1.0e-4f);
    assertEquals(0.5f, (low.x + high.x) / 2.0f, 1.0e-5f);
    assertEquals(0.5f, (low.y + high.y) / 2.0f, 1.0e-5f);
    assertEquals(0.5f, (low.z + high.z) / 2.0f, 1.0e-5f);
  }

  @ParameterizedTest(name = "({0},{1},{2})..({3},{4},{5})")
  @CsvSource({
    "-0.4375, -0.125, -0.125, 0.3125, 0.4375, 0.125",
    "0, 0, 0, 1, 1, 1",
    "-2, 1, -2, 2, 2, 2",
    "0, 0, 0, 0.25, 0.5, 0.125"
  })
  @DisplayName("centred in the block, the longest side fills it and the model sits in the middle")
  void centresInTheBlock(
      final float minX,
      final float minY,
      final float minZ,
      final float maxX,
      final float maxY,
      final float maxZ) {
    final ModelFit fit =
        ModelFit.centredInBlock(new Vector3f(minX, minY, minZ), new Vector3f(maxX, maxY, maxZ));
    final Vector3f low = new Vector3f(minX, minY, minZ).mul(fit.scale()).add(fit.offset());
    final Vector3f high = new Vector3f(maxX, maxY, maxZ).mul(fit.scale()).add(fit.offset());
    assertEquals(1.0f, Math.max(high.x - low.x, Math.max(high.y - low.y, high.z - low.z)), 1.0e-5f);
    assertEquals(0.5f, (low.x + high.x) / 2.0f, 1.0e-5f);
    assertEquals(0.5f, (low.y + high.y) / 2.0f, 1.0e-5f);
    assertEquals(0.5f, (low.z + high.z) / 2.0f, 1.0e-5f);
  }
}
