package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DogWetnessTest {

  static Stream<Arguments> dryingTimers() {
    return Stream.of(
        Arguments.of("a wet tick resets the timer", true, false, 15, 0),
        Arguments.of("a wet tick resets a timer that was already counting", true, true, 15, 0),
        Arguments.of("the first dry tick starts at one", false, true, 0, 1),
        Arguments.of("a running dry timer keeps climbing", false, false, 5, 6),
        Arguments.of("a dog that was never wet stays at zero", false, false, 0, 0));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("dryingTimers")
  @DisplayName("the drying timer resets while wet, starts on the first dry tick, and then climbs")
  void dryingTimerTransitions(
      final String label,
      final boolean wet,
      final boolean wasWet,
      final int ticksSinceWet,
      final int expected) {
    assertEquals(expected, DogWetness.nextTicksSinceWet(wet, wasWet, ticksSinceWet));
  }

  @Test
  @DisplayName("the shake fires on exactly one dry tick, and never while the dog is still wet")
  void shakeStartsOnASingleDryTick() {
    int startTicks = 0;
    for (int ticks = 0; ticks <= 60; ticks++) {
      if (DogWetness.isShakeStartTick(false, ticks)) {
        startTicks++;
      }
      assertFalse(
          DogWetness.isShakeStartTick(true, ticks),
          "a wet dog should never start a shake, ticks=" + ticks);
    }
    assertEquals(1, startTicks);
  }

  @Test
  @DisplayName("a dog shakes exactly once, the shake delay after its last wet tick")
  void shakeStartsAfterTheDelayFollowingTheLastWetTick() {
    final DogWetness wetness = new DogWetness();

    for (int tick = 0; tick < 40; tick++) {
      assertFalse(wetness.tick(true), "a wet dog should not shake while still wet, tick=" + tick);
    }

    int shakeStartTicks = 0;
    int shakeStartTick = -1;
    for (int dryTick = 1; dryTick <= 60; dryTick++) {
      if (wetness.tick(false)) {
        shakeStartTicks++;
        shakeStartTick = dryTick;
      }
    }

    assertEquals(1, shakeStartTicks);
    assertEquals(DogWetness.SHAKE_DELAY_TICKS, shakeStartTick);
  }

  @Test
  @DisplayName("stepping back into the wet before the delay elapses restarts the countdown")
  void reWettingRestartsTheCountdown() {
    final DogWetness wetness = new DogWetness();
    wetness.tick(true);

    for (int dryTick = 1; dryTick < DogWetness.SHAKE_DELAY_TICKS; dryTick++) {
      assertFalse(wetness.tick(false), "the shake should not start early, dryTick=" + dryTick);
    }
    assertFalse(wetness.tick(true), "getting wet again should cancel the pending shake");

    for (int dryTick = 1; dryTick < DogWetness.SHAKE_DELAY_TICKS; dryTick++) {
      assertFalse(
          wetness.tick(false), "the restarted countdown should not fire early, dryTick=" + dryTick);
    }
    assertTrue(wetness.tick(false), "the shake should fire the full delay after the last wet tick");
  }

  @Test
  @DisplayName("a dog that is never wet never shakes")
  void neverWetNeverShakes() {
    final DogWetness wetness = new DogWetness();
    for (int tick = 0; tick < 200; tick++) {
      assertFalse(wetness.tick(false), "a dry dog should never shake, tick=" + tick);
    }
  }
}
