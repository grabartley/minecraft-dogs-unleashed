package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChargedJumpTest {

  private static final float EPSILON = 0.0001f;

  @ParameterizedTest(name = "held {0} ticks -> {1}")
  @CsvSource({"0,0", "1,0.1", "5,0.5", "9,0.9", "10,1.0", "11,0.9", "29,0.81"})
  @DisplayName("the charge builds like a horse jump, peaks at ten ticks, then settles")
  void chargeCurve(final int heldTicks, final float expected) {
    assertEquals(expected, ChargedJump.charge(heldTicks), EPSILON);
  }

  @ParameterizedTest(name = "charge {0} -> power {1}")
  @CsvSource({"0,0.4", "0.45,0.6", "0.89,0.7956", "0.9,1.0", "1.0,1.0"})
  @DisplayName("a tap still leaps, and nine tenths of a charge gives the full jump")
  void powerCurve(final float charge, final float expected) {
    assertEquals(expected, ChargedJump.power(charge), 0.001f);
  }

  @Test
  @DisplayName("releasing after a full charge leaps at full power, then waits out a cooldown")
  void releaseLeapsOnceThenCoolsDown() {
    final ChargedJump jump = new ChargedJump();
    for (int tick = 0; tick < ChargedJump.FULL_CHARGE_TICKS; tick++) {
      assertEquals(0.0f, jump.tick(true));
    }
    assertEquals(1.0f, jump.charge(), EPSILON);
    assertEquals(1.0f, jump.tick(false));
    assertEquals(0.0f, jump.charge());
    for (int tick = 0; tick < ChargedJump.COOLDOWN_TICKS; tick++) {
      assertEquals(0.0f, jump.tick(true));
      assertEquals(0.0f, jump.charge());
    }
    jump.tick(true);
    assertEquals(0.1f, jump.charge(), EPSILON);
  }

  @Test
  @DisplayName("nothing happens while jump is never pressed")
  void idleDoesNothing() {
    final ChargedJump jump = new ChargedJump();
    for (int tick = 0; tick < 30; tick++) {
      assertEquals(0.0f, jump.tick(false));
    }
  }
}
