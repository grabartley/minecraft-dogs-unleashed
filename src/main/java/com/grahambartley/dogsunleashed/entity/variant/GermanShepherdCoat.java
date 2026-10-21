package com.grahambartley.dogsunleashed.entity.variant;

import java.util.Locale;

public enum GermanShepherdCoat implements UnleashedDogCoat {
  SADDLE,
  SABLE,
  BLACK;

  public static GermanShepherdCoat fromOrdinal(int ordinal) {
    final GermanShepherdCoat[] values = values();
    if (ordinal < 0 || ordinal >= values.length) {
      return SADDLE;
    }
    return values[ordinal];
  }

  @Override
  public String getTexturePrefix() {
    return switch (this) {
      case SADDLE -> "saddle";
      case SABLE -> "sable";
      case BLACK -> "black";
    };
  }

  @Override
  public int getOrdinal() {
    return this.ordinal();
  }

  @Override
  public String translationKey() {
    return "coat.dogs-unleashed.germanshepherd." + this.name().toLowerCase(Locale.ROOT);
  }
}
