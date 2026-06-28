package com.grahambartley.entity.variant;

import net.minecraft.entity.SpawnReason;

public final class DachshundCoatRolls {
  public static final int ROLL_BOUND = 100;
  public static final int BREEDING_BLACK_TAN_THRESHOLD = 34;
  public static final int BREEDING_RED_THRESHOLD = 58;
  public static final int BREEDING_CHOCOLATE_TAN_THRESHOLD = 70;
  public static final int BREEDING_CHOCOLATE_CREAM_THRESHOLD = 78;
  public static final int BREEDING_BLACK_CREAM_THRESHOLD = 85;
  public static final int BREEDING_LIGHT_BLACK_CREAM_THRESHOLD = 90;
  public static final int BREEDING_RED_PIEBALD_THRESHOLD = 94;
  public static final int BREEDING_BLACK_TAN_PIEBALD_THRESHOLD = 98;
  public static final int BREEDING_BLUE_TAN_THRESHOLD = 99;

  public static final int NATURAL_BLACK_TAN_THRESHOLD = 36;
  public static final int NATURAL_RED_THRESHOLD = 61;
  public static final int NATURAL_CHOCOLATE_TAN_THRESHOLD = 73;
  public static final int NATURAL_CHOCOLATE_CREAM_THRESHOLD = 81;
  public static final int NATURAL_BLACK_CREAM_THRESHOLD = 88;
  public static final int NATURAL_LIGHT_BLACK_CREAM_THRESHOLD = 94;

  private DachshundCoatRolls() {}

  public static DachshundCoat resolveCoatFromRoll(final SpawnReason spawnReason, final int roll) {
    if (spawnReason == SpawnReason.BREEDING) {
      if (roll < BREEDING_BLACK_TAN_THRESHOLD) {
        return DachshundCoat.BLACK_TAN;
      }
      if (roll < BREEDING_RED_THRESHOLD) {
        return DachshundCoat.RED;
      }
      if (roll < BREEDING_CHOCOLATE_TAN_THRESHOLD) {
        return DachshundCoat.CHOCOLATE_TAN;
      }
      if (roll < BREEDING_CHOCOLATE_CREAM_THRESHOLD) {
        return DachshundCoat.CHOCOLATE_CREAM;
      }
      if (roll < BREEDING_BLACK_CREAM_THRESHOLD) {
        return DachshundCoat.BLACK_CREAM;
      }
      if (roll < BREEDING_LIGHT_BLACK_CREAM_THRESHOLD) {
        return DachshundCoat.LIGHT_BLACK_CREAM;
      }
      if (roll < BREEDING_RED_PIEBALD_THRESHOLD) {
        return DachshundCoat.RED_PIEBALD;
      }
      if (roll < BREEDING_BLACK_TAN_PIEBALD_THRESHOLD) {
        return DachshundCoat.BLACK_TAN_PIEBALD;
      }
      if (roll < BREEDING_BLUE_TAN_THRESHOLD) {
        return DachshundCoat.BLUE_TAN;
      }
      return DachshundCoat.ALBINO;
    }
    if (roll < NATURAL_BLACK_TAN_THRESHOLD) {
      return DachshundCoat.BLACK_TAN;
    }
    if (roll < NATURAL_RED_THRESHOLD) {
      return DachshundCoat.RED;
    }
    if (roll < NATURAL_CHOCOLATE_TAN_THRESHOLD) {
      return DachshundCoat.CHOCOLATE_TAN;
    }
    if (roll < NATURAL_CHOCOLATE_CREAM_THRESHOLD) {
      return DachshundCoat.CHOCOLATE_CREAM;
    }
    if (roll < NATURAL_BLACK_CREAM_THRESHOLD) {
      return DachshundCoat.BLACK_CREAM;
    }
    if (roll < NATURAL_LIGHT_BLACK_CREAM_THRESHOLD) {
      return DachshundCoat.LIGHT_BLACK_CREAM;
    }
    return DachshundCoat.RED_PIEBALD;
  }
}
