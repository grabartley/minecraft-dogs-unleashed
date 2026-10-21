package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import java.util.OptionalDouble;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class GroundNapRulesTest {

  private static final GroundNapRules.Situation CALM_AND_IDLE =
      new GroundNapRules.Situation(false, true, false, false, false, false, false, false, false);

  @ParameterizedTest
  @CsvSource({"0, 0", "11999, 0", "12000, 1", "23999, 1", "24000, 2", "36500, 3", "-1, -1"})
  void periodOfSplitsEachDayIntoADayHalfAndANightHalf(final long timeOfDay, final long period) {
    assertEquals(period, GroundNapRules.periodOf(timeOfDay));
  }

  @ParameterizedTest
  @CsvSource({"0, 0", "1000, 1000", "5999, 5999", "6000, 0", "-1, 5999"})
  void dueOffsetLeavesEveryDogTwoMinutesToStartAWholeNapBeforeThePeriodEnds(
      final long seed, final long offset) {
    assertEquals(offset, GroundNapRules.dueOffset(seed, GroundNapRules.NAP_TICKS));
  }

  @ParameterizedTest
  @CsvSource({"0, 0", "1000, 1000", "2399, 2399", "2400, 0", "-1, 2399"})
  void aSeniorsLongerNapNarrowsItsOffsetSoTheNapStillFits(final long seed, final long offset) {
    assertEquals(offset, GroundNapRules.dueOffset(seed, GroundNapRules.SENIOR_NAP_TICKS));
  }

  @ParameterizedTest
  @CsvSource({"false, 1.0, 3600", "true, 1.0, 7200", "false, 1.5, 5400", "true, 1.5, 9599"})
  void seniorsNapTwiceAsLongAndLazyDogsLongerStillWithinThePeriod(
      final boolean senior, final double napScale, final long napTicks) {
    assertEquals(napTicks, GroundNapRules.napTicks(senior, napScale));
  }

  @ParameterizedTest
  @CsvSource({
    "false, false, false, true",
    "false, true, false, true",
    "true, false, false, false",
    "true, true, false, true",
    "true, false, true, true"
  })
  void bedSleepersOnlyNapOnTheGroundOnceTheyAreSeniorOrLazy(
      final boolean sleepsInBeds,
      final boolean senior,
      final boolean lazy,
      final boolean napsOnGround) {
    assertEquals(napsOnGround, GroundNapRules.napsOnGround(sleepsInBeds, senior, lazy));
  }

  @Test
  void aLazySeniorsCappedNapStillFitsInThePeriod() {
    final long napTicks = GroundNapRules.napTicks(true, 1.5);
    assertEquals(0, GroundNapRules.dueOffset(12345L, napTicks));
    assertTrue(
        GroundNapRules.isDue(GroundNapRules.START_GRACE_TICKS, GroundNapRules.NEVER, 0L, napTicks));
  }

  @ParameterizedTest
  @CsvSource({
    "999, -9223372036854775808, 1000, false",
    "1000, -9223372036854775808, 1000, true",
    "8400, -9223372036854775808, 1000, true",
    "8401, -9223372036854775808, 1000, false",
    "11999, -9223372036854775808, 1000, false",
    "5000, 0, 1000, false",
    "13000, 0, 1000, true",
    "12500, 0, 1000, false",
    "25000, 1, 1000, true",
    "25000, 2, 1000, false"
  })
  void isDueFromTheDogsOffsetUntilANapWouldRunPastThePeriodOnceAPeriod(
      final long timeOfDay, final long lastNappedPeriod, final long seed, final boolean due) {
    assertEquals(
        due, GroundNapRules.isDue(timeOfDay, lastNappedPeriod, seed, GroundNapRules.NAP_TICKS));
  }

  @ParameterizedTest
  @CsvSource({"999, false", "1000, true", "4800, true", "4801, false", "8400, false"})
  void aSeniorIsOnlyDueWhileItsLongerNapStillFitsInThePeriod(
      final long timeOfDay, final boolean due) {
    assertEquals(
        due,
        GroundNapRules.isDue(
            timeOfDay, GroundNapRules.NEVER, 1000, GroundNapRules.SENIOR_NAP_TICKS));
  }

  @ParameterizedTest
  @CsvSource({
    "100, 100, 3600, false",
    "3699, 100, 3600, false",
    "3700, 100, 3600, true",
    "9000, 100, 3600, true",
    "3700, 100, 7200, false",
    "7299, 100, 7200, false",
    "7300, 100, 7200, true"
  })
  void isOverOnceTheNapHasRunItsLength(
      final long now, final long startedAt, final long napTicks, final boolean over) {
    assertEquals(over, GroundNapRules.isOver(now, startedAt, napTicks));
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
            new GroundNapRules.Situation(
                false, true, true, false, false, false, false, false, false)),
        Arguments.of(
            "hostile nearby",
            new GroundNapRules.Situation(
                false, true, false, true, false, false, false, false, false)),
        Arguments.of(
            "hurt recently",
            new GroundNapRules.Situation(
                false, true, false, false, true, false, false, false, false)),
        Arguments.of(
            "ridden",
            new GroundNapRules.Situation(
                false, true, false, false, false, true, false, false, false)),
        Arguments.of(
            "leashed",
            new GroundNapRules.Situation(
                false, true, false, false, false, false, true, false, false)),
        Arguments.of(
            "playing",
            new GroundNapRules.Situation(
                false, true, false, false, false, false, false, true, false)),
        Arguments.of(
            "owner walked away",
            new GroundNapRules.Situation(
                false, true, false, false, false, false, false, false, true)));
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
            new GroundNapRules.Situation(
                true, true, false, false, false, false, false, false, false)),
        Arguments.of(
            "off the ground",
            new GroundNapRules.Situation(
                false, false, false, false, false, false, false, false, false)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("calmButNotReadySituations")
  void aBusyOrAirborneDogKeepsANapGoingButNeverStartsOne(
      final String name, final GroundNapRules.Situation situation) {
    assertTrue(situation.isCalm());
    assertFalse(situation.allowsNapToStart());
  }

  @ParameterizedTest(name = "now {0}, checked {1} -> {2}")
  @CsvSource({
    "100, -9223372036854775808, true",
    "100, 91, false",
    "100, 90, true",
    "100, 100, false",
    "100, 0, true"
  })
  @DisplayName("the hostile scan runs on the first tick and then every half second")
  void hostileCheckDueEveryTenTicks(final long now, final long checkedAt, final boolean due) {
    assertEquals(due, GroundNapRules.hostileCheckDue(now, checkedAt));
  }

  @ParameterizedTest(name = "follows={0}, distance² {1} -> {2}")
  @CsvSource({"true, 101.0, true", "true, 100.0, false", "false, 400.0, false", "true, , false"})
  @DisplayName("the owner is away only past ten blocks from a dog that follows them")
  void ownerAwayPastTenBlocks(final boolean follows, final Double distance, final boolean away) {
    assertEquals(
        away,
        GroundNapRules.isOwnerAway(
            follows, distance == null ? OptionalDouble.empty() : OptionalDouble.of(distance)));
  }
}
