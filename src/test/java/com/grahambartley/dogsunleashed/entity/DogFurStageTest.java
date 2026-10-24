package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
        Arguments.of(DogFurStage.BOTCHED, 0, 50, false, false, DogFurStage.TRIMMED),
        Arguments.of(DogFurStage.TRIMMED, 0, 100, false, false, DogFurStage.NATURAL),
        Arguments.of(DogFurStage.NATURAL, 5, 150, false, true, DogFurStage.SHAGGY),
        Arguments.of(DogFurStage.SHAGGY, 12, 200, false, true, DogFurStage.OVERGROWN),
        Arguments.of(DogFurStage.OVERGROWN, 20, 0, true, true, DogFurStage.OVERGROWN));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("ladder")
  @DisplayName(
      "each stage has the botch chance, growth time, speed penalty, trim rule and next stage")
  void stageLadder(
      final DogFurStage stage,
      final int botchChance,
      final int growthTime,
      final boolean slows,
      final boolean trimmable,
      final DogFurStage next) {
    assertEquals(botchChance, stage.botchChancePercent());
    assertEquals(growthTime, stage.growthTimePercent());
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
  @CsvSource({"-1", "5", "99"})
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
  @DisplayName("each stage's name is translated in the English lang file")
  void translationKeyIsTranslated(final DogFurStage stage) throws IOException {
    try (InputStream in =
        DogFurStageTest.class.getResourceAsStream("/assets/dogs-unleashed/lang/en_us.json")) {
      final JsonObject lang =
          JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
              .getAsJsonObject();
      assertTrue(lang.has(stage.translationKey()), stage.translationKey() + " is not translated");
    }
  }
}
