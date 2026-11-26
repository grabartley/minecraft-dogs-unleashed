package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class PettingRulesTest {

  private static final long NOW = 48_000L;
  private static final PettingRules.DogState CALM =
      new PettingRules.DogState(false, false, false, false);

  @ParameterizedTest(name = "sneaking {0}, main hand {1}, empty hand {2} -> pat {3}")
  @CsvSource({
    "true, true, true, true",
    "false, true, true, false",
    "true, false, true, false",
    "true, true, false, false",
    "false, false, false, false",
    "true, false, false, false"
  })
  @DisplayName("only a sneak click with an empty main hand is a pat")
  void onlyASneakClickWithAnEmptyMainHandIsAPat(
      final boolean sneaking,
      final boolean mainHand,
      final boolean emptyHand,
      final boolean expected) {
    assertEquals(expected, PettingRules.isPetGesture(sneaking, mainHand, emptyHand));
  }

  static Stream<Arguments> dogStates() {
    return Stream.of(
        Arguments.of("a calm dog", CALM, true),
        Arguments.of("a sleeping dog", new PettingRules.DogState(true, false, false, false), false),
        Arguments.of(
            "a dog angry at the player",
            new PettingRules.DogState(false, true, false, false),
            false),
        Arguments.of(
            "a dog busy with a trick or errand",
            new PettingRules.DogState(false, false, true, false),
            false),
        Arguments.of(
            "a dog the player is riding",
            new PettingRules.DogState(false, false, false, true),
            false),
        Arguments.of(
            "an angry sleeping dog", new PettingRules.DogState(true, true, false, false), false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("dogStates")
  @DisplayName("a dog takes a pat only when it is calm and free")
  void aDogTakesAPatOnlyWhenCalmAndFree(
      final String label, final PettingRules.DogState state, final boolean expected) {
    assertEquals(expected, PettingRules.welcomes(state, NOW, PettingRules.NEVER));
  }

  @ParameterizedTest(name = "last pat {0} ticks ago -> cooled down {1}")
  @CsvSource({"0, false", "1, false", "29, false", "30, true", "31, true", "6000, true"})
  @DisplayName("a dog waits one pat before taking the next")
  void aDogWaitsOnePatBeforeTheNext(final long ticksAgo, final boolean expected) {
    assertEquals(expected, PettingRules.cooledDown(NOW, NOW - ticksAgo));
    assertEquals(expected, PettingRules.welcomes(CALM, NOW, NOW - ticksAgo));
  }

  @ParameterizedTest(name = "last pat stored at {0}")
  @CsvSource({"-9223372036854775808", "48001", "96000"})
  @DisplayName("a dog never petted, or petted in a later clock, takes a pat now")
  void aDogNeverPettedOrFromALaterClockTakesAPat(final long lastPettedAt) {
    assertEquals(true, PettingRules.cooledDown(NOW, lastPettedAt));
  }

  @ParameterizedTest(name = "cooldown {0} ticks")
  @CsvSource({"30"})
  @DisplayName("the cooldown lasts as long as the player's pat")
  void theCooldownLastsAsLongAsThePat(final int ticks) {
    assertEquals(ticks, PettingRules.COOLDOWN_TICKS);
  }
}
