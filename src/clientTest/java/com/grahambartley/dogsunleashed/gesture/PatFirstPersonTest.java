package com.grahambartley.dogsunleashed.gesture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.Arm;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PatFirstPersonTest {

  private static final float EPSILON = 1.0e-4f;

  @Test
  @DisplayName("each empty hand rests low on its own side of the view, mirrored")
  void theHandsRestOnTheirOwnSide() {
    final Vector3f right = PatFirstPerson.restTip(Arm.RIGHT);
    final Vector3f left = PatFirstPerson.restTip(Arm.LEFT);
    assertTrue(right.x > 0.0f && right.y < 0.0f && right.z < 0.0f, "right hand at " + right);
    assertEquals(-right.x, left.x, EPSILON);
    assertEquals(right.y, left.y, EPSILON);
    assertEquals(right.z, left.z, EPSILON);
  }

  @ParameterizedTest(name = "yaw {0}, pitch {1}, offset {2},{3},{4} -> {5},{6},{7}")
  @CsvSource({
    "0, 0, 0, 0, 1, 0, 0, -1",
    "0, 0, -1, 0, 0, 1, 0, 0",
    "0, 0, 0, 1, 0, 0, 1, 0",
    "0, 90, 0, -1, 0, 0, 0, -1",
    "90, 0, -1, 0, 0, 0, 0, -1",
    "180, 0, 1, 0, 0, 1, 0, 0"
  })
  @DisplayName("the world is seen from the camera: ahead is -z, right is +x, up is +y")
  void theWorldIsSeenFromTheCamera(
      final float yaw,
      final float pitch,
      final double x,
      final double y,
      final double z,
      final float vx,
      final float vy,
      final float vz) {
    final Vector3f view = PatFirstPerson.toView(new Vec3d(x, y, z), yaw, pitch);
    assertEquals(vx, view.x, EPSILON);
    assertEquals(vy, view.y, EPSILON);
    assertEquals(vz, view.z, EPSILON);
  }

  @ParameterizedTest(name = "crown at {0},{1},{2}")
  @CsvSource({"0, -0.5, -1.0", "0.3, -0.6, -0.6", "-0.2, -0.3, -2.5"})
  @DisplayName("with no stroke the hand tip lands on the sightline to the dog's head")
  void theHandLandsOnTheSightline(final float x, final float y, final float z) {
    final Vector3f crown = new Vector3f(x, y, z);
    final Vector3f tip =
        PatFirstPerson.placement(Arm.RIGHT, crown, 0, 0, 1)
            .place(PatFirstPerson.restTip(Arm.RIGHT));
    final Vector3f expected =
        new Vector3f(crown).normalize().mul(Math.min(crown.length(), PatFirstPerson.MAX_REACH));
    assertEquals(expected.x, tip.x, EPSILON);
    assertEquals(expected.y, tip.y, EPSILON);
    assertEquals(expected.z, tip.z, EPSILON);
  }

  @ParameterizedTest(name = "crown at {0},{1},{2}")
  @CsvSource({"0, -0.5, -1.0", "0.3, -0.6, -0.6"})
  @DisplayName("the arm swings from the shoulder to point at the dog, not up like a pole")
  void theArmPointsAtTheDog(final float x, final float y, final float z) {
    final PatFirstPerson.Placement placement =
        PatFirstPerson.placement(Arm.RIGHT, new Vector3f(x, y, z), 0, 0, 1);
    final Vector3f shoulder = placement.place(PatFirstPerson.restShoulder(Arm.RIGHT));
    final Vector3f tip = placement.place(PatFirstPerson.restTip(Arm.RIGHT));
    final Vector3f arm = tip.sub(shoulder).normalize();
    final Vector3f sight =
        new Vector3f(x, y, z)
            .normalize()
            .mul(Math.min(new Vector3f(x, y, z).length(), PatFirstPerson.MAX_REACH))
            .sub(PatFirstPerson.restShoulder(Arm.RIGHT))
            .normalize();
    assertTrue(arm.dot(sight) > 0.99f, "arm " + arm + " against " + sight);
  }

  @Test
  @DisplayName("with no aim the hand stays where vanilla holds it")
  void noAimLeavesTheHandAlone() {
    final PatFirstPerson.Placement placement =
        PatFirstPerson.placement(Arm.RIGHT, new Vector3f(0, -0.5f, -1), 0.1f, 0.1f, 0.0f);
    final Vector3f tip = PatFirstPerson.restTip(Arm.RIGHT);
    assertEquals(0.0f, placement.place(tip).sub(tip).length(), EPSILON);
  }
}
