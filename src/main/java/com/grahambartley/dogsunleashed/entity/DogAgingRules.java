package com.grahambartley.dogsunleashed.entity;

public final class DogAgingRules {

  public static final long TICKS_PER_DAY = 24000L;
  public static final long NEVER_SEEN = Long.MIN_VALUE;
  public static final double SENIOR_SPEED_FRACTION = -0.10;
  public static final int LEGACY_TRICK_LEARNING_MULTIPLIER = 2;
  public static final int LEGACY_BOND_STEPS = 1;

  private DogAgingRules() {}

  public static long elapsedSince(final long lastSeen, final long now) {
    return lastSeen == NEVER_SEEN ? 0L : Math.max(0L, now - lastSeen);
  }

  public static boolean ages(
      final boolean enabled,
      final boolean baby,
      final boolean undead,
      final DireWolfGrowthStage stage,
      final boolean growsThroughStages) {
    if (!enabled || baby || undead) {
      return false;
    }
    return !growsThroughStages || stage.number() >= DireWolfGrowthStage.ADULT.number();
  }

  public static long seniorAgeTicks(final int seniorAfterDays) {
    return seniorAfterDays * TICKS_PER_DAY;
  }

  public static boolean isSenior(
      final boolean enabled, final long adultAgeTicks, final int seniorAfterDays) {
    return enabled && adultAgeTicks >= seniorAgeTicks(seniorAfterDays);
  }

  public static boolean inheritsLegacy(final boolean parentASenior, final boolean parentBSenior) {
    return parentASenior || parentBSenior;
  }

  public static int trickLearningMultiplier(final boolean legacy) {
    return legacy ? LEGACY_TRICK_LEARNING_MULTIPLIER : 1;
  }

  public static int startingBondSteps(final boolean legacy) {
    return legacy ? LEGACY_BOND_STEPS : 0;
  }
}
