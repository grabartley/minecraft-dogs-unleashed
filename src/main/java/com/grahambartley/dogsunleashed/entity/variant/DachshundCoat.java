package com.grahambartley.dogsunleashed.entity.variant;

public enum DachshundCoat implements UnleashedDogCoat {
  BLACK_TAN,
  RED,
  CHOCOLATE_TAN,
  CHOCOLATE_CREAM,
  BLACK_CREAM,
  RED_PIEBALD,
  BLACK_TAN_PIEBALD,
  BLUE_TAN,
  ALBINO,
  LIGHT_BLACK_CREAM;

  public static DachshundCoat fromOrdinal(int ordinal) {
    final DachshundCoat[] values = values();
    if (ordinal < 0 || ordinal >= values.length) {
      return BLACK_TAN;
    }
    return values[ordinal];
  }

  @Override
  public String getTexturePrefix() {
    return switch (this) {
      case BLACK_TAN -> "blacktan";
      case RED -> "red";
      case CHOCOLATE_TAN -> "chocolatetan";
      case CHOCOLATE_CREAM -> "chocolatecream";
      case BLACK_CREAM -> "blackcream";
      case RED_PIEBALD -> "redpiebald";
      case BLACK_TAN_PIEBALD -> "blacktanpiebald";
      case BLUE_TAN -> "bluetan";
      case ALBINO -> "albino";
      case LIGHT_BLACK_CREAM -> "lightblackcream";
    };
  }

  @Override
  public int getOrdinal() {
    return this.ordinal();
  }
}
