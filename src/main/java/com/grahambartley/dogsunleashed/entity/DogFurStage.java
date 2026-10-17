package com.grahambartley.dogsunleashed.entity;

import java.util.Locale;

public enum DogFurStage {
  TRIMMED(0),
  NATURAL(5),
  SHAGGY(12),
  OVERGROWN(20);

  public static final double OVERGROWN_SPEED_MULTIPLIER = -0.10;

  private final int botchChancePercent;

  DogFurStage(final int botchChancePercent) {
    this.botchChancePercent = botchChancePercent;
  }

  public int botchChancePercent() {
    return this.botchChancePercent;
  }

  public boolean slowsMovement() {
    return this == OVERGROWN;
  }

  public boolean canBeTrimmed() {
    return this != TRIMMED;
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
