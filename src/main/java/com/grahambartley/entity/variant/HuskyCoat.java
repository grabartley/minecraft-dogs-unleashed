package com.grahambartley.entity.variant;

import net.minecraft.util.math.random.Random;

public enum HuskyCoat {
  BLACK_WHITE,
  GREY_WHITE,
  AGOUTI,
  RED_WHITE,
  SABLE,
  WHITE;

  public static HuskyCoat fromOrdinal(int ordinal) {
    final HuskyCoat[] values = values();
    if (ordinal < 0 || ordinal >= values.length) {
      return BLACK_WHITE;
    }
    return values[ordinal];
  }

  public static HuskyCoat fromRandom(Random random) {
    final int roll = random.nextInt(100);
    if (roll < 30) {
      return BLACK_WHITE;
    } else if (roll < 55) {
      return GREY_WHITE;
    } else if (roll < 70) {
      return AGOUTI;
    } else if (roll < 85) {
      return RED_WHITE;
    } else if (roll < 95) {
      return SABLE;
    } else {
      return WHITE;
    }
  }
}
