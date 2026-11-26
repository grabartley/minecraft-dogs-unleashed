package com.grahambartley.dogsunleashed.gesture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.DogHeadReach;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PatTargetTest {

  private static final float EPSILON = 1.0e-4f;
  private static final DogHeadReach HEAD = new DogHeadReach(0.3f, 0.6f, 0.1f, 0.7f);

  @ParameterizedTest(name = "dog yaw {0} -> crown at {1}, {2}")
  @CsvSource({"0, 0, 0.3", "180, 0, -0.3", "90, -0.3, 0", "-90, 0.3, 0"})
  @DisplayName("a crown in front of the rig lands in front of the dog's facing")
  void theCrownTurnsWithTheDog(final float yaw, final float x, final float z) {
    final Vector3f onDog = PatTarget.onDog(new Vector3f(0.0f, 0.5f, -0.3f), yaw);
    assertEquals(x, onDog.x, EPSILON);
    assertEquals(0.5f, onDog.y, EPSILON);
    assertEquals(z, onDog.z, EPSILON);
  }

  @ParameterizedTest(name = "player yaw {0}, offset {1},{2},{3} -> model {4},{5},{6}")
  @CsvSource({
    "0, 0, 0, 1, 0, 24.016, -17.0667",
    "0, 1, 0, 0, 17.0667, 24.016, 0",
    "90, 0, 0, 1, 17.0667, 24.016, 0",
    "180, 0, 1, 0, 0, 6.9493, 0"
  })
  @DisplayName("the world is read in the player model's own space: forward is -z, left is +x")
  void theWorldIsReadInModelSpace(
      final float yaw,
      final double x,
      final double y,
      final double z,
      final float mx,
      final float my,
      final float mz) {
    final PatTarget.Frame frame = new PatTarget.Frame(Vec3d.ZERO, yaw, 1.0f);
    final Vector3f model = frame.toModel(new Vec3d(x, y, z));
    assertEquals(mx, model.x, 1.0e-3f);
    assertEquals(my, model.y, 1.0e-3f);
    assertEquals(mz, model.z, 1.0e-3f);
  }

  @ParameterizedTest(name = "yaw {0}, mirror {1}")
  @CsvSource({"0, 1", "37, 1", "-120, -1", "200, -1"})
  @DisplayName("model space and the world map back onto each other for either hand")
  void modelSpaceRoundTrips(final float yaw, final float mirror) {
    final PatTarget.Frame frame = new PatTarget.Frame(new Vec3d(3, 64, -2), yaw, mirror);
    final Vec3d world = new Vec3d(3.4, 64.6, -1.7);
    final Vec3d back = frame.toWorld(frame.toModel(world));
    assertEquals(0.0, back.distanceTo(world), 1.0e-4);
  }

  @ParameterizedTest(name = "player at {0},{1}")
  @CsvSource({"1, 0", "-1, 0", "0.5, 2"})
  @DisplayName(
      "the hand goes to the side of the neck facing the player, behind and below the crown")
  void theNeckSpotFacesThePlayer(final double px, final double pz) {
    final Vec3d crown = new Vec3d(0, 0.7, 0);
    final Vec3d facing = new Vec3d(0, 0, 1);
    final Vec3d neck = PatTarget.neckOf(crown, facing, new Vec3d(px, 0, pz), HEAD);
    assertTrue(neck.z < crown.z, "behind the crown");
    assertTrue(neck.y < crown.y, "below the crown");
    assertEquals(Math.signum(px), Math.signum(neck.x), "on the player's side");
  }

  @ParameterizedTest(name = "half width {0}, kneel {1} -> stroke scale {2}")
  @CsvSource({"0.103125, 1, 1", "0.2499, 0, 1", "0.01, 1, 0.5", "1.0, 0, 1.6"})
  @DisplayName("strokes scale with the size of the dog's head, within limits")
  void strokesScaleWithTheHead(final float halfWidth, final float kneel, final float scale) {
    assertEquals(scale, PatTarget.strokeScale(halfWidth, kneel), 1.0e-2f);
  }
}
