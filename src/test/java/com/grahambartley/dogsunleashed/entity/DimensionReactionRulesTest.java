package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DimensionReactionRulesTest {

  @ParameterizedTest(name = "in Nether {0}, fire guarded {1} -> {2}")
  @CsvSource({
    "true, false, true",
    "true, true, false",
    "false, false, false",
    "false, true, false"
  })
  @DisplayName("only a dog in the Nether without the Fire Guardian pendant is uneasy")
  void isUneasy(final boolean inNether, final boolean fireGuarded, final boolean expected) {
    assertEquals(expected, DimensionReactionRules.isUneasy(inNether, fireGuarded));
  }

  @ParameterizedTest(name = "uneasy {0} -> {1}")
  @CsvSource({"true, 0.5", "false, 1.0"})
  @DisplayName("an uneasy dog follows from half the usual distance")
  void followDistanceScale(final boolean uneasy, final double expected) {
    assertEquals(expected, DimensionReactionRules.followDistanceScale(uneasy));
  }

  @ParameterizedTest(name = "uneasy {0} is never closer than the closest scale")
  @CsvSource({"true", "false"})
  @DisplayName("the closest follow scale bounds every dimension's scale")
  void closestFollowDistanceScale(final boolean uneasy) {
    assertTrue(
        DimensionReactionRules.closestFollowDistanceScale()
            <= DimensionReactionRules.followDistanceScale(uneasy));
    assertEquals(0.5, DimensionReactionRules.closestFollowDistanceScale());
  }

  @ParameterizedTest(name = "alive {0}, asleep {1}, uneasy {2} -> {3}")
  @CsvSource({
    "true, false, true, true",
    "true, true, true, false",
    "false, false, true, false",
    "true, false, false, false"
  })
  @DisplayName("only a living, awake, uneasy dog shows its unease")
  void runsUnease(
      final boolean alive, final boolean asleep, final boolean uneasy, final boolean expected) {
    assertEquals(expected, DimensionReactionRules.runsUnease(alive, asleep, uneasy));
  }

  @ParameterizedTest(name = "reacted {0}, {1} ticks since, roll {2} -> {3}")
  @CsvSource({
    "false, 0, 7, true",
    "false, 5000, 7, true",
    "true, 0, 0, false",
    "true, 1199, 0, false",
    "true, 1200, 0, true",
    "true, 1200, 1, false",
    "true, 99999, 2399, false",
    "true, 99999, 0, true"
  })
  @DisplayName("an uneasy dog reacts on arrival, then by chance once a minute has passed")
  void actsUneasyNow(
      final boolean reacted, final long ticksSince, final int roll, final boolean expected) {
    assertEquals(expected, DimensionReactionRules.actsUneasyNow(reacted, ticksSince, roll));
  }

  @ParameterizedTest(name = "sitting {0} -> {1}")
  @CsvSource({"false, true", "true, false"})
  @DisplayName("an uneasy dog cowers unless it is sitting")
  void cowersWhenUneasy(final boolean sitting, final boolean expected) {
    assertEquals(expected, DimensionReactionRules.cowersWhenUneasy(sitting));
  }

  @ParameterizedTest(name = "in End {0}, enderman {1} -> {2}")
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("a dog spares endermen only in the End")
  void sparesEnderman(final boolean inEnd, final boolean enderman, final boolean expected) {
    assertEquals(expected, DimensionReactionRules.sparesEnderman(inEnd, enderman));
  }

  @ParameterizedTest(name = "in End {0}, asleep {1}, sitting {2} -> {3}")
  @CsvSource({
    "true, false, false, true",
    "true, true, false, false",
    "true, false, true, false",
    "false, false, false, false"
  })
  @DisplayName("an awake, standing dog in the End watches endermen")
  void watchesEndermen(
      final boolean inEnd, final boolean asleep, final boolean sitting, final boolean expected) {
    assertEquals(expected, DimensionReactionRules.watchesEndermen(inEnd, asleep, sitting));
  }

  @ParameterizedTest(name = "{0} ticks -> {1}")
  @CsvSource({"0, true", "1, false", "59, false", "60, true", "120, true"})
  @DisplayName("a watching dog barks every three seconds")
  void barksNow(final long watchTicks, final boolean expected) {
    assertEquals(expected, DimensionReactionRules.barksNow(watchTicks));
  }
}
