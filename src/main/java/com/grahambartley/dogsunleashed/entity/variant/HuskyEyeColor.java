package com.grahambartley.dogsunleashed.entity.variant;

import java.util.Locale;
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

  public boolean heterochromatic() {
    return this == BLUE_HAZEL || this == HAZEL_BLUE;
  }

  public String translationKey() {
    return "eyes.dogs-unleashed.husky." + this.name().toLowerCase(Locale.ROOT);
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
