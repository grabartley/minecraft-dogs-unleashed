package com.grahambartley.dogsunleashed.entity.trick;

import java.util.List;
import java.util.Optional;
import java.util.function.IntUnaryOperator;

public final class TrickLearningRules {

  public static final int REWARDS_TO_LEARN = 5;
  public static final int REWARD_WINDOW_TICKS = 5 * 20;
  public static final int OFFER_ONE_IN_TICKS = 10 * 60 * 20;
  public static final double OWNER_RANGE = 8.0;
  public static final long NEVER = Long.MIN_VALUE;
  public static final float LEARNED_BARK_PITCH_BOOST = 1.2f;

  private TrickLearningRules() {}

  public static int clampRewards(final int rewards) {
    return Math.clamp(rewards, 0, REWARDS_TO_LEARN);
  }

  public static boolean isLearned(final int rewards) {
    return rewards >= REWARDS_TO_LEARN;
  }

  public static float barkPitchBoost(final boolean learnedTrick) {
    return learnedTrick ? LEARNED_BARK_PITCH_BOOST : 1.0f;
  }

  public static boolean mayOffer(
      final boolean tamed, final boolean idle, final double ownerDistance) {
    return tamed && idle && ownerDistance <= OWNER_RANGE;
  }

  public static boolean rollsOffer(final IntUnaryOperator random) {
    return random.applyAsInt(OFFER_ONE_IN_TICKS) == 0;
  }

  public static boolean isWindowOpen(final long offeredAt, final long now) {
    return offeredAt != NEVER && now >= offeredAt && now - offeredAt <= REWARD_WINDOW_TICKS;
  }

  public static Optional<DogTrick> pickUnlearned(
      final TrickProgress progress, final IntUnaryOperator random) {
    final List<DogTrick> unlearned = List.copyOf(progress.unlearned());
    if (unlearned.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(unlearned.get(random.applyAsInt(unlearned.size())));
  }
}
