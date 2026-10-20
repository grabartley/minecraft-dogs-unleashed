package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class DogAgingRulesTest {

  static Stream<Arguments> elapsedCases() {
    return Stream.of(
        Arguments.of(DogAgingRules.NEVER_SEEN, 500L, 0L),
        Arguments.of(100L, 100L, 0L),
        Arguments.of(100L, 101L, 1L),
        Arguments.of(100L, 24_100L, 24_000L),
        Arguments.of(500L, 100L, 0L));
  }

  @ParameterizedTest(name = "last seen {0}, now {1} -> {2}")
  @MethodSource("elapsedCases")
  @DisplayName("elapsed world time counts forward only and starts at zero")
  void elapsedSince(final long lastSeen, final long now, final long expected) {
    assertEquals(expected, DogAgingRules.elapsedSince(lastSeen, now));
  }

  static Stream<Arguments> agesCases() {
    return Stream.of(
        Arguments.of(true, false, false, DireWolfGrowthStage.YEARLING, false, true),
        Arguments.of(false, false, false, DireWolfGrowthStage.YEARLING, false, false),
        Arguments.of(true, true, false, DireWolfGrowthStage.PUP, false, false),
        Arguments.of(true, false, true, DireWolfGrowthStage.YEARLING, false, false),
        Arguments.of(true, false, false, DireWolfGrowthStage.YEARLING, true, false),
        Arguments.of(true, false, false, DireWolfGrowthStage.ADULT, true, true),
        Arguments.of(true, false, false, DireWolfGrowthStage.PRIME, true, true),
        Arguments.of(true, false, false, DireWolfGrowthStage.ALPHA, true, true),
        Arguments.of(false, false, false, DireWolfGrowthStage.ALPHA, true, false));
  }

  @ParameterizedTest(name = "enabled {0}, baby {1}, undead {2}, stage {3}, staged {4} -> {5}")
  @MethodSource("agesCases")
  @DisplayName("only living adults age, and staged breeds only from their adult stage")
  void ages(
      final boolean enabled,
      final boolean baby,
      final boolean undead,
      final DireWolfGrowthStage stage,
      final boolean growsThroughStages,
      final boolean expected) {
    assertEquals(expected, DogAgingRules.ages(enabled, baby, undead, stage, growsThroughStages));
  }

  @ParameterizedTest(name = "enabled {0}, age {1} ticks, senior after {2} days -> {3}")
  @CsvSource({
    "true, 0, 1, false",
    "true, 23999, 1, false",
    "true, 24000, 1, true",
    "true, 2399999, 100, false",
    "true, 2400000, 100, true",
    "false, 2400000, 100, false",
    "false, 99999999, 1, false"
  })
  @DisplayName("a dog is senior once its adult age reaches the configured days")
  void isSenior(
      final boolean enabled, final long ageTicks, final int days, final boolean expected) {
    assertEquals(expected, DogAgingRules.isSenior(enabled, ageTicks, days));
  }

  @ParameterizedTest(name = "parents senior {0} and {1} -> legacy {2}")
  @CsvSource({"false, false, false", "true, false, true", "false, true, true", "true, true, true"})
  @DisplayName("a pup inherits the legacy when either parent is senior")
  void inheritsLegacy(final boolean parentA, final boolean parentB, final boolean expected) {
    assertEquals(expected, DogAgingRules.inheritsLegacy(parentA, parentB));
  }

  @ParameterizedTest(name = "legacy {0} -> tricks x{1}, bond +{2}")
  @CsvSource({"false, 1, 0", "true, 2, 1"})
  @DisplayName("legacy pups learn tricks twice as fast and start one bond step higher")
  void legacyBonuses(final boolean legacy, final int trickMultiplier, final int bondSteps) {
    assertEquals(trickMultiplier, DogAgingRules.trickLearningMultiplier(legacy));
    assertEquals(bondSteps, DogAgingRules.startingBondSteps(legacy));
  }
}
