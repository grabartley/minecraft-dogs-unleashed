package com.grahambartley.util;

public class EntityUtils {

  public static boolean isValidScale(float scale) {
    return scale >= 0.5f && scale <= 2.0f;
  }

  public static float clampScale(float scale) {
    if (scale < 0.5f) {
      return 0.5f;
    }
    if (scale > 2.0f) {
      return 2.0f;
    }
    return scale;
  }

  public static int getSpawnEggColor(int primary, int secondary) {
    if (!isValidColor(primary) || !isValidColor(secondary)) {
      throw new IllegalArgumentException("Invalid spawn egg color");
    }
    return (primary << 16) | secondary;
  }

  private static boolean isValidColor(int color) {
    return color >= 0x000000 && color <= 0xFFFFFF;
  }

  public static String getModVersion() {
    return "1.0.0";
  }
}
