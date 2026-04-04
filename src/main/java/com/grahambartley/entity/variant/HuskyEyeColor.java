package com.grahambartley.entity.variant;

import net.minecraft.util.math.random.Random;

public enum HuskyEyeColor {
  BROWN,
  BLUE,
  HETEROCHROMIA;

  public static HuskyEyeColor fromOrdinal(int ordinal) {
    final HuskyEyeColor[] values = values();
    if (ordinal < 0 || ordinal >= values.length) {
      return BROWN;
    }
    return values[ordinal];
  }

  public static HuskyEyeColor fromRandom(Random random) {
    final int roll = random.nextInt(100);
    if (roll < 40) {
      return BROWN;
    } else if (roll < 75) {
      return BLUE;
    } else {
      return HETEROCHROMIA;
    }
  }
}
