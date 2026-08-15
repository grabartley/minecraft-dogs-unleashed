package com.grahambartley.dogsunleashed.entity.variant;

public enum DogRarity {
  COMMON("common", 0xFFFFFFFF),
  UNCOMMON("uncommon", 0xFFFFFF55),
  RARE("rare", 0xFF55FFFF),
  EPIC("epic", 0xFFFF55FF);

  private final String serializedName;
  private final int colorArgb;

  DogRarity(final String serializedName, final int colorArgb) {
    this.serializedName = serializedName;
    this.colorArgb = colorArgb;
  }

  public String translationKey() {
    return "rarity.dogs-unleashed." + this.serializedName;
  }

  public int colorArgb() {
    return this.colorArgb;
  }
}
