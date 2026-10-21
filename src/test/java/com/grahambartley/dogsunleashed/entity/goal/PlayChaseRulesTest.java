package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules.Playground;
import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules.Readiness;
import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules.Spot;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class PlayChaseRulesTest {

  private static final Spot ORIGIN = new Spot(0, 0);

  @ParameterizedTest
  @CsvSource({
    "true, true, false, FREE_ROAM",
    "true, true, true, FREE_ROAM",
    "true, false, false, NONE",
    "true, false, true, NONE",
    "false, false, true, WILD_PACK",
    "false, true, true, WILD_PACK",
    "false, false, false, NONE",
    "false, true, false, NONE"
  })
  void onlyFreeRoamingPetsAndWildPackPupsGetAPlayground(
      final boolean tamed,
      final boolean freeRoaming,
      final boolean wildPackPup,
      final Playground expected) {
    assertEquals(expected, PlayChaseRules.playgroundOf(tamed, freeRoaming, wildPackPup));
  }

  @ParameterizedTest
  @CsvSource({
    "FREE_ROAM, FREE_ROAM, false, true",
    "FREE_ROAM, FREE_ROAM, true, true",
    "WILD_PACK, WILD_PACK, true, true",
    "WILD_PACK, WILD_PACK, false, false",
    "FREE_ROAM, WILD_PACK, true, false",
    "WILD_PACK, FREE_ROAM, true, false",
    "NONE, NONE, true, false",
    "NONE, FREE_ROAM, false, false"
  })
  void petsPlayWithPetsAndPupsOnlyWithTheirOwnPack(
      final Playground a, final Playground b, final boolean samePack, final boolean expected) {
    assertEquals(expected, PlayChaseRules.arePlaymates(a, b, samePack));
  }

  static Stream<Arguments> readiness() {
    final Readiness ready =
        new Readiness(Playground.FREE_ROAM, true, true, false, false, false, false);
    return Stream.of(
        Arguments.of(ready, true),
        Arguments.of(
            new Readiness(Playground.WILD_PACK, true, true, false, false, false, false), true),
        Arguments.of(new Readiness(Playground.NONE, true, true, false, false, false, false), false),
        Arguments.of(
            new Readiness(Playground.FREE_ROAM, false, true, false, false, false, false), false),
        Arguments.of(
            new Readiness(Playground.FREE_ROAM, true, false, false, false, false, false), false),
        Arguments.of(
            new Readiness(Playground.FREE_ROAM, true, true, true, false, false, false), false),
        Arguments.of(
            new Readiness(Playground.FREE_ROAM, true, true, false, true, false, false), false),
        Arguments.of(
            new Readiness(Playground.FREE_ROAM, true, true, false, false, true, false), false),
        Arguments.of(
            new Readiness(Playground.FREE_ROAM, true, true, false, false, false, true), false));
  }

  @ParameterizedTest
  @MethodSource("readiness")
  void aDogPlaysOnlyWhenIdleSafeAwakeAndUnhurt(final Readiness readiness, final boolean expected) {
    assertEquals(expected, readiness.mayPlay());
  }

  @ParameterizedTest
  @CsvSource({"false, 0, true", "false, 1, false", "true, 0, true", "true, 3, false"})
  void aGameStartsOnlyOnTheWinningRoll(
      final boolean playful, final int roll, final boolean expected) {
    assertEquals(expected, PlayChaseRules.startsGame(playful, bound -> roll));
  }

  @ParameterizedTest
  @CsvSource({"false, 12", "true, 4"})
  void playfulDogsStartGamesThreeTimesAsOften(final boolean playful, final int oneIn) {
    assertEquals(oneIn, PlayChaseRules.startOneIn(playful));
    final int[] asked = new int[1];
    PlayChaseRules.startsGame(
        playful,
        bound -> {
          asked[0] = bound;
          return 1;
        });
    assertEquals(oneIn, asked[0]);
  }

  @ParameterizedTest
  @CsvSource({"0, 200", "100, 300", "200, 400"})
  void aGameLastsTenToTwentySeconds(final int roll, final int ticks) {
    assertEquals(ticks, PlayChaseRules.gameTicks(bound -> Math.min(roll, bound - 1)));
  }

  @ParameterizedTest
  @CsvSource({"0, 1.0, true", "0, 1.5, true", "0, 1.6, false", "99, 4.0, false", "100, 4.0, true"})
  void rolesSwapOnATagOrAfterFiveSecondsOfChasing(
      final long legTicks, final double distance, final boolean expected) {
    assertEquals(expected, PlayChaseRules.swapsRoles(legTicks, distance));
  }

  @ParameterizedTest
  @CsvSource({
    "0, 0, true",
    "12, 0, true",
    "0, -12, true",
    "8, 8, true",
    "8.5, 8.5, false",
    "12.1, 0, false"
  })
  void thePlayAreaIsTwelveBlocksAroundWhereTheGameStarted(
      final double x, final double z, final boolean expected) {
    assertEquals(expected, PlayChaseRules.isInPlayArea(ORIGIN, new Spot(x, z)));
  }

  @ParameterizedTest
  @CsvSource({
    "0, 0, -1, 0, 6, 0",
    "0, 0, 0, 3, 0, -6",
    "8, 0, 6, 0, 10.5, 0",
    "0, 0, 0, 0, 6, 0",
    "3, 0, 3, 0, 9, 0"
  })
  void theRunnerFleesAwayFromTheChaserButStaysInThePlayArea(
      final double runnerX,
      final double runnerZ,
      final double chaserX,
      final double chaserZ,
      final double expectedX,
      final double expectedZ) {
    final Spot target =
        PlayChaseRules.fleeTarget(ORIGIN, new Spot(runnerX, runnerZ), new Spot(chaserX, chaserZ));
    assertEquals(expectedX, target.x(), 1.0E-9);
    assertEquals(expectedZ, target.z(), 1.0E-9);
    assertTrue(PlayChaseRules.isInPlayArea(ORIGIN, target));
  }

  @ParameterizedTest
  @CsvSource({"0, 0, 4, 6, 2, 3", "-2, 5, 2, -5, 0, 0"})
  void aGameIsCentredBetweenTheTwoDogs(
      final double ax,
      final double az,
      final double bx,
      final double bz,
      final double x,
      final double z) {
    assertEquals(new Spot(x, z), PlayChaseRules.midpoint(new Spot(ax, az), new Spot(bx, bz)));
  }
}
