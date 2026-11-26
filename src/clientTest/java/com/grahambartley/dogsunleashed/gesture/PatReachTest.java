package com.grahambartley.dogsunleashed.gesture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.gesture.PatSkeleton.Bones;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PatReachTest {

  private static final Bones STANDING = PatSkeletonTest.standing();

  @ParameterizedTest(name = "target {0},{1},{2}")
  @CsvSource({"-6, 8, -5", "-5, 4, -6", "-7, 10, -3"})
  @DisplayName("a target the arm already reaches needs no lean")
  void aCloseTargetNeedsNoLean(final float x, final float y, final float z) {
    assertEquals(0.0f, PatReach.extraLean(STANDING, new Vector3f(x, y, z)), 1.0e-5f);
  }

  @ParameterizedTest(name = "target {0},{1},{2}")
  @CsvSource({"-6, 12, -9", "-5, 11, -10", "-6, 10, -11"})
  @DisplayName("a low, far target gets just enough lean for the arm to reach it")
  void aFarTargetGetsJustEnoughLean(final float x, final float y, final float z) {
    final Vector3f target = new Vector3f(x, y, z);
    final float lean = PatReach.extraLean(STANDING, target);
    assertTrue(lean > 0.0f, "leans in");
    final Vector3f shoulder = PatSkeleton.of(PatBones.leanedIn(STANDING, lean)).rightArm().pivot();
    assertTrue(shoulder.distance(target) <= ArmReach.reach(), "the arm reaches");
    final float step = PatReach.MAX_EXTRA_LEAN / PatReach.LEAN_STEPS;
    final Vector3f lessLean =
        PatSkeleton.of(PatBones.leanedIn(STANDING, lean - step)).rightArm().pivot();
    assertTrue(lessLean.distance(target) > ArmReach.reach(), "no more lean than needed");
  }

  @ParameterizedTest(name = "target {0},{1},{2}")
  @CsvSource({"-6, 23, -30", "-6, -20, -30"})
  @DisplayName("a target no lean can reach gets the lean that comes closest, capped")
  void anUnreachableTargetIsCapped(final float x, final float y, final float z) {
    final float lean = PatReach.extraLean(STANDING, new Vector3f(x, y, z));
    assertTrue(lean <= PatReach.MAX_EXTRA_LEAN + 1.0e-5f);
  }
}
