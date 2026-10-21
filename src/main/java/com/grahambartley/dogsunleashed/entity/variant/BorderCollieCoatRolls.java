package com.grahambartley.dogsunleashed.entity.variant;

import net.minecraft.entity.SpawnReason;

public final class BorderCollieCoatRolls {
  public static final int ROLL_BOUND = 100;
  public static final int BLACK_WHITE_THRESHOLD = 70;
  public static final int RED_MERLE_THRESHOLD = 85;

  private BorderCollieCoatRolls() {}

  public static BorderCollieCoat resolveCoatFromRoll(
      final SpawnReason spawnReason, final int roll) {
    if (roll < BLACK_WHITE_THRESHOLD) {
      return BorderCollieCoat.BLACK_WHITE;
    }
    if (spawnReason == SpawnReason.BREEDING && roll >= RED_MERLE_THRESHOLD) {
      return BorderCollieCoat.BLUE_MERLE;
    }
    return BorderCollieCoat.RED_MERLE;
  }
}
