package com.grahambartley.dogsunleashed.render.costume;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.render.rig.SlotAnchor;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CostumePartFitTest {

  private static final float TOLERANCE = 1.0e-5f;
  private static final SlotAnchor LEVEL =
      new SlotAnchor(
          "torso", new Vector3f(1, 2, 3), new Quaternionf(), new Vector3f(0.5f, 0.25f, 1.0f));
  private static final SlotAnchor TILTED =
      new SlotAnchor(
          "torso",
          new Vector3f(0, 1, 0),
          new Quaternionf().rotationX((float) Math.toRadians(90)),
          new Vector3f(1, 1, 1));

  @ParameterizedTest(name = "yaw {0}: ({1}, {2}, {3}) -> ({4}, {5}, {6})")
  @CsvSource({
    "0, 0, 0, 0, 1, 2, 3",
    "0, 0.5, 0.5, 0.5, 1.25, 2.125, 3.5",
    "0, -0.5, -0.5, -0.5, 0.75, 1.875, 2.5",
    "180, 0.5, 0, 0.5, 0.75, 2, 2.5"
  })
  @DisplayName("a one-block reference box around the part origin stretches over the slot bounds")
  void referenceBoxFillsTheSlot(
      final float yaw,
      final float x,
      final float y,
      final float z,
      final float ex,
      final float ey,
      final float ez) {
    assertPoint(new Vector3f(ex, ey, ez), transform(LEVEL, yaw, new Vector3f(x, y, z)));
  }

  @ParameterizedTest(name = "({0}, {1}, {2}) -> ({3}, {4}, {5})")
  @CsvSource({"0, 0, 0, 0, 1, 0", "0, 0.5, 0, 0, 1, -0.5", "0, 0, 0.5, 0, 1.5, 0"})
  @DisplayName("the part stays upright in model space however the anchor bone is turned at rest")
  void restRotationIsUndone(
      final float x, final float y, final float z, final float ex, final float ey, final float ez) {
    assertPoint(new Vector3f(ex, ey, ez), transform(TILTED, 0, new Vector3f(x, y, z)));
  }

  private static Vector3f transform(
      final SlotAnchor anchor, final float yaw, final Vector3f point) {
    return CostumePartFit.partToBone(anchor, yaw).transformPosition(point);
  }

  private static void assertPoint(final Vector3f expected, final Vector3f actual) {
    assertEquals(expected.x, actual.x, TOLERANCE);
    assertEquals(expected.y, actual.y, TOLERANCE);
    assertEquals(expected.z, actual.z, TOLERANCE);
  }
}
