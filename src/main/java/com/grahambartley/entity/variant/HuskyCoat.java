package com.grahambartley.entity.variant;

public enum HuskyCoat implements UnleashedDogCoat {
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

  @Override
  public String getTexturePrefix() {
    return switch (this) {
      case BLACK_WHITE -> "blackwhite";
      case GREY_WHITE -> "graywhite";
      case AGOUTI -> "agouti";
      case RED_WHITE -> "redwhite";
      case SABLE -> "sablewhite";
      case WHITE -> "white";
    };
  }

  @Override
  public int getOrdinal() {
    return this.ordinal();
  }
}
