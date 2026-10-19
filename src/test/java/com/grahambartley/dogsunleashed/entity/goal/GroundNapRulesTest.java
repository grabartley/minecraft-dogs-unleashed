package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class GroundNapRulesTest {

  private static final GroundNapRules.Situation CALM_AND_IDLE =
      new GroundNapRules.Situation(false, true, false, false, false, false, false, false);

  @Test
  void aNapLastsThreeMinutes() {
    assertEquals(3600, GroundNapRules.NAP_TICKS);
  }

  @Test
  void theDayAndTheNightAreEachOnePeriod() {
    assertEquals(12000, GroundNapRules.PERIOD_TICKS);
  }

  @ParameterizedTest
  @CsvSource({"0, 0", "11999, 0", "12000, 1", "23999, 1", "24000, 2", "36500, 3", "-1, -1"})
  void periodOfSplitsEachDayIntoADayHalfAndANightHalf(final long timeOfDay, final long period) {
    assertEquals(period, GroundNapRules.periodOf(timeOfDay));
  }

  @ParameterizedTest
  @CsvSource({"0, 0", "1000, 1000", "8399, 8399", "8400, 0", "-1, 8399"})
  void dueOffsetLeavesRoomForAWholeNapBeforeThePeriodEnds(final long seed, final long offset) {
    assertEquals(offset, GroundNapRules.dueOffset(seed));
  }

  @ParameterizedTest
  @CsvSource({
    "999, -9223372036854775808, 1000, false",
    "1000, -9223372036854775808, 1000, true",
    "11999, -9223372036854775808, 1000, true",
    "5000, 0, 1000, false",
    "13000, 0, 1000, true",
    "12500, 0, 1000, false",
    "25000, 1, 1000, true",
    "25000, 2, 1000, false"
  })
  void isDueOnceThePeriodReachesTheDogsOffsetAndItHasNotNappedInIt(
      final long timeOfDay, final long lastNappedPeriod, final long seed, final boolean due) {
    assertEquals(due, GroundNapRules.isDue(timeOfDay, lastNappedPeriod, seed));
  }

  @ParameterizedTest
  @CsvSource({"100, 100, false", "3699, 100, false", "3700, 100, true", "9000, 100, true"})
  void isOverOnceTheNapHasRunItsLength(final long now, final long startedAt, final boolean over) {
    assertEquals(over, GroundNapRules.isOver(now, startedAt));
  }

  @ParameterizedTest
  @CsvSource({
    "1000, -9223372036854775808, false",
    "1000, 1000, true",
    "1599, 1000, true",
    "1600, 1000, false"
  })
  void isHurtRecentlyForThirtySecondsAfterAHit(
      final long now, final long lastHurtAt, final boolean recent) {
    assertEquals(recent, GroundNapRules.isHurtRecently(now, lastHurtAt));
  }

  @ParameterizedTest
  @EnumSource(DogCommand.class)
  void napsOnlyOnFollowStayGuardOrFreeRoam(final DogCommand command) {
    final boolean expected =
        command == DogCommand.FOLLOW
            || command == DogCommand.STAY
            || command == DogCommand.GUARD
            || command == DogCommand.FREE_ROAM;
    assertEquals(expected, GroundNapRules.allowsCommand(command));
  }

  @Test
  void aCalmIdleDogOnSolidGroundMayStartANap() {
    assertTrue(CALM_AND_IDLE.isCalm());
    assertTrue(CALM_AND_IDLE.allowsNapToStart());
  }

  static Stream<Arguments> unsafeSituations() {
    return Stream.of(
        Arguments.of(
            "has a target",
            new GroundNapRules.Situation(false, true, true, false, false, false, false, false)),
        Arguments.of(
            "hostile nearby",
            new GroundNapRules.Situation(false, true, false, true, false, false, false, false)),
        Arguments.of(
            "hurt recently",
            new GroundNapRules.Situation(false, true, false, false, true, false, false, false)),
        Arguments.of(
            "ridden",
            new GroundNapRules.Situation(false, true, false, false, false, true, false, false)),
        Arguments.of(
            "leashed",
            new GroundNapRules.Situation(false, true, false, false, false, false, true, false)),
        Arguments.of(
            "playing",
            new GroundNapRules.Situation(false, true, false, false, false, false, false, true)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("unsafeSituations")
  void anUnsafeSituationNeitherStartsNorKeepsANap(
      final String name, final GroundNapRules.Situation situation) {
    assertFalse(situation.isCalm());
    assertFalse(situation.allowsNapToStart());
  }

  static Stream<Arguments> calmButNotReadySituations() {
    return Stream.of(
        Arguments.of(
            "busy",
            new GroundNapRules.Situation(true, true, false, false, false, false, false, false)),
        Arguments.of(
            "off the ground",
            new GroundNapRules.Situation(false, false, false, false, false, false, false, false)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("calmButNotReadySituations")
  void aBusyOrAirborneDogKeepsANapGoingButNeverStartsOne(
      final String name, final GroundNapRules.Situation situation) {
    assertTrue(situation.isCalm());
    assertFalse(situation.allowsNapToStart());
  }
}
