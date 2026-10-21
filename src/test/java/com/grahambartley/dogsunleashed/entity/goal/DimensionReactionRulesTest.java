package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

  @ParameterizedTest(name = "{0} ticks, stagger {1} -> {2}")
  @CsvSource({
    "0, 0, false",
    "600, 0, true",
    "599, 0, false",
    "1200, 0, true",
    "590, 10, true",
    "600, 10, false",
    "1, 599, true",
    "600, 1200, true"
  })
  @DisplayName("an uneasy dog whines every thirty seconds, staggered per dog, never at once")
  void whinesNow(final long uneasyTicks, final int stagger, final boolean expected) {
    assertEquals(expected, DimensionReactionRules.whinesNow(uneasyTicks, stagger));
  }

  @ParameterizedTest(name = "{0} ticks, sitting {1} -> {2}")
  @CsvSource({"0, false, true", "50, false, false", "100, false, true", "100, true, false"})
  @DisplayName("an uneasy dog cowers every five seconds unless it is sitting")
  void cowersNow(final long uneasyTicks, final boolean sitting, final boolean expected) {
    assertEquals(expected, DimensionReactionRules.cowersNow(uneasyTicks, sitting));
  }

  @ParameterizedTest(name = "in End {0}, enderman {1} -> {2}")
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("a hunting dog spares endermen only in the End")
  void sparesFromHunt(final boolean inEnd, final boolean enderman, final boolean expected) {
    assertEquals(expected, DimensionReactionRules.sparesFromHunt(inEnd, enderman));
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
