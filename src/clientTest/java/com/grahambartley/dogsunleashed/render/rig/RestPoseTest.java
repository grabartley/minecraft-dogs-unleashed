package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import software.bernie.geckolib.cache.object.GeoBone;

class RestPoseTest {

  private static final float EPSILON = 1.0e-5f;
  private static final float QUARTER_TURN = (float) (Math.PI / 2.0);

  @Test
  @DisplayName("no joints leaves a point where it is")
  void noneIsIdentity() {
    assertClose(new Vector3f(1, 2, 3), RestPose.NONE.point(new Vector3f(1, 2, 3)));
  }

  @Test
  @DisplayName("a joint turns a point around its own pivot")
  void turnsAroundPivot() {
    final RestPose pose =
        new RestPose(
            List.of(new RestPose.Joint(new Vector3f(0, 1, 0), new Vector3f(QUARTER_TURN, 0, 0))));

    assertClose(new Vector3f(0, 2, 0), pose.point(new Vector3f(0, 1, -1)));
  }

  @Test
  @DisplayName("the child joint turns first, then its parent")
  void childBeforeParent() {
    final RestPose pose =
        new RestPose(
            List.of(
                new RestPose.Joint(new Vector3f(), new Vector3f(QUARTER_TURN, 0, 0)),
                new RestPose.Joint(new Vector3f(), new Vector3f(0, QUARTER_TURN, 0))));

    assertClose(new Vector3f(1, 0, 0), pose.point(new Vector3f(0, 1, 0)));
  }

  @Test
  @DisplayName("a neck tipped forward turns its underside to face forward")
  void tippedNeckFacesForward() {
    final RestPose pose =
        new RestPose(List.of(new RestPose.Joint(new Vector3f(), new Vector3f(QUARTER_TURN, 0, 0))));

    assertClose(new Vector3f(0, 0, -1), pose.direction(new Vector3f(0, -1, 0)));
  }

  @Test
  @DisplayName("undoing the rest rotation brings a posed direction back")
  void rotationUndoes() {
    final RestPose pose =
        new RestPose(
            List.of(
                new RestPose.Joint(new Vector3f(), new Vector3f(0.3f, 0, 0)),
                new RestPose.Joint(new Vector3f(), new Vector3f(0, 0.7f, 0.2f))));
    final Vector3f local = new Vector3f(0.2f, -0.9f, 0.4f).normalize();

    assertClose(local, pose.direction(local).rotate(pose.rotation().conjugate()));
  }

  @ParameterizedTest(name = "snapshot saved: {0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("a bone's rest pose turns through the bone and then its parent")
  void ofWalksTheBoneThenItsParent(final boolean snapshotSaved) {
    final GeoBone body = bone(null, "body", 0, 0, 0, 0, QUARTER_TURN, 0);
    final GeoBone neck = bone(body, "neck", 0, 16, 0, QUARTER_TURN, 0, 0);
    if (snapshotSaved) {
      body.saveInitialSnapshot();
      neck.saveInitialSnapshot();
      body.setRotY(0);
      neck.setRotX(0);
    }

    final RestPose pose = RestPose.of(neck);

    assertClose(new Vector3f(1, 1, 0), pose.point(new Vector3f(0, 2, 0)));
  }

  private static GeoBone bone(
      final GeoBone parent,
      final String name,
      final float pivotX,
      final float pivotY,
      final float pivotZ,
      final float rotX,
      final float rotY,
      final float rotZ) {
    final GeoBone bone = new GeoBone(parent, name, false, 0.0, false, false);
    bone.setPivotX(pivotX);
    bone.setPivotY(pivotY);
    bone.setPivotZ(pivotZ);
    bone.setRotX(rotX);
    bone.setRotY(rotY);
    bone.setRotZ(rotZ);
    return bone;
  }

  private static void assertClose(final Vector3f expected, final Vector3f actual) {
    assertEquals(expected.x, actual.x, EPSILON, "x of " + actual);
    assertEquals(expected.y, actual.y, EPSILON, "y of " + actual);
    assertEquals(expected.z, actual.z, EPSILON, "z of " + actual);
  }
}
