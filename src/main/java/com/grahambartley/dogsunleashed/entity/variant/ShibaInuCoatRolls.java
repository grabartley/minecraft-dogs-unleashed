package com.grahambartley.dogsunleashed.entity.variant;

import net.minecraft.entity.SpawnReason;

public final class ShibaInuCoatRolls {
  public static final int ROLL_BOUND = 100;
  public static final int RED_THRESHOLD = 65;
  public static final int BLACK_THRESHOLD = 85;

  private ShibaInuCoatRolls() {}

  public static ShibaInuCoat resolveCoatFromRoll(final SpawnReason spawnReason, final int roll) {
    if (spawnReason == SpawnReason.BREEDING) {
      if (roll < RED_THRESHOLD) {
        return ShibaInuCoat.RED;
      }
      if (roll < BLACK_THRESHOLD) {
        return ShibaInuCoat.BLACK;
      }
      return ShibaInuCoat.SESAME;
    }
    if (roll < RED_THRESHOLD) {
      return ShibaInuCoat.RED;
    }
    return ShibaInuCoat.BLACK;
  }
}
