package com.grahambartley.dogsunleashed.entity.variant;

import net.minecraft.entity.SpawnReason;

public final class GermanShepherdCoatRolls {
  public static final int ROLL_BOUND = 100;
  public static final int SADDLE_THRESHOLD = 65;
  public static final int SABLE_THRESHOLD = 85;

  private GermanShepherdCoatRolls() {}

  public static GermanShepherdCoat resolveCoatFromRoll(
      final SpawnReason spawnReason, final int roll) {
    if (roll < SADDLE_THRESHOLD) {
      return GermanShepherdCoat.SADDLE;
    }
    if (spawnReason == SpawnReason.BREEDING && roll >= SABLE_THRESHOLD) {
      return GermanShepherdCoat.BLACK;
    }
    return GermanShepherdCoat.SABLE;
  }
}
