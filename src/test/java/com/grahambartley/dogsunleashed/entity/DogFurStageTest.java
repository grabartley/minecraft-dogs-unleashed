package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class DogFurStageTest {

  static Stream<Arguments> ladder() {
    return Stream.of(
        Arguments.of(DogFurStage.TRIMMED, 0, false, false, DogFurStage.NATURAL),
        Arguments.of(DogFurStage.NATURAL, 5, false, true, DogFurStage.SHAGGY),
        Arguments.of(DogFurStage.SHAGGY, 12, false, true, DogFurStage.OVERGROWN),
        Arguments.of(DogFurStage.OVERGROWN, 20, true, true, DogFurStage.OVERGROWN));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("ladder")
  @DisplayName(
      "each stage has the botch chance, speed penalty, trim rule and next stage from the spec")
  void stageLadder(
      final DogFurStage stage,
      final int botchChance,
      final boolean slows,
      final boolean trimmable,
      final DogFurStage next) {
    assertEquals(botchChance, stage.botchChancePercent());
    assertEquals(slows, stage.slowsMovement());
    assertEquals(trimmable, stage.canBeTrimmed());
    assertEquals(next, stage.next());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogFurStage.class)
  @DisplayName("a stage survives a round trip through its ordinal")
  void ordinalRoundTrip(final DogFurStage stage) {
    assertEquals(stage, DogFurStage.fromOrdinal(stage.ordinal()));
  }

  @ParameterizedTest(name = "ordinal {0}")
  @CsvSource({"-1", "4", "99"})
  @DisplayName("an unknown ordinal falls back to the natural coat")
  void unknownOrdinalIsNatural(final int ordinal) {
    assertEquals(DogFurStage.NATURAL, DogFurStage.fromOrdinal(ordinal));
  }

  @ParameterizedTest(name = "{0} capped at {1} is {2}")
  @CsvSource({
    "OVERGROWN, SHAGGY, SHAGGY",
    "SHAGGY, SHAGGY, SHAGGY",
    "NATURAL, SHAGGY, NATURAL",
    "TRIMMED, OVERGROWN, TRIMMED"
  })
  @DisplayName("atMost clamps a stage to a breed's cap")
  void atMostClamps(final DogFurStage stage, final DogFurStage cap, final DogFurStage expected) {
    assertEquals(expected, stage.atMost(cap));
  }

  @ParameterizedTest(name = "{0} above {1} is {2}")
  @CsvSource({
    "OVERGROWN, TRIMMED, 3",
    "SHAGGY, TRIMMED, 2",
    "NATURAL, TRIMMED, 1",
    "TRIMMED, NATURAL, 0"
  })
  @DisplayName("stagesAbove counts the stages a trim removes and never goes negative")
  void stagesAbove(final DogFurStage stage, final DogFurStage lower, final int expected) {
    assertEquals(expected, stage.stagesAbove(lower));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogFurStage.class)
  @DisplayName("each stage has its own fur lang key")
  void translationKey(final DogFurStage stage) {
    assertEquals(
        "fur.dogs-unleashed." + stage.name().toLowerCase(Locale.ROOT), stage.translationKey());
  }
}
