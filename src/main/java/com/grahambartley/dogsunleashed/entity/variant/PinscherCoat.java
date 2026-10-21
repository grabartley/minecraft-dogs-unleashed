package com.grahambartley.dogsunleashed.entity.variant;

import java.util.Locale;

public enum PinscherCoat implements UnleashedDogCoat {
  BLACK_TAN,
  RED,
  CHOCOLATE_TAN;

  public static PinscherCoat fromOrdinal(int ordinal) {
    final PinscherCoat[] values = values();
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
    };
  }

  @Override
  public int getOrdinal() {
    return this.ordinal();
  }

  @Override
  public String translationKey() {
    return "coat.dogs-unleashed.pinscher." + this.name().toLowerCase(Locale.ROOT);
  }
}
