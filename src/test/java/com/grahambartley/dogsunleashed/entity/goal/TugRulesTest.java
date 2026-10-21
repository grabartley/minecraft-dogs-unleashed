package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.goal.TugRules.Outcome;
import com.grahambartley.dogsunleashed.entity.goal.TugRules.Readiness;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class TugRulesTest {

  private static final double EPSILON = 1.0e-9;

  @ParameterizedTest(name = "{0} ticks since the last pull -> {1}")
  @CsvSource({"0, 0.0", "4, 0.0", "5, 0.18", "8, 0.18", "12, 0.18", "13, 0.08", "200, 0.08"})
  @DisplayName("a pull in a steady rhythm counts most, a slow pull less and mashing not at all")
  void playerPullRewardsRhythm(final long gap, final double expected) {
    assertEquals(expected, TugRules.playerPull(gap), EPSILON);
  }

  @ParameterizedTest(name = "health {0}, puppy {1}, bond step {2} -> {3}")
  @CsvSource({
    "10, false, 0, 0.004",
    "17, false, 0, 0.0068",
    "25, false, 0, 0.010",
    "30, false, 0, 0.012",
    "30, false, 4, 0.0144",
    "30, true, 0, 0.006",
    "25, true, 2, 0.0055",
    "6, false, 0, 0.004"
  })
  @DisplayName("bigger dogs and closer bonds pull harder, puppies pull half as hard")
  void dogStrengthFollowsSizeBondAndAge(
      final double health, final boolean puppy, final int bondStep, final double expected) {
    assertEquals(expected, TugRules.dogStrength(health, puppy, bondStep), EPSILON);
  }

  @ParameterizedTest(name = "strength {0}, roll {1} -> {2}")
  @CsvSource({
    "0.01, 0.0, 0.0075",
    "0.01, 0.5, 0.01",
    "0.01, 1.0, 0.0125",
    "0.01, -1.0, 0.0075",
    "0.01, 2.0, 0.0125",
    "0.0, 0.5, 0.0"
  })
  @DisplayName("each tick the dog pulls within a quarter either side of its strength")
  void dogPullJitters(final double strength, final double roll, final double expected) {
    assertEquals(expected, TugRules.dogPull(strength, roll), EPSILON);
  }

  @ParameterizedTest(name = "{0} + {1} -> {2}")
  @CsvSource({"0, 0.18, 0.18", "0.9, 0.18, 1.0", "-0.99, -0.05, -1.0", "0.5, -0.2, 0.3"})
  @DisplayName("the balance never passes either end of the rope")
  void balanceIsClamped(final double balance, final double change, final double expected) {
    assertEquals(expected, TugRules.balanceAfter(balance, change), EPSILON);
  }

  @ParameterizedTest(name = "balance {0} -> {1}")
  @CsvSource({
    "1.0, PLAYER_WINS",
    "0.999, ONGOING",
    "0, ONGOING",
    "-0.999, ONGOING",
    "-1.0, DOG_WINS"
  })
  @DisplayName("the game ends when the rope reaches either end")
  void outcomeAtTheEnds(final double balance, final Outcome expected) {
    assertEquals(expected, TugRules.outcomeOf(balance));
  }

  @ParameterizedTest(name = "{0} ticks, player {1} blocks away -> {2}")
  @CsvSource({
    "0, 1.0, false",
    "599, 6.0, false",
    "600, 1.0, true",
    "10, 6.1, true",
    "10, 20.0, true"
  })
  @DisplayName("a tug stops after 30 seconds or once the player walks more than 6 blocks off")
  void abandonedByTimeOrDistance(final long ticks, final double distance, final boolean expected) {
    assertEquals(expected, TugRules.isAbandoned(ticks, distance));
  }

  @ParameterizedTest(name = "balance {0} -> {1}")
  @CsvSource({"-1, 0", "-0.5, 0.25", "0, 0.5", "0.5, 0.75", "1, 1", "-4, 0", "4, 1"})
  @DisplayName("the meter shows your share of the rope from 0 to 1")
  void meterFill(final double balance, final float expected) {
    assertEquals(expected, TugRules.meterFill(balance), 1.0e-6f);
  }

  static Stream<Arguments> readiness() {
    return Stream.of(
        Arguments.of(
            "ready", new Readiness(true, false, false, false, false, false, true, false), true),
        Arguments.of(
            "not the owner",
            new Readiness(false, false, false, false, false, false, true, false),
            false),
        Arguments.of(
            "sneaking", new Readiness(true, true, false, false, false, false, true, false), false),
        Arguments.of(
            "already carrying",
            new Readiness(true, false, true, false, false, false, true, false),
            false),
        Arguments.of(
            "playing fetch",
            new Readiness(true, false, false, true, false, false, true, false),
            false),
        Arguments.of(
            "asleep", new Readiness(true, false, false, false, true, false, true, false), false),
        Arguments.of(
            "ridden or hitched",
            new Readiness(true, false, false, false, false, true, true, false),
            false),
        Arguments.of(
            "in the air",
            new Readiness(true, false, false, false, false, false, false, false),
            false),
        Arguments.of(
            "just hurt",
            new Readiness(true, false, false, false, false, false, true, true),
            false));
  }

  @ParameterizedTest(name = "{0} -> {2}")
  @MethodSource("readiness")
  @DisplayName("only an owner standing up can start a tug with a dog that is free and grounded")
  void mayStart(final String label, final Readiness readiness, final boolean expected) {
    assertEquals(expected, readiness.mayStart(), label);
  }
}
