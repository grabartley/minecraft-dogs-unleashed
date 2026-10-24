package com.grahambartley.dogsunleashed.entity;

import java.util.Locale;

public enum DogFurStage {
  BOTCHED(0, 50),
  TRIMMED(0, 100),
  NATURAL(5, 150),
  SHAGGY(12, 200),
  OVERGROWN(20, 0);

  public static final double OVERGROWN_SPEED_MULTIPLIER = -0.10;

  private final int botchChancePercent;
  private final int growthTimePercent;

  DogFurStage(final int botchChancePercent, final int growthTimePercent) {
    this.botchChancePercent = botchChancePercent;
    this.growthTimePercent = growthTimePercent;
  }

  public int botchChancePercent() {
    return this.botchChancePercent;
  }

  public int growthTimePercent() {
    return this.growthTimePercent;
  }

  public boolean slowsMovement() {
    return this == OVERGROWN;
  }

  public boolean canBeTrimmed() {
    return this.ordinal() > TRIMMED.ordinal();
  }

  public DogFurStage next() {
    return this == OVERGROWN ? OVERGROWN : values()[this.ordinal() + 1];
  }

  public DogFurStage atMost(final DogFurStage cap) {
    return this.ordinal() > cap.ordinal() ? cap : this;
  }

  public int stagesAbove(final DogFurStage lower) {
    return Math.max(0, this.ordinal() - lower.ordinal());
  }

  public String translationKey() {
    return "fur.dogs-unleashed." + this.name().toLowerCase(Locale.ROOT);
  }

  public static DogFurStage fromOrdinal(final int ordinal) {
    final DogFurStage[] stages = values();
    return ordinal >= 0 && ordinal < stages.length ? stages[ordinal] : NATURAL;
  }
}
