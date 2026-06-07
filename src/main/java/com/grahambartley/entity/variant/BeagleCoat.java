package com.grahambartley.entity.variant;

public enum BeagleCoat implements UnleashedDogCoat {
  TRI_1,
  RED_1,
  TRI_2,
  TRI_3,
  RED_2,
  LEMON_1,
  BROWN_WHITE,
  BROWN_WHITE_TAN,
  LEMON_2,
  BLACK_WHITE,
  LILAC_1,
  LILAC_2;

  public static BeagleCoat fromOrdinal(int ordinal) {
    final BeagleCoat[] values = values();
    if (ordinal < 0 || ordinal >= values.length) {
      return TRI_1;
    }
    return values[ordinal];
  }

  @Override
  public String getTexturePrefix() {
    return switch (this) {
      case TRI_1 -> "tri1";
      case RED_1 -> "red1";
      case TRI_2 -> "tri2";
      case TRI_3 -> "tri3";
      case RED_2 -> "red2";
      case LEMON_1 -> "lemon1";
      case BROWN_WHITE -> "brownwhite";
      case BROWN_WHITE_TAN -> "brownwhitetan";
      case LEMON_2 -> "lemon2";
      case BLACK_WHITE -> "blackwhite";
      case LILAC_1 -> "lilac1";
      case LILAC_2 -> "lilac2";
    };
  }

  @Override
  public int getOrdinal() {
    return this.ordinal();
  }
}
