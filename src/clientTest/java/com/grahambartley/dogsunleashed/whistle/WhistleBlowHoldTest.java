package com.grahambartley.dogsunleashed.whistle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.Arm;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class WhistleBlowHoldTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(Arm.class)
  @DisplayName("at rest the whistle stays where vanilla holds it")
  void restingHoldIsUntouched(final Arm arm) {
    final Vector3f shift = WhistleBlowHold.shift(arm, 0.0f);
    assertEquals(0.0f, shift.x, 1.0e-6f);
    assertEquals(0.0f, shift.y, 1.0e-6f);
    assertEquals(0.0f, shift.z, 1.0e-6f);
    assertEquals(0.0f, WhistleBlowHold.turn(arm, 0.0f), 1.0e-6f);
  }

  @ParameterizedTest(name = "{0} at lift {1}")
  @CsvSource({"RIGHT, 1, -1", "RIGHT, 0.5, -1", "LEFT, 1, 1", "LEFT, 0.25, 1"})
  @DisplayName("the whistle moves in from its own side, up and towards the face")
  void movesTowardsTheMouth(final Arm arm, final float lift, final float inward) {
    final Vector3f shift = WhistleBlowHold.shift(arm, lift);
    assertEquals(inward * WhistleBlowHold.TOWARDS_MOUTH * lift, shift.x, 1.0e-6f);
    assertEquals(WhistleBlowHold.LIFT * lift, shift.y, 1.0e-6f);
    assertEquals(WhistleBlowHold.CLOSER * lift, shift.z, 1.0e-6f);
  }

  @ParameterizedTest(name = "lift {0}")
  @CsvSource({"0.25", "0.5", "1"})
  @DisplayName("the two hands mirror each other")
  void handsMirror(final float lift) {
    final Vector3f right = WhistleBlowHold.shift(Arm.RIGHT, lift);
    final Vector3f left = WhistleBlowHold.shift(Arm.LEFT, lift);
    assertEquals(-right.x, left.x, 1.0e-6f);
    assertEquals(right.y, left.y, 1.0e-6f);
    assertEquals(right.z, left.z, 1.0e-6f);
    assertEquals(
        -WhistleBlowHold.turn(Arm.RIGHT, lift), WhistleBlowHold.turn(Arm.LEFT, lift), 1.0e-6f);
  }
}
