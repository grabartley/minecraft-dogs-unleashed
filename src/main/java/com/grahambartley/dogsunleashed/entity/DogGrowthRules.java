package com.grahambartley.dogsunleashed.entity;

public final class DogGrowthRules {

  public static final int PERCENT = 100;
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

  public static boolean advancesStage(final int ticksInStage) {
    return ticksInStage >= DireWolfGrowthStage.TICKS_PER_ADULT_STAGE;
  }
}
