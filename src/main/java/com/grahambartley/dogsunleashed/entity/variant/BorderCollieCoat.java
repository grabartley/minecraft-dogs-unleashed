package com.grahambartley.dogsunleashed.entity.variant;

import java.util.Locale;

public enum BorderCollieCoat implements UnleashedDogCoat {
  BLACK_WHITE,
  RED_MERLE,
  BLUE_MERLE;

  public static BorderCollieCoat fromOrdinal(int ordinal) {
    final BorderCollieCoat[] values = values();
    if (ordinal < 0 || ordinal >= values.length) {
      return BLACK_WHITE;
    }
    return values[ordinal];
  }

  @Override
  public String getTexturePrefix() {
    return switch (this) {
      case BLACK_WHITE -> "blackwhite";
      case RED_MERLE -> "redmerle";
      case BLUE_MERLE -> "bluemerle";
    };
  }

  @Override
  public int getOrdinal() {
    return this.ordinal();
  }

  @Override
  public String translationKey() {
    return "coat.dogs-unleashed.bordercollie." + this.name().toLowerCase(Locale.ROOT);
  }
}
