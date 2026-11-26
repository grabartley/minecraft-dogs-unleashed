package com.grahambartley.dogsunleashed.gesture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.gesture.PatSkeleton.Bones;
import com.grahambartley.dogsunleashed.gesture.PatSkeleton.Part;
import com.grahambartley.dogsunleashed.gesture.PatSkeleton.Pose;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PatSkeletonTest {

  private static final float EPSILON = 1.0e-4f;

  static Bones standing() {
    return new Bones(
        new Vector3f(),
        new Quaternionf(),
        new Quaternionf(),
        new Quaternionf(),
        new Quaternionf(),
        0.0f,
        new Quaternionf(),
        0.0f,
        new Quaternionf(),
        0.0f,
        new Quaternionf(),
        0.0f);
  }

  private static void assertAt(final float x, final float y, final float z, final Vector3f at) {
    assertEquals(x, at.x, EPSILON);
    assertEquals(y, at.y, EPSILON);
    assertEquals(z, at.z, EPSILON);
  }

  @Test
  @DisplayName("standing still, every part sits where the vanilla player model puts it")
  void standingMatchesVanilla() {
    final Pose pose = PatSkeleton.of(standing());
    assertAt(0, 0, 0, pose.body().pivot());
    assertAt(0, 0, 0, pose.head().pivot());
    assertAt(-5, 2, 0, pose.rightArm().pivot());
    assertAt(5, 2, 0, pose.leftArm().pivot());
    assertAt(-1.9f, 12, 0, pose.rightLeg().pivot());
    assertAt(1.9f, 12, 0, pose.leftLeg().pivot());
    assertAt(-6, 11, 0, PatSkeleton.rightPalm(pose.rightArm()));
  }

  @ParameterizedTest(name = "lean {0} degrees")
  @CsvSource({"10", "30", "45"})
  @DisplayName("leaning the torso swings the neck and shoulders forward about the hips")
  void leaningSwingsTheShoulders(final float degrees) {
    final float lean = (float) Math.toRadians(degrees);
    final Bones bones = PatBones.leanedIn(standing(), lean);
    final Pose pose = PatSkeleton.of(bones);
    assertAt(
        0, 12 - 12 * (float) Math.cos(lean), -12 * (float) Math.sin(lean), pose.body().pivot());
    assertEquals(lean, pose.rightArm().angles().x, EPSILON);
    assertAt(-1.9f, 12, 0, pose.rightLeg().pivot());
  }

  @ParameterizedTest(name = "drop {0}")
  @CsvSource({"2", "5"})
  @DisplayName("dropping the hips lowers the whole body and the legs with it")
  void droppingTheHipsLowersEverything(final float drop) {
    final Bones bones =
        new Bones(
            new Vector3f(0, drop, 0),
            new Quaternionf(),
            new Quaternionf(),
            new Quaternionf(),
            new Quaternionf(),
            0.0f,
            new Quaternionf(),
            0.0f,
            new Quaternionf(),
            0.0f,
            new Quaternionf(),
            0.0f);
    final Pose pose = PatSkeleton.of(bones);
    assertEquals(drop, pose.head().pivot().y, EPSILON);
    assertEquals(12 + drop, pose.leftLeg().pivot().y, EPSILON);
  }

  @ParameterizedTest(name = "elbow {0} degrees")
  @CsvSource({"-90", "-45", "0"})
  @DisplayName("bending the elbow folds the forearm forward from the elbow")
  void theElbowFoldsForward(final float degrees) {
    final float bend = (float) Math.toRadians(degrees);
    final Part arm = new Part(new Vector3f(-5, 2, 0), new Quaternionf(), bend);
    assertAt(-6, 6, 0, PatSkeleton.rightElbow(arm));
    final Vector3f palm = PatSkeleton.rightPalm(arm);
    assertEquals(6 + 5 * (float) Math.cos(bend), palm.y, EPSILON);
    assertEquals(5 * (float) Math.sin(bend), palm.z, EPSILON);
  }

  @Test
  @DisplayName("a left handed pose is the mirror image, with the sides swapped")
  void mirroringSwapsSides() {
    final Quaternionf turned = new Quaternionf().rotationZYX(0.2f, 0.4f, -0.6f);
    final Pose pose =
        new Pose(
            new Part(new Vector3f(), new Quaternionf(), 0),
            new Part(new Vector3f(), new Quaternionf(), 0),
            new Part(new Vector3f(-5, 2, 1), turned, -0.5f),
            new Part(new Vector3f(5, 2, 0), new Quaternionf(), 0),
            new Part(new Vector3f(-1.9f, 12, 0), new Quaternionf(), 0.3f),
            new Part(new Vector3f(1.9f, 12, 0), new Quaternionf(), 0));
    final Pose mirrored = pose.mirrored();
    assertAt(5, 2, 1, mirrored.leftArm().pivot());
    final Vector3f angles = mirrored.leftArm().angles();
    assertEquals(-0.6f, angles.x, EPSILON);
    assertEquals(-0.4f, angles.y, EPSILON);
    assertEquals(-0.2f, angles.z, EPSILON);
    assertEquals(-0.5f, mirrored.leftArm().bend(), EPSILON);
    assertEquals(0.3f, mirrored.leftLeg().bend(), EPSILON);
    assertTrue(mirrored.rightArm().pivot().x < 0, "the resting arm moves to the right side");
  }
}
