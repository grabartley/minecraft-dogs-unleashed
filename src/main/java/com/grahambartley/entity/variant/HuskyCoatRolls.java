package com.grahambartley.entity.variant;

import net.minecraft.entity.SpawnReason;

public final class HuskyCoatRolls {
  public static final int ROLL_BOUND = 100;
  public static final int NATURAL_BLACK_WHITE_THRESHOLD = 40;
  public static final int NATURAL_GREY_WHITE_THRESHOLD = 72;
  public static final int NATURAL_RED_WHITE_THRESHOLD = 87;
  public static final int BREEDING_SABLE_THRESHOLD = 93;
  public static final int BREEDING_AGOUTI_THRESHOLD = 97;

  private HuskyCoatRolls() {}

  public static HuskyCoat resolveCoatFromRoll(final SpawnReason spawnReason, final int roll) {
    if (spawnReason == SpawnReason.BREEDING) {
      if (roll < NATURAL_BLACK_WHITE_THRESHOLD) {
        return HuskyCoat.BLACK_WHITE;
      }
      if (roll < NATURAL_GREY_WHITE_THRESHOLD) {
        return HuskyCoat.GREY_WHITE;
      }
      if (roll < NATURAL_RED_WHITE_THRESHOLD) {
        return HuskyCoat.RED_WHITE;
      }
      if (roll < BREEDING_SABLE_THRESHOLD) {
        return HuskyCoat.SABLE;
      }
      if (roll < BREEDING_AGOUTI_THRESHOLD) {
        return HuskyCoat.AGOUTI;
      }
      return HuskyCoat.WHITE;
    }
    if (roll < NATURAL_BLACK_WHITE_THRESHOLD) {
      return HuskyCoat.BLACK_WHITE;
    }
    if (roll < NATURAL_GREY_WHITE_THRESHOLD) {
      return HuskyCoat.GREY_WHITE;
    }
    if (roll < NATURAL_RED_WHITE_THRESHOLD) {
      return HuskyCoat.RED_WHITE;
    }
    return HuskyCoat.SABLE;
  }
}
