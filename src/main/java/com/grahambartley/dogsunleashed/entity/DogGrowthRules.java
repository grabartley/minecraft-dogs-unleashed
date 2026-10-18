package com.grahambartley.dogsunleashed.entity;

import java.util.Optional;

public final class DogGrowthRules {

  public static final int PERCENT = 100;
  public static final int BATTLES_TO_EVOLVE = 20;
  public static final double BOND_RANGE_BLOCKS = 16.0;
  static final int LEADER_MEMBER = 0;
  static final int PUP_MEMBER = 1;
  static final int GUARANTEED_ADULTS = 3;
  static final int FOUNDING_ADULT_YEARLING_CHANCE = 50;
  static final int PACK_PUP_CHANCE = 40;
  static final int PACK_YEARLING_CHANCE = 40;
  static final int LEADER_PRIME_CHANCE = 20;
  static final int PACK_PRIME_CHANCE = 5;

  private DogGrowthRules() {}

  public static DireWolfGrowthStage packStage(final int memberIndex, final int roll) {
    if (memberIndex == PUP_MEMBER) {
      return DireWolfGrowthStage.PUP;
    }
    if (memberIndex == LEADER_MEMBER && roll >= PERCENT - LEADER_PRIME_CHANCE) {
      return DireWolfGrowthStage.PRIME;
    }
    if (memberIndex <= GUARANTEED_ADULTS) {
      return roll < FOUNDING_ADULT_YEARLING_CHANCE
          ? DireWolfGrowthStage.YEARLING
          : DireWolfGrowthStage.ADULT;
    }
    if (roll < PACK_PUP_CHANCE) {
      return DireWolfGrowthStage.PUP;
    }
    if (roll < PACK_PUP_CHANCE + PACK_YEARLING_CHANCE) {
      return DireWolfGrowthStage.YEARLING;
    }
    if (roll >= PERCENT - PACK_PRIME_CHANCE) {
      return DireWolfGrowthStage.PRIME;
    }
    return DireWolfGrowthStage.ADULT;
  }

  public static float healthAfterStageChange(
      final float currentHealth, final float previousMaxHealth, final float newMaxHealth) {
    final float grown = currentHealth + Math.max(0.0f, newMaxHealth - previousMaxHealth);
    return Math.min(newMaxHealth, grown);
  }

  public static boolean advancesStage(
      final DireWolfGrowthStage stage, final int ticksInStage, final int growthRatePercent) {
    return stage.growsWithTime()
        && growthRatePercent > 0
        && (long) ticksInStage * growthRatePercent >= (long) stage.ticksToGrow() * PERCENT;
  }

  public static int battlesStillNeeded(final int battlesWon, final int battlesToEvolve) {
    return Math.max(0, battlesToEvolve - battlesWon);
  }

  public static boolean canEvolve(
      final DireWolfGrowthStage stage,
      final boolean tamed,
      final int battlesWon,
      final int battlesToEvolve) {
    return stage.evolvesThroughBond()
        && tamed
        && battlesStillNeeded(battlesWon, battlesToEvolve) == 0;
  }

  public static Optional<String> evolutionRefusalKey(
      final DireWolfGrowthStage stage, final int battlesWon, final int battlesToEvolve) {
    if (stage == DireWolfGrowthStage.ALPHA) {
      return Optional.of("message.dogs-unleashed.direwolf.already_alpha");
    }
    if (!stage.evolvesThroughBond()) {
      return Optional.of("message.dogs-unleashed.direwolf.not_prime");
    }
    final int remaining = battlesStillNeeded(battlesWon, battlesToEvolve);
    if (remaining == 1) {
      return Optional.of("message.dogs-unleashed.direwolf.needs_one_battle");
    }
    return remaining > 1
        ? Optional.of("message.dogs-unleashed.direwolf.needs_battles")
        : Optional.empty();
  }
}
