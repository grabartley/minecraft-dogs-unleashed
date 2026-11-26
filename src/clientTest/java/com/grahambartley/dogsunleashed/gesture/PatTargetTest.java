package com.grahambartley.dogsunleashed.gesture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.Arm;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PatTargetTest {

  private static final float EPSILON = 1.0e-4f;

  @ParameterizedTest(name = "dog yaw {0} -> crown at {1}, {2}")
  @CsvSource({"0, 0, 0.3", "180, 0, -0.3", "90, -0.3, 0", "-90, 0.3, 0"})
  @DisplayName("a crown in front of the rig lands in front of the dog's facing")
  void theCrownTurnsWithTheDog(final float yaw, final float x, final float z) {
    final Vector3f onDog = PatTarget.onDog(new Vector3f(0.0f, 0.5f, -0.3f), yaw);
    assertEquals(x, onDog.x, EPSILON);
    assertEquals(0.5f, onDog.y, EPSILON);
    assertEquals(z, onDog.z, EPSILON);
  }

  @ParameterizedTest(name = "player yaw {0}, offset {1},{2} -> forward {3}, right {4}")
  @CsvSource({
    "0, 0, 1, 1, 0",
    "0, -1, 0, 0, 1",
    "180, 0, -1, 1, 0",
    "90, -1, 0, 1, 0",
    "90, 0, -1, 0, 1"
  })
  @DisplayName("the crown is measured in the player's own forward and right")
  void theCrownIsMeasuredInTheBodyFrame(
      final float yaw, final double x, final double z, final float forward, final float right) {
    final PatTarget.Crown right_ = PatTarget.inBodyFrame(new Vec3d(x, 0.4, z), yaw, Arm.RIGHT);
    final PatTarget.Crown left = PatTarget.inBodyFrame(new Vec3d(x, 0.4, z), yaw, Arm.LEFT);
    assertEquals(0.4f, right_.up(), EPSILON);
    assertEquals(forward, right_.forward(), EPSILON);
    assertEquals(right, right_.side(), EPSILON);
    assertEquals(-right, left.side(), EPSILON);
  }
}
