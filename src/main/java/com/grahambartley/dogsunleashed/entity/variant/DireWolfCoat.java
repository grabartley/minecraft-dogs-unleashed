package com.grahambartley.dogsunleashed.entity.variant;

import java.util.Locale;

public enum DireWolfCoat implements UnleashedDogCoat {
  SMOKEGREY,
  ASHGREY,
  FROSTGREY,
  SILVER,
  BLACK,
  WHITE;

  public static DireWolfCoat fromOrdinal(final int ordinal) {
    final DireWolfCoat[] values = values();
    if (ordinal < 0 || ordinal >= values.length) {
      return SMOKEGREY;
    }
    return values[ordinal];
  }

  @Override
  public String getTexturePrefix() {
    return this.name().toLowerCase(Locale.ROOT);
  }

  @Override
  public int getOrdinal() {
    return this.ordinal();
  }

  @Override
  public String translationKey() {
    return "coat.dogs-unleashed.direwolf." + this.getTexturePrefix();
  }
}
