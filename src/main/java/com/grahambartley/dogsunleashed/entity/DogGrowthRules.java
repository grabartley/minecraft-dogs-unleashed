package com.grahambartley.dogsunleashed.entity;

public final class DogGrowthRules {

  public static final int PERCENT = 100;
  public static final int BATTLES_TO_EVOLVE = 20;
  public static final double BOND_RANGE_BLOCKS = 16.0;
  static final int PUP_MEMBER = 1;
  static final int GUARANTEED_ADULTS = 3;
  static final int FOUNDING_ADULT_YEARLING_CHANCE = 50;
  static final int PACK_PUP_CHANCE = 40;
  static final int PACK_YEARLING_CHANCE = 40;

  private DogGrowthRules() {}

  public static DireWolfGrowthStage packStage(final int memberIndex, final int roll) {
    if (memberIndex == PUP_MEMBER) {
      return DireWolfGrowthStage.PUP;
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
    return DireWolfGrowthStage.ADULT;
  }

  public static float healthAfterStageChange(
      final float currentHealth, final float previousMaxHealth, final float newMaxHealth) {
    final float grown = currentHealth + Math.max(0.0f, newMaxHealth - previousMaxHealth);
    return Math.min(newMaxHealth, grown);
  }

  public static boolean advancesStage(final DireWolfGrowthStage stage, final int ticksInStage) {
    return stage.growsWithTime() && ticksInStage >= stage.ticksToGrow();
  }

  public static int battlesStillNeeded(final int battlesWon) {
    return Math.max(0, BATTLES_TO_EVOLVE - battlesWon);
  }

  public static boolean canEvolve(
      final DireWolfGrowthStage stage, final boolean tamed, final int battlesWon) {
    return stage.evolvesThroughBond() && tamed && battlesStillNeeded(battlesWon) == 0;
  }
}
