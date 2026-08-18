package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class DogSleepWindowTest {

  private static final long MIDDAY = 6000;
  private static final long DUSK = 13500;
  private static final long MIDNIGHT = 18000;
  private static final long DAWN = 23500;
  private static final long PUPPY_BEDTIME = 11500;

  static Stream<Arguments> adultCases() {
    return Stream.of(
        Arguments.of("midday", MIDDAY, false),
        Arguments.of("just after nightfall", DUSK, true),
        Arguments.of("midnight", MIDNIGHT, true),
        Arguments.of("after sunrise", DAWN, false),
        Arguments.of("exactly nightfall", DogSleepWindow.NIGHT_START_TICK, true),
        Arguments.of("exactly sunrise", DogSleepWindow.SUNRISE_TICK, false));
  }

  @ParameterizedTest(name = "an adult dog at {0} sleeps={2}")
  @MethodSource("adultCases")
  @DisplayName("adult dogs keep the night")
  void adultDogsSleepAtNight(final String label, final long timeOfDay, final boolean expected) {
    assertEquals(expected, DogSleepWindow.isSleepTime(timeOfDay, false, false));
  }

  static Stream<Arguments> puppyCases() {
    return Stream.of(
        Arguments.of("midday", MIDDAY, false),
        Arguments.of("two hours before adult bedtime", PUPPY_BEDTIME, true),
        Arguments.of("one hour past sunrise", DAWN, true),
        Arguments.of("the new day", 100L, false));
  }

  @ParameterizedTest(name = "a puppy at {0} sleeps={2}")
  @MethodSource("puppyCases")
  @DisplayName("puppies turn in early and lie in")
  void puppiesTurnInEarlyAndLieIn(
      final String label, final long timeOfDay, final boolean expected) {
    assertEquals(expected, DogSleepWindow.isSleepTime(timeOfDay, true, false));
  }

  @ParameterizedTest(name = "an undead dog inverts the schedule at tick {0}")
  @ValueSource(longs = {0, 6000, 11500, 13500, 18000, 23500, 23999})
  @DisplayName("an undead dog sleeps exactly when a living one of the same age would be awake")
  void undeadInvertsTheLivingSchedule(final long timeOfDay) {
    for (final boolean baby : new boolean[] {false, true}) {
      assertNotEquals(
          DogSleepWindow.isSleepTime(timeOfDay, baby, false),
          DogSleepWindow.isSleepTime(timeOfDay, baby, true),
          "baby=" + baby + " at tick " + timeOfDay);
    }
  }

  @ParameterizedTest(name = "day {0} of the world clock still resolves to the same window")
  @ValueSource(longs = {0, 1, 7, 100})
  @DisplayName("the window reads the time of day out of a monotonic world clock")
  void windowWrapsAcrossWorldDays(final long day) {
    final long worldTime = day * DogSleepWindow.DAY_LENGTH_TICKS + MIDNIGHT;
    assertTrue(DogSleepWindow.isSleepTime(worldTime, false, false));
  }

  static Stream<Arguments> windowCases() {
    return Stream.of(
        Arguments.of("inside a plain window", 500L, 100L, 900L, true),
        Arguments.of("on the start edge", 100L, 100L, 900L, true),
        Arguments.of("on the end edge", 900L, 100L, 900L, false),
        Arguments.of("inside a wrapping window before midnight", 23000L, 22000L, 1000L, true),
        Arguments.of("inside a wrapping window after midnight", 500L, 22000L, 1000L, true),
        Arguments.of("outside a wrapping window", 12000L, 22000L, 1000L, false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("windowCases")
  @DisplayName("isWithinWindow is half-open and handles the midnight wrap")
  void isWithinWindowIsHalfOpenAndWraps(
      final String label,
      final long timeOfDay,
      final long start,
      final long end,
      final boolean expected) {
    assertEquals(expected, DogSleepWindow.isWithinWindow(timeOfDay, start, end));
  }
}
