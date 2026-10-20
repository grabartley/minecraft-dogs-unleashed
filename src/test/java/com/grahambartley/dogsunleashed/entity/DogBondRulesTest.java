package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogBondRulesTest {

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"-1, 0", "0, 0", "350, 350", "700, 700", "701, 700"})
  @DisplayName("points stay between zero and Devoted")
  void clamp(final int points, final int expected) {
    assertEquals(expected, DogBondRules.clamp(points));
  }

  @ParameterizedTest(name = "time of day {0} -> day {1}")
  @CsvSource({"0, 0", "23999, 0", "24000, 1", "71999, 2", "-1, -1"})
  @DisplayName("the daily caps reset with each in-game day")
  void dayOf(final long timeOfDay, final long expected) {
    assertEquals(expected, DogBondRules.dayOf(timeOfDay));
  }

  @ParameterizedTest(name = "{0}: +{1} with {2} earned today -> +{3}")
  @CsvSource({
    "WALK, 1, 0, 1",
    "WALK, 1, 19, 1",
    "WALK, 1, 20, 0",
    "WALK, 3, 18, 2",
    "PLAY, 4, 16, 4",
    "PLAY, 4, 18, 2",
    "FIGHT, 5, 20, 0",
    "TREAT, 4, 8, 4",
    "TREAT, 4, 12, 0",
    "GROOM, 10, 0, 10",
    "GROOM, 10, 10, 0",
    "GROOM, 10, 25, 0"
  })
  @DisplayName("each source only gives bond up to its daily cap")
  void allowedGain(
      final DogBondRules.Source source, final int amount, final int earned, final int expected) {
    assertEquals(expected, DogBondRules.allowedGain(source, amount, earned));
  }

  @ParameterizedTest(name = "step of {0} blocks counts: {1}")
  @CsvSource({"0.0, false", "0.01, true", "0.4, true", "1.0, true", "1.01, false", "30.0, false"})
  @DisplayName("only walking counts, not standing still or teleporting")
  void countsAsWalkStep(final double step, final boolean expected) {
    assertEquals(expected, DogBondRules.countsAsWalkStep(step));
  }

  @ParameterizedTest(name = "{0} blocks -> {1} points")
  @CsvSource({"0.0, 0", "19.9, 0", "20.0, 1", "59.9, 2", "100.0, 5"})
  @DisplayName("a walk point comes every twenty blocks")
  void walkPoints(final double walked, final int expected) {
    assertEquals(expected, DogBondRules.walkPoints(walked));
  }

  @ParameterizedTest(name = "senior parent {0} -> {1} points")
  @CsvSource({"false, 0", "true, 50"})
  @DisplayName("a pup of a senior starts one bond step higher")
  void startingPoints(final boolean seniorParent, final int expected) {
    assertEquals(expected, DogBondRules.startingPoints(seniorParent));
  }

  @ParameterizedTest(name = "last near {0}, now {1} -> {2}")
  @CsvSource({"-9223372036854775808, 100, 0", "100, 100, 0", "100, 6100, 6000", "6100, 100, 0"})
  @DisplayName("time apart counts forward only and starts at zero")
  void absentFor(final long lastNear, final long now, final long expected) {
    assertEquals(expected, DogBondRules.absentFor(lastNear, now));
  }

  @ParameterizedTest(name = "{0} after {1} ticks apart -> {2}")
  @CsvSource({
    "STRANGER, 6000, false",
    "FAMILIAR, 99999, false",
    "FRIEND, 5999, false",
    "FRIEND, 6000, true",
    "DEVOTED, 6000, true",
    "DEVOTED, 0, false"
  })
  @DisplayName("a dog from Friend up play bows when its owner comes back after five minutes")
  void greetsOnReturn(final DogBondStep step, final long absent, final boolean expected) {
    assertEquals(expected, DogBondRules.greetsOnReturn(step, absent));
  }

  @ParameterizedTest(name = "hostile {0}, {1} blocks from the owner -> {2}")
  @CsvSource({"true, 0.0, true", "true, 16.0, true", "true, 16.1, false", "false, 2.0, false"})
  @DisplayName("only a hostile killed near the owner counts as a fight won together")
  void fightCounts(final boolean hostile, final double distance, final boolean expected) {
    assertEquals(expected, DogBondRules.fightCounts(hostile, distance));
  }

  @ParameterizedTest(name = "owner hurt {0}, {1} ticks left -> {2}")
  @CsvSource({"true, 0, 400", "true, 120, 400", "false, 400, 399", "false, 1, 0", "false, 0, 0"})
  @DisplayName("a rescue lasts twenty seconds and starts over each time the owner is hurt")
  void rescueTicksLeft(final boolean hurt, final int ticksLeft, final int expected) {
    assertEquals(expected, DogBondRules.rescueTicksLeft(hurt, ticksLeft));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "FOLLOW, true",
    "HEEL, true",
    "HUNT, true",
    "FREE_ROAM, true",
    "STAY, false",
    "SIT, false",
    "GUARD, false"
  })
  @DisplayName("a rescue never breaks a stay, sit or guard order")
  void answersRescue(final DogCommand command, final boolean expected) {
    assertEquals(expected, DogBondRules.answersRescue(command));
  }

  @ParameterizedTest(name = "{0}, owner hurt {1}, {2} blocks -> {3}")
  @CsvSource({
    "DEVOTED, true, 10.0, true",
    "DEVOTED, true, 24.0, true",
    "DEVOTED, true, 24.1, false",
    "DEVOTED, false, 3.0, false",
    "COMPANION, true, 3.0, false",
    "STRANGER, true, 3.0, false"
  })
  @DisplayName("only a Devoted dog nearby comes running when its owner is hurt")
  void startsRescue(
      final DogBondStep step, final boolean hurt, final double distance, final boolean expected) {
    assertEquals(expected, DogBondRules.startsRescue(step, hurt, distance));
  }
}
