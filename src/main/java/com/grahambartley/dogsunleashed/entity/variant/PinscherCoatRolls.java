package com.grahambartley.dogsunleashed.entity.variant;

import net.minecraft.entity.SpawnReason;

public final class PinscherCoatRolls {
  public static final int ROLL_BOUND = 100;
  public static final int BLACK_TAN_THRESHOLD = 65;
  public static final int RED_THRESHOLD = 85;

  private PinscherCoatRolls() {}

  public static PinscherCoat resolveCoatFromRoll(final SpawnReason spawnReason, final int roll) {
    if (roll < BLACK_TAN_THRESHOLD) {
      return PinscherCoat.BLACK_TAN;
    }
    if (spawnReason == SpawnReason.BREEDING && roll >= RED_THRESHOLD) {
      return PinscherCoat.CHOCOLATE_TAN;
    }
    return PinscherCoat.RED;
  }
}
