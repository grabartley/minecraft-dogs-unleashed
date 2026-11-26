package com.grahambartley.dogsunleashed.gesture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.math.MathHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PatReachTest {

  private static final float EPSILON = 0.05f;
  private static final float HAND_ON_HEAD = 1.0f;

  @ParameterizedTest(name = "crown {0} blocks up -> kneels {1}")
  @CsvSource({"0.3, true", "0.62, true", "0.75, true", "0.77, false", "0.95, false", "1.6, false"})
  @DisplayName("the player kneels for a low head and stands for a tall one")
  void kneelsOnlyForALowHead(final float up, final boolean kneels) {
    assertEquals(kneels, PatReach.toward(up, 0.55f, 0.0f).drop() > 0.0f);
  }

  @ParameterizedTest(name = "crown {0} up, {1} forward")
  @CsvSource({"0.31, 0.45", "0.62, 0.45", "0.95, 0.55", "1.05, 0.6"})
  @DisplayName("the hand comes to rest on the crown when it can")
  void theHandRestsOnTheCrown(final float up, final float forward) {
    final PatReach.Pose pose = PatReach.toward(up, forward, 0.0f);
    final float shoulderUp = PatReach.shoulderHeight(pose.drop(), pose.lean());
    final float shoulderForward = PatReach.HIP_TO_SHOULDER * MathHelper.sin(pose.lean());
    final float handUp = shoulderUp - PatReach.ARM_REACH * MathHelper.cos(pose.armPitch());
    final float handForward =
        shoulderForward - PatReach.ARM_REACH * MathHelper.sin(pose.armPitch());
    assertEquals(up * PatReach.PIXELS_PER_BLOCK, handUp, HAND_ON_HEAD);
    assertEquals(forward * PatReach.PIXELS_PER_BLOCK, handForward, HAND_ON_HEAD);
  }

  @ParameterizedTest(name = "crown {0} up")
  @CsvSource({"1.6", "2.0"})
  @DisplayName("a head above the shoulder gets a raised arm from a standing player")
  void aHighHeadGetsARaisedArm(final float up) {
    final PatReach.Pose pose = PatReach.toward(up, 0.6f, 0.0f);
    assertEquals(0.0f, pose.drop(), EPSILON);
    assertTrue(pose.armPitch() < -MathHelper.HALF_PI, "the arm points up past level");
  }

  @ParameterizedTest(name = "crown {0} blocks to the patting side -> inward {1}")
  @CsvSource({"0.0, true", "0.1, true", "0.4, false"})
  @DisplayName("the arm turns in towards a crown on the body's centre line")
  void theArmTurnsInTowardsTheCrown(final float side, final boolean inward) {
    assertEquals(inward, PatReach.toward(0.62f, 0.55f, side).armInward() > 0.0f);
  }

  @ParameterizedTest(name = "drop {0} px")
  @CsvSource({"0, 0", "6, 0.3515625"})
  @DisplayName("the eye drops by the kneel, in blocks")
  void theEyeDropsByTheKneel(final float drop, final float blocks) {
    assertEquals(blocks, new PatReach.Pose(drop, 0.0f, 0.0f, 0.0f).eyeDropBlocks(), 1.0e-5f);
  }
}
