package com.grahambartley.dogsunleashed.entity.trick;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrickLearningRulesTest {

  @ParameterizedTest(name = "opened at {0}, now {1} -> open {2}")
  @CsvSource({
    "100, 100, true",
    "100, 150, true",
    "100, 200, true",
    "100, 201, false",
    "100, 99, false",
    "-9223372036854775808, 0, false"
  })
  @DisplayName("the reward window stays open for five seconds after the trick")
  void windowLastsFiveSeconds(final long offeredAt, final long now, final boolean open) {
    assertEquals(open, TrickLearningRules.isWindowOpen(offeredAt, now));
  }

  @ParameterizedTest(name = "tamed={0}, idle={1}, owner {2} away -> {3}")
  @CsvSource({
    "true, true, 0, true",
    "true, true, 8, true",
    "true, true, 8.1, false",
    "true, false, 2, false",
    "false, true, 2, false"
  })
  @DisplayName("only an idle tamed dog near its owner shows off a trick")
  void offersOnlyWhenIdleNearOwner(
      final boolean tamed, final boolean idle, final double distance, final boolean expected) {
    assertEquals(expected, TrickLearningRules.mayOffer(tamed, idle, distance));
  }

  @ParameterizedTest(name = "roll {0} -> offer {1}")
  @CsvSource({"0, true", "1, false", "3599, false"})
  @DisplayName("a dog shows off about once every three minutes")
  void rollsOneInThreeMinutes(final int roll, final boolean offers) {
    assertEquals(
        offers,
        TrickLearningRules.rollsOffer(
            bound -> {
              assertEquals(3 * 60 * 20, bound);
              return roll;
            }));
  }

  @ParameterizedTest(name = "roll {0} picks {1}")
  @CsvSource({"0, BEG", "1, ROLL_OVER", "2, PLAY_DEAD"})
  @DisplayName("a dog that knows no tricks may try any of them")
  void picksAnyTrickWhenNoneLearned(final int roll, final DogTrick expected) {
    assertEquals(
        Optional.of(expected), TrickLearningRules.pickUnlearned(TrickProgress.NONE, bound -> roll));
  }

  @Test
  @DisplayName("a dog only tries tricks it hasn't learned yet")
  void skipsLearnedTricks() {
    final TrickProgress progress = TrickProgress.of(Map.of(DogTrick.BEG, 5, DogTrick.PLAY_DEAD, 5));
    assertEquals(
        Optional.of(DogTrick.ROLL_OVER),
        TrickLearningRules.pickUnlearned(
            progress,
            bound -> {
              assertEquals(1, bound);
              return 0;
            }));
  }

  @Test
  @DisplayName("a dog that knows every trick stops showing off")
  void nothingToPickOnceAllLearned() {
    final TrickProgress progress =
        TrickProgress.of(Map.of(DogTrick.BEG, 5, DogTrick.ROLL_OVER, 5, DogTrick.PLAY_DEAD, 5));
    assertTrue(TrickLearningRules.pickUnlearned(progress, bound -> 0).isEmpty());
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"-1, 0, false", "0, 0, false", "4, 4, false", "5, 5, true", "6, 5, true"})
  @DisplayName("reward counts clamp to the learning threshold")
  void clampsAndLearns(final int rewards, final int clamped, final boolean learned) {
    assertEquals(clamped, TrickLearningRules.clampRewards(rewards));
    assertEquals(learned, TrickLearningRules.isLearned(rewards));
  }

  @ParameterizedTest(name = "learned={0} -> pitch x{1}")
  @CsvSource({"true, 1.2", "false, 1.0"})
  @DisplayName("the treat bark is happier on the reward that teaches a trick")
  void barkIsHappierWhenATrickIsLearned(final boolean learned, final float boost) {
    assertEquals(boost, TrickLearningRules.barkPitchBoost(learned));
  }
}
