package com.grahambartley.dogsunleashed.entity.trick;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class TrickProgress {

  public static final TrickProgress NONE = new TrickProgress(new EnumMap<>(DogTrick.class));

  private final Map<DogTrick, Integer> rewards;

  private TrickProgress(final EnumMap<DogTrick, Integer> rewards) {
    this.rewards = Collections.unmodifiableMap(rewards);
  }

  public static TrickProgress of(final Map<DogTrick, Integer> rewards) {
    final EnumMap<DogTrick, Integer> clamped = new EnumMap<>(DogTrick.class);
    rewards.forEach(
        (trick, count) -> {
          final int value = TrickLearningRules.clampRewards(count);
          if (value > 0) {
            clamped.put(trick, value);
          }
        });
    return new TrickProgress(clamped);
  }

  public int rewardsFor(final DogTrick trick) {
    return this.rewards.getOrDefault(trick, 0);
  }

  public boolean isLearned(final DogTrick trick) {
    return TrickLearningRules.isLearned(this.rewardsFor(trick));
  }

  public Set<DogTrick> learned() {
    return this.learnedSet();
  }

  public Set<DogTrick> unlearned() {
    return EnumSet.complementOf(this.learnedSet());
  }

  private EnumSet<DogTrick> learnedSet() {
    final EnumSet<DogTrick> learned = EnumSet.noneOf(DogTrick.class);
    for (final DogTrick trick : DogTrick.values()) {
      if (this.isLearned(trick)) {
        learned.add(trick);
      }
    }
    return learned;
  }

  public boolean learnsWithNextReward(final DogTrick trick) {
    return !this.isLearned(trick) && TrickLearningRules.isLearned(this.rewardsFor(trick) + 1);
  }

  public TrickProgress reward(final DogTrick trick) {
    final EnumMap<DogTrick, Integer> next = new EnumMap<>(DogTrick.class);
    next.putAll(this.rewards);
    next.put(trick, this.rewardsFor(trick) + 1);
    return of(next);
  }

  public Map<DogTrick, Integer> rewards() {
    return this.rewards;
  }

  @Override
  public boolean equals(final Object other) {
    return other instanceof TrickProgress progress && this.rewards.equals(progress.rewards);
  }

  @Override
  public int hashCode() {
    return this.rewards.hashCode();
  }

  @Override
  public String toString() {
    return "TrickProgress" + this.rewards;
  }
}
