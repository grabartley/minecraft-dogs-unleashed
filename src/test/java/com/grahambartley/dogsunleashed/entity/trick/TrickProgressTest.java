package com.grahambartley.dogsunleashed.entity.trick;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class TrickProgressTest {

  private static TrickProgress rewarded(final DogTrick trick, final int times) {
    TrickProgress progress = TrickProgress.NONE;
    for (int i = 0; i < times; i++) {
      progress = progress.reward(trick);
    }
    return progress;
  }

  @ParameterizedTest(name = "{0} rewards -> learned {1}")
  @CsvSource({"0, false", "1, false", "4, false", "5, true", "9, true"})
  @DisplayName("a trick is learned after five rewards")
  void learnsAfterFiveRewards(final int rewards, final boolean learned) {
    final TrickProgress progress = rewarded(DogTrick.BEG, rewards);
    assertEquals(learned, progress.isLearned(DogTrick.BEG));
    assertEquals(Math.min(rewards, 5), progress.rewardsFor(DogTrick.BEG));
  }

  @ParameterizedTest
  @EnumSource(DogTrick.class)
  @DisplayName("rewarding one trick leaves the others untouched")
  void rewardsAreCountedPerTrick(final DogTrick trick) {
    final TrickProgress progress = rewarded(trick, 5);
    assertEquals(Set.of(trick), progress.learned());
    assertEquals(EnumSet.complementOf(EnumSet.of(trick)), progress.unlearned());
  }

  @Test
  @DisplayName("a new dog knows no tricks and can learn all of them")
  void noneKnowsNothing() {
    assertTrue(TrickProgress.NONE.learned().isEmpty());
    assertEquals(EnumSet.allOf(DogTrick.class), TrickProgress.NONE.unlearned());
  }

  @Test
  @DisplayName("rewarding returns a new progress and never changes the old one")
  void rewardIsImmutable() {
    final TrickProgress before = rewarded(DogTrick.ROLL_OVER, 2);
    before.reward(DogTrick.ROLL_OVER);
    assertEquals(2, before.rewardsFor(DogTrick.ROLL_OVER));
  }

  @ParameterizedTest(name = "saved {0} reads as {1}")
  @CsvSource({"-3, 0", "0, 0", "3, 3", "5, 5", "40, 5"})
  @DisplayName("saved counts are clamped so a bad save can't break learning")
  void ofClampsCounts(final int saved, final int expected) {
    final TrickProgress progress = TrickProgress.of(Map.of(DogTrick.PLAY_DEAD, saved));
    assertEquals(expected, progress.rewardsFor(DogTrick.PLAY_DEAD));
    assertEquals(expected == 0, progress.rewards().isEmpty());
  }

  @Test
  @DisplayName("two progresses with the same counts are equal")
  void equalityFollowsCounts() {
    assertEquals(rewarded(DogTrick.BEG, 3), TrickProgress.of(Map.of(DogTrick.BEG, 3)));
    assertFalse(rewarded(DogTrick.BEG, 3).equals(rewarded(DogTrick.BEG, 2)));
  }
}
