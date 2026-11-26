package com.grahambartley.dogsunleashed.gesture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class PatStrokeTest {

  private static final float EPSILON = 1.0e-5f;

  @ParameterizedTest(name = "{0} ticks in, reach {1}")
  @CsvSource({"-1, 0", "0, 0", "2.5, 0.5", "5, 1", "15, 1", "25, 1", "27.5, 0.5", "30, 0", "45, 0"})
  @DisplayName("the hand eases out to the dog, stays, then eases back")
  void reachEasesInAndOut(final float elapsed, final float expected) {
    assertEquals(expected, PatStroke.reach(elapsed), EPSILON);
  }

  @ParameterizedTest(name = "{0} ticks in, lift {1}")
  @CsvSource({"0, 0", "5, 1", "10, 0", "15, 1", "20, 0", "25, 1", "30, 0", "-3, 0"})
  @DisplayName("the hand pats three times, landing between lifts")
  void theHandPatsThreeTimes(final float elapsed, final float expected) {
    assertEquals(expected, PatStroke.patLift(elapsed), EPSILON);
  }

  @ParameterizedTest(name = "rest pitch {0}")
  @ValueSource(floats = {-0.6f, 0.0f, 0.4f, 1.2f})
  @DisplayName("before and after the pat the arm keeps its own pose")
  void outsideThePatTheArmKeepsItsPose(final float restPitch) {
    assertEquals(restPitch, PatStroke.armPitch(restPitch, 0.3f, -1.0f), EPSILON);
    assertEquals(restPitch, PatStroke.armPitch(restPitch, 0.3f, 0.0f), EPSILON);
    assertEquals(restPitch, PatStroke.armPitch(restPitch, 0.3f, 30.0f), EPSILON);
  }

  @ParameterizedTest(name = "looking {0} radians down")
  @ValueSource(floats = {-0.4f, 0.0f, 0.5f, 1.0f})
  @DisplayName("with the hand on the head the arm points where the player looks")
  void theLandedHandPointsWhereThePlayerLooks(final float headPitch) {
    assertEquals(
        -MathHelper.HALF_PI + headPitch, PatStroke.armPitch(0.2f, headPitch, 10.0f), EPSILON);
  }

  @ParameterizedTest(name = "looking {0} radians down")
  @ValueSource(floats = {-0.4f, 0.0f, 0.5f})
  @DisplayName("between pats the hand lifts off the head")
  void betweenPatsTheHandLifts(final float headPitch) {
    final float landed = PatStroke.armPitch(0.0f, headPitch, 10.0f);
    final float lifted = PatStroke.armPitch(0.0f, headPitch, 15.0f);
    assertTrue(lifted < landed, "a lifted arm is raised further than a landed one");
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Arm.class)
  @DisplayName("in first person the hand stays where vanilla holds it outside the pat")
  void firstPersonRestsOutsideThePat(final Arm arm) {
    final Vector3f shift = PatStroke.firstPersonShift(arm, 30.0f);
    assertEquals(0.0f, shift.length(), EPSILON);
    assertEquals(0.0f, PatStroke.firstPersonTiltDegrees(30.0f), EPSILON);
  }

  @ParameterizedTest(name = "{0} arm moves {1} on x")
  @CsvSource({"RIGHT, -1", "LEFT, 1"})
  @DisplayName("in first person the hand reaches in, down and forward from its own side")
  void firstPersonReachesInDownAndForward(final Arm arm, final float inward) {
    final Vector3f shift = PatStroke.firstPersonShift(arm, 10.0f);
    assertEquals(Math.signum(inward), Math.signum(shift.x), EPSILON);
    assertTrue(shift.y < 0.0f, "the hand drops towards the dog");
    assertTrue(shift.z < 0.0f, "the hand reaches away from the camera");
    assertTrue(PatStroke.firstPersonTiltDegrees(10.0f) > 0.0f, "the palm turns down");
  }
}
