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

class DogSleepControllerTest {

  private static final long DAY = DogSleepController.DAY_LENGTH_TICKS;
  private static final long NIGHT = DogSleepController.NIGHT_START_TICK;

  static Stream<Arguments> worldTimes() {
    return Stream.of(
        Arguments.of("start of day one", 0L, 0L),
        Arguments.of("dusk on day one", NIGHT, NIGHT),
        Arguments.of("the exact day boundary wraps to zero", DAY, 0L),
        Arguments.of("early on day two", DAY + 1000L, 1000L),
        Arguments.of("dusk on day five", DAY * 4 + NIGHT, NIGHT));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("worldTimes")
  @DisplayName("world time reduces to a time of day inside a single day")
  void worldTimeReducesToTimeOfDay(final String label, final long worldTime, final long expected) {
    assertEquals(expected, DogSleepController.timeOfDay(worldTime));
  }

  @Test
  @DisplayName("night begins at the night start tick and runs to the end of the day")
  void nightSpansFromDuskToDayEnd() {
    assertFalse(DogSleepController.isNight(0L));
    assertFalse(DogSleepController.isNight(NIGHT - 1));
    assertTrue(DogSleepController.isNight(NIGHT));
    assertTrue(DogSleepController.isNight(DAY - 1));
  }

  static Stream<Arguments> suppressionCases() {
    return Stream.of(
        Arguments.of("woken moments ago, still night", 0, NIGHT, true),
        Arguments.of("woken moments ago, sun already up", 0, 0L, false),
        Arguments.of("the first tick of night still suppresses", 0, NIGHT, true),
        Arguments.of("one tick before night does not suppress", 0, NIGHT - 1, false),
        Arguments.of("late in the same night", (int) (DAY - 1), NIGHT, true),
        Arguments.of("a full day elapsed, even at night", (int) DAY, NIGHT, false),
        Arguments.of("more than a day elapsed at night", (int) DAY + 500, NIGHT, false),
        Arguments.of("a full day elapsed during the day", (int) DAY, 0L, false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("suppressionCases")
  @DisplayName("auto-sleep stays suppressed only during the same night the dog was woken")
  void suppressionHoldsForTheRestOfTheNight(
      final String label, final int elapsed, final long currentTimeOfDay, final boolean expected) {
    assertEquals(
        expected, DogSleepController.shouldKeepAutoSleepSuppressed(elapsed, currentTimeOfDay));
  }

  @Test
  @DisplayName("suppression lapses at sunrise rather than lingering into the day")
  void suppressionLapsesAtSunrise() {
    assertTrue(DogSleepController.shouldKeepAutoSleepSuppressed(100, DAY - 1));
    assertFalse(DogSleepController.shouldKeepAutoSleepSuppressed(100, 0L));
  }

  @Test
  @DisplayName("the elapsed cap releases suppression a dog would otherwise carry forever")
  void elapsedCapReleasesSuppression() {
    assertTrue(DogSleepController.shouldKeepAutoSleepSuppressed((int) DAY - 1, NIGHT));
    assertFalse(DogSleepController.shouldKeepAutoSleepSuppressed((int) DAY, NIGHT));
  }
}
