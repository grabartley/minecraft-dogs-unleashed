package com.grahambartley.dogsunleashed.pendant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class VitalityCadenceTest {

  private static final int VANILLA_REGENERATION_ONE_INTERVAL = 50;
  private static final int CHECK_EVERY_TICKS = 20;
  private static final int SIMULATED_TICKS = 2000;

  static Stream<Arguments> durations() {
    return Stream.of(
        Arguments.of("no effect starts a fresh cycle", VitalityCadence.NO_EFFECT, 50),
        Arguments.of("plenty left changes nothing", 45, VitalityCadence.NO_CHANGE),
        Arguments.of("low on time extends by a whole heal interval", 30, 80),
        Arguments.of("nearly gone extends by a whole heal interval", 11, 61));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("durations")
  @DisplayName("chooses the duration to apply from what is left")
  void choosesDuration(final String label, final int remaining, final int expected) {
    assertEquals(expected, VitalityCadence.durationToApply(remaining));
  }

  @Test
  @DisplayName("refreshing every second heals on vanilla's Regeneration I rhythm")
  void healsOnVanillaRhythm() {
    final List<Integer> healTicks = simulateHeals();

    assertTrue(healTicks.size() >= SIMULATED_TICKS / VANILLA_REGENERATION_ONE_INTERVAL - 1);
    for (int i = 1; i < healTicks.size(); i++) {
      assertEquals(
          VANILLA_REGENERATION_ONE_INTERVAL,
          healTicks.get(i) - healTicks.get(i - 1),
          "heals should land exactly 50 ticks apart");
    }
  }

  private static List<Integer> simulateHeals() {
    final List<Integer> healTicks = new ArrayList<>();
    int remaining = VitalityCadence.NO_EFFECT;
    for (int tick = 0; tick < SIMULATED_TICKS; tick++) {
      if (tick % CHECK_EVERY_TICKS == 0) {
        final int applied = VitalityCadence.durationToApply(remaining);
        if (applied != VitalityCadence.NO_CHANGE) {
          remaining = applied;
        }
      }
      if (remaining > 0) {
        if (remaining % VANILLA_REGENERATION_ONE_INTERVAL == 0) {
          healTicks.add(tick);
        }
        remaining--;
        if (remaining == 0) {
          remaining = VitalityCadence.NO_EFFECT;
        }
      }
    }
    return healTicks;
  }
}
