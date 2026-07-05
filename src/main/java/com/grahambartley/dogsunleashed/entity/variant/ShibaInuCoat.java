package com.grahambartley.dogsunleashed.entity.variant;

public enum ShibaInuCoat implements UnleashedDogCoat {
  RED,
  BLACK,
  SESAME;

  public static ShibaInuCoat fromOrdinal(int ordinal) {
    final ShibaInuCoat[] values = values();
    if (ordinal < 0 || ordinal >= values.length) {
      return RED;
    }
    return values[ordinal];
  }

  @Override
  public String getTexturePrefix() {
    return switch (this) {
      case RED -> "red";
      case BLACK -> "black";
      case SESAME -> "sesame";
    };
  }

  @Override
  public int getOrdinal() {
    return this.ordinal();
  }
}
