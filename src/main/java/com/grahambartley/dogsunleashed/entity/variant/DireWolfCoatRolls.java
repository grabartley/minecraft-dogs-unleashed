package com.grahambartley.dogsunleashed.entity.variant;

import net.minecraft.entity.SpawnReason;

public final class DireWolfCoatRolls {
  public static final int NATURAL_SMOKEGREY_THRESHOLD = 30;
  public static final int NATURAL_ASHGREY_THRESHOLD = 57;
  public static final int NATURAL_FROSTGREY_THRESHOLD = 77;
  public static final int NATURAL_SILVER_THRESHOLD = 91;
  public static final int NATURAL_BLACK_THRESHOLD = 98;

  public static final int BREEDING_SMOKEGREY_THRESHOLD = 26;
  public static final int BREEDING_ASHGREY_THRESHOLD = 50;
  public static final int BREEDING_FROSTGREY_THRESHOLD = 72;
  public static final int BREEDING_SILVER_THRESHOLD = 89;
  public static final int BREEDING_BLACK_THRESHOLD = 98;

  private DireWolfCoatRolls() {}

  public static DireWolfCoat resolveCoatFromRoll(final SpawnReason spawnReason, final int roll) {
    if (spawnReason == SpawnReason.BREEDING) {
      return resolve(
          roll,
          BREEDING_SMOKEGREY_THRESHOLD,
          BREEDING_ASHGREY_THRESHOLD,
          BREEDING_FROSTGREY_THRESHOLD,
          BREEDING_SILVER_THRESHOLD,
          BREEDING_BLACK_THRESHOLD);
    }
    return resolve(
        roll,
        NATURAL_SMOKEGREY_THRESHOLD,
        NATURAL_ASHGREY_THRESHOLD,
        NATURAL_FROSTGREY_THRESHOLD,
        NATURAL_SILVER_THRESHOLD,
        NATURAL_BLACK_THRESHOLD);
  }

  private static DireWolfCoat resolve(final int roll, final int... thresholds) {
    for (int index = 0; index < thresholds.length; index++) {
      if (roll < thresholds[index]) {
        return DireWolfCoat.values()[index];
      }
    }
    return DireWolfCoat.WHITE;
  }
}
