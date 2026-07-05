package com.grahambartley.dogsunleashed.entity.variant;

import net.minecraft.entity.SpawnReason;

public final class BeagleCoatRolls {
  public static final int ROLL_BOUND = 100;
  public static final int BREEDING_TRI_1_THRESHOLD = 14;
  public static final int BREEDING_RED_1_THRESHOLD = 26;
  public static final int BREEDING_TRI_2_THRESHOLD = 37;
  public static final int BREEDING_BLACK_WHITE_THRESHOLD = 47;
  public static final int BREEDING_TRI_3_THRESHOLD = 55;
  public static final int BREEDING_RED_2_THRESHOLD = 63;
  public static final int BREEDING_LEMON_1_THRESHOLD = 71;
  public static final int BREEDING_LILAC_1_THRESHOLD = 78;
  public static final int BREEDING_LEMON_2_THRESHOLD = 84;
  public static final int BREEDING_BROWN_WHITE_THRESHOLD = 90;
  public static final int BREEDING_BROWN_WHITE_TAN_THRESHOLD = 95;

  public static final int NATURAL_TRI_1_THRESHOLD = 18;
  public static final int NATURAL_RED_1_THRESHOLD = 33;
  public static final int NATURAL_TRI_2_THRESHOLD = 47;
  public static final int NATURAL_TRI_3_THRESHOLD = 57;
  public static final int NATURAL_RED_2_THRESHOLD = 67;
  public static final int NATURAL_LEMON_1_THRESHOLD = 77;
  public static final int NATURAL_BROWN_WHITE_THRESHOLD = 85;
  public static final int NATURAL_BROWN_WHITE_TAN_THRESHOLD = 93;

  private BeagleCoatRolls() {}

  public static BeagleCoat resolveCoatFromRoll(final SpawnReason spawnReason, final int roll) {
    if (spawnReason == SpawnReason.BREEDING) {
      if (roll < BREEDING_TRI_1_THRESHOLD) {
        return BeagleCoat.TRI_1;
      }
      if (roll < BREEDING_RED_1_THRESHOLD) {
        return BeagleCoat.RED_1;
      }
      if (roll < BREEDING_TRI_2_THRESHOLD) {
        return BeagleCoat.TRI_2;
      }
      if (roll < BREEDING_BLACK_WHITE_THRESHOLD) {
        return BeagleCoat.BLACK_WHITE;
      }
      if (roll < BREEDING_TRI_3_THRESHOLD) {
        return BeagleCoat.TRI_3;
      }
      if (roll < BREEDING_RED_2_THRESHOLD) {
        return BeagleCoat.RED_2;
      }
      if (roll < BREEDING_LEMON_1_THRESHOLD) {
        return BeagleCoat.LEMON_1;
      }
      if (roll < BREEDING_LILAC_1_THRESHOLD) {
        return BeagleCoat.LILAC_1;
      }
      if (roll < BREEDING_LEMON_2_THRESHOLD) {
        return BeagleCoat.LEMON_2;
      }
      if (roll < BREEDING_BROWN_WHITE_THRESHOLD) {
        return BeagleCoat.BROWN_WHITE;
      }
      if (roll < BREEDING_BROWN_WHITE_TAN_THRESHOLD) {
        return BeagleCoat.BROWN_WHITE_TAN;
      }
      return BeagleCoat.LILAC_2;
    }
    if (roll < NATURAL_TRI_1_THRESHOLD) {
      return BeagleCoat.TRI_1;
    }
    if (roll < NATURAL_RED_1_THRESHOLD) {
      return BeagleCoat.RED_1;
    }
    if (roll < NATURAL_TRI_2_THRESHOLD) {
      return BeagleCoat.TRI_2;
    }
    if (roll < NATURAL_TRI_3_THRESHOLD) {
      return BeagleCoat.TRI_3;
    }
    if (roll < NATURAL_RED_2_THRESHOLD) {
      return BeagleCoat.RED_2;
    }
    if (roll < NATURAL_LEMON_1_THRESHOLD) {
      return BeagleCoat.LEMON_1;
    }
    if (roll < NATURAL_BROWN_WHITE_THRESHOLD) {
      return BeagleCoat.BROWN_WHITE;
    }
    if (roll < NATURAL_BROWN_WHITE_TAN_THRESHOLD) {
      return BeagleCoat.BROWN_WHITE_TAN;
    }
    return BeagleCoat.LEMON_2;
  }
}
