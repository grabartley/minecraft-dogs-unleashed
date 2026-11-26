package com.grahambartley.dogsunleashed.gesture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.gesture.PatSkeleton.Part;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ArmReachTest {

  private static final Part SHOULDER = new Part(new Vector3f(-5, 2, 0), new Quaternionf(), 0.0f);
  private static final Vector3f ELBOW_BACK = new Vector3f(-7, 6, 4);

  @ParameterizedTest(name = "target {0},{1},{2}")
  @CsvSource({"-6, 9, -4", "-4, 8, -6", "-8, 4, -5", "-6, 10.5, 0", "-3, 6, -3"})
  @DisplayName("the palm lands on any target within reach")
  void thePalmLandsOnTheTarget(final float x, final float y, final float z) {
    final Vector3f target = new Vector3f(x, y, z);
    final Part arm = ArmReach.toward(SHOULDER, target, ELBOW_BACK);
    assertEquals(0.0f, PatSkeleton.rightPalm(arm).distance(target), 0.05f);
    assertEquals(SHOULDER.pivot(), arm.pivot());
  }

  @ParameterizedTest(name = "target {0},{1},{2}")
  @CsvSource({"-6, 20, -10", "-6, -5, -15"})
  @DisplayName("a target out of reach gets a straight arm pointing at it")
  void aFarTargetGetsAStraightArm(final float x, final float y, final float z) {
    final Vector3f target = new Vector3f(x, y, z);
    final Part arm = ArmReach.toward(SHOULDER, target, ELBOW_BACK);
    assertTrue(Math.abs(arm.bend()) < 0.25f, "nearly straight, bend " + arm.bend());
    final Vector3f palm = PatSkeleton.rightPalm(arm);
    final Vector3f shoulder = new Vector3f(-6, 2, 0);
    assertTrue(
        new Vector3f(palm)
                .sub(shoulder)
                .normalize()
                .dot(new Vector3f(target).sub(shoulder).normalize())
            > 0.98f,
        "points at the target");
  }

  @ParameterizedTest(name = "target {0},{1},{2}")
  @CsvSource({"-6, 9, -4", "-4, 8, -6", "-6, 6, -6"})
  @DisplayName("the elbow only ever folds forward, the way a real elbow does")
  void theElbowFoldsForward(final float x, final float y, final float z) {
    final Part arm = ArmReach.toward(SHOULDER, new Vector3f(x, y, z), ELBOW_BACK);
    assertTrue(arm.bend() <= 0.0f, "bend " + arm.bend());
  }
}
