package com.grahambartley.dogsunleashed.render.cosmetic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.render.rig.RestPose;
import com.grahambartley.dogsunleashed.render.rig.RestPose.Joint;
import java.util.List;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MobHeadArtPlacementTest {

  private static final float EPSILON = 1.0e-5f;

  @Test
  @DisplayName("a bone at rest leaves art where it was drawn")
  void restingBoneIsIdentity() {
    final RestPose pose =
        new RestPose(List.of(new Joint(new Vector3f(0.0f, 0.025f, 0.0f), new Vector3f())));

    final Vector3f placed = new Vector3f(0.3f, 0.5f, -0.2f);
    MobHeadArtPlacement.modelToBone(pose).transformPosition(placed);

    assertVector(new Vector3f(0.3f, 0.5f, -0.2f), placed);
  }

  @Test
  @DisplayName("a quarter turn about Y is undone")
  void quarterTurnIsUndone() {
    final RestPose pose =
        new RestPose(
            List.of(new Joint(new Vector3f(), new Vector3f(0.0f, (float) (Math.PI / 2.0), 0.0f))));

    final Vector3f placed = new Vector3f(1.0f, 0.0f, 0.0f);
    MobHeadArtPlacement.modelToBone(pose).transformPosition(placed);

    assertVector(new Vector3f(0.0f, 0.0f, 1.0f), placed);
  }

  @ParameterizedTest(name = "{0} {1} {2}")
  @CsvSource({"0.3, 0.5, -0.2", "-1.0, 0.0, 2.0", "0.0, 0.0, 0.0"})
  @DisplayName("posing a placed point lands it back where it was drawn")
  void posingLandsWhereDrawn(final float x, final float y, final float z) {
    final RestPose pose =
        new RestPose(
            List.of(
                new Joint(new Vector3f(0.1f, 0.2f, 0.3f), new Vector3f(0.4f, -0.7f, 0.2f)),
                new Joint(new Vector3f(0.0f, 0.5f, -0.1f), new Vector3f(0.0f, 0.3f, -0.5f))));

    final Vector3f placed = new Vector3f(x, y, z);
    MobHeadArtPlacement.modelToBone(pose).transformPosition(placed);

    assertVector(new Vector3f(x, y, z), pose.point(placed));
  }

  private static void assertVector(final Vector3f expected, final Vector3f actual) {
    assertEquals(expected.x, actual.x, EPSILON);
    assertEquals(expected.y, actual.y, EPSILON);
    assertEquals(expected.z, actual.z, EPSILON);
  }
}
