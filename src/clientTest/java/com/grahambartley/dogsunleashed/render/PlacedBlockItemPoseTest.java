package com.grahambartley.dogsunleashed.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PlacedBlockItemPoseTest {

  private static MatrixStack posed(final float scale) {
    final MatrixStack stack = new MatrixStack();
    PlacedBlockItemPose.apply(stack, scale);
    return stack;
  }

  @ParameterizedTest(name = "scale {0}, model point ({1},{2},{3})")
  @CsvSource({
    "1.0, 0, 0, 0",
    "2.0, -0.26875, 0, -0.19375",
    "2.0, 0.26875, 0.1625, 0.28125",
    "1.285, -0.28125, 0.75, 0.09375",
    "1.285, 0.28125, 0, -0.14375"
  })
  @DisplayName("a model point lands where the placed block entity renderer drew it on main")
  void matchesThePlacedBlock(final float scale, final float x, final float y, final float z) {
    final MatrixStack placed = new MatrixStack();
    placed.translate(0.5, 0, 0.5);
    placed.scale(scale, scale, scale);
    placed.translate(-0.5, 0, -0.5);
    placed.translate(0.5, 0, 0.5);
    final Vector3f expected =
        placed.peek().getPositionMatrix().transformPosition(new Vector3f(x, y, z));
    final Vector3f actual =
        posed(scale).peek().getPositionMatrix().transformPosition(new Vector3f(x, y, z));
    assertEquals(expected.x, actual.x, 1.0e-6f);
    assertEquals(expected.y, actual.y, 1.0e-6f);
    assertEquals(expected.z, actual.z, 1.0e-6f);
  }

  @ParameterizedTest(name = "scale {0}")
  @CsvSource({"1.0", "2.0", "1.285"})
  @DisplayName("the model origin sits on the floor in the middle of the block")
  void putsTheOriginOnTheFloorCentre(final float scale) {
    final Vector3f origin =
        posed(scale).peek().getPositionMatrix().transformPosition(new Vector3f());
    assertEquals(0.5f, origin.x, 1.0e-6f);
    assertEquals(0.0f, origin.y, 1.0e-6f);
    assertEquals(0.5f, origin.z, 1.0e-6f);
  }

  @ParameterizedTest(name = "scale {0}")
  @CsvSource({"1.0", "2.0", "1.285"})
  @DisplayName("the pose leaves the normal matrix alone, so lighting matches the placed block")
  void leavesLightingAlone(final float scale) {
    final MatrixStack inGui = new MatrixStack();
    inGui.scale(16.0f, -16.0f, 16.0f);
    final Matrix3f before = new Matrix3f(inGui.peek().getNormalMatrix());
    PlacedBlockItemPose.apply(inGui, scale);
    assertEquals(before, inGui.peek().getNormalMatrix());
  }
}
