package com.grahambartley.dogsunleashed.entity;

import org.jetbrains.annotations.Nullable;

public enum DireWolfGrowthStage {
  PUP(1, 0.40f, 0.55f, 0.50f, 20.0, 0.30, 2.0, 0.0, false, new SlotScales(1.25f, 1.0f, 0.8f, 0.8f)),
  YEARLING(2, 0.80f, 1.10f, 1.00f, 30.0, 0.32, 6.0, 2.0, false, SlotScales.NONE),
  ADULT(
      3,
      1.10f,
      1.35f,
      1.25f,
      40.0,
      0.34,
      8.0,
      3.0,
      false,
      new SlotScales(0.97f, 1.02f, 1.04f, 1.02f)),
  PRIME(
      4,
      1.40f,
      1.60f,
      1.45f,
      50.0,
      0.36,
      10.0,
      4.0,
      true,
      new SlotScales(0.94f, 1.05f, 1.07f, 1.04f)),
  ALPHA(
      5,
      1.80f,
      2.50f,
      2.25f,
      60.0,
      0.38,
      12.0,
      6.0,
      true,
      new SlotScales(0.9f, 1.08f, 1.12f, 1.06f));

  public static final int TICKS_PER_ADULT_STAGE = 24000;

  private final int number;
  private final float width;
  private final float height;
  private final float renderScaleMultiplier;
  private final double maxHealth;
  private final double movementSpeed;
  private final double attackDamage;
  private final double armor;
  private final boolean rideable;
  private final SlotScales slotScales;

  DireWolfGrowthStage(
      final int number,
      final float width,
      final float height,
      final float renderScaleMultiplier,
      final double maxHealth,
      final double movementSpeed,
      final double attackDamage,
      final double armor,
      final boolean rideable,
      final SlotScales slotScales) {
    this.number = number;
    this.width = width;
    this.height = height;
    this.renderScaleMultiplier = renderScaleMultiplier;
    this.maxHealth = maxHealth;
    this.movementSpeed = movementSpeed;
    this.attackDamage = attackDamage;
    this.armor = armor;
    this.rideable = rideable;
    this.slotScales = slotScales;
  }

  public static DireWolfGrowthStage fromNumber(final int number) {
    for (final DireWolfGrowthStage stage : values()) {
      if (stage.number == number) {
        return stage;
      }
    }
    return YEARLING;
  }

  public @Nullable DireWolfGrowthStage next() {
    return this == ALPHA ? null : values()[this.ordinal() + 1];
  }

  public boolean isPup() {
    return this == PUP;
  }

  public int number() {
    return this.number;
  }

  public float width() {
    return this.width;
  }

  public float height() {
    return this.height;
  }

  public float renderScaleMultiplier() {
    return this.renderScaleMultiplier;
  }

  public double maxHealth() {
    return this.maxHealth;
  }

  public double movementSpeed() {
    return this.movementSpeed;
  }

  public double attackDamage() {
    return this.attackDamage;
  }

  public double armor() {
    return this.armor;
  }

  public boolean rideable() {
    return this.rideable;
  }

  public SlotScales slotScales() {
    return this.slotScales;
  }

  public record SlotScales(float head, float torso, float legs, float tail) {
    public static final SlotScales NONE = new SlotScales(1.0f, 1.0f, 1.0f, 1.0f);
  }
}
