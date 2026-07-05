package com.grahambartley.dogsunleashed.entity.variant;

import net.minecraft.util.math.random.Random;

public enum HuskyEyeColor {
  HAZEL_HAZEL,
  BLUE_BLUE,
  BLUE_HAZEL,
  HAZEL_BLUE;

  public String textureSuffix() {
    return switch (this) {
      case HAZEL_HAZEL -> "hazelhazel";
      case BLUE_BLUE -> "blueblue";
      case BLUE_HAZEL -> "bluehazel";
      case HAZEL_BLUE -> "hazelblue";
    };
  }

  public static HuskyEyeColor fromOrdinal(int ordinal) {
    final HuskyEyeColor[] values = values();
    if (ordinal < 0 || ordinal >= values.length) {
      return HAZEL_HAZEL;
    }
    return values[ordinal];
  }

  public static HuskyEyeColor fromRandom(Random random) {
    return values()[random.nextInt(values().length)];
  }
}
