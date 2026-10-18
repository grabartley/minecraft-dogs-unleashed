package com.grahambartley.dogsunleashed.entity;

import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public enum DireWolfGrowthStage {
  PUP(
      1,
      0.40f,
      0.55f,
      0.50f,
      20.0,
      0.30,
      2.0,
      0.0,
      false,
      0,
      0.0,
      new SlotScales(1.25f, 1.0f, 0.8f, 0.8f)),
  YEARLING(2, 0.80f, 1.10f, 1.00f, 30.0, 0.32, 6.0, 2.0, false, 24000, 0.0, SlotScales.NONE),
  ADULT(
      3,
      1.15f,
      1.50f,
      1.36f,
      40.0,
      0.34,
      8.0,
      3.0,
      false,
      48000,
      0.0,
      new SlotScales(0.97f, 1.02f, 1.04f, 1.02f)),
  PRIME(
      4,
      1.60f,
      2.55f,
      2.32f,
      50.0,
      0.36,
      10.0,
      4.0,
      true,
      0,
      1.5,
      new SlotScales(0.94f, 1.05f, 1.07f, 1.04f)),
  ALPHA(
      5,
      2.00f,
      3.20f,
      2.91f,
      60.0,
      0.38,
      12.0,
      6.0,
      true,
      0,
      2.0,
      new SlotScales(0.9f, 1.08f, 1.12f, 1.06f));

  private final int number;
  private final float width;
  private final float height;
  private final float renderScaleMultiplier;
  private final double maxHealth;
  private final double movementSpeed;
  private final double attackDamage;
  private final double armor;
  private final boolean rideable;
  private final int ticksToGrow;
  private final double jumpClearance;
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
      final int ticksToGrow,
      final double jumpClearance,
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
    this.ticksToGrow = ticksToGrow;
    this.jumpClearance = jumpClearance;
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

  public int ticksToGrow() {
    return this.ticksToGrow;
  }

  public boolean growsWithTime() {
    return this.ticksToGrow > 0;
  }

  public boolean evolvesThroughBond() {
    return this == PRIME;
  }

  public String translationKey() {
    return "growth.dogs-unleashed.direwolf." + this.name().toLowerCase(Locale.ROOT);
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

  public boolean leapsHigh() {
    return this.jumpClearance > 0.0;
  }

  public double jumpClearance() {
    return this.leapsHigh() ? this.jumpClearance : DogJumpPhysics.VANILLA_CLEARANCE;
  }

  public double jumpStrength() {
    return this.leapsHigh()
        ? DogJumpPhysics.strengthToClear(this.jumpClearance)
        : DogJumpPhysics.VANILLA_JUMP_STRENGTH;
  }

  public boolean clearsFences() {
    return this.jumpClearance() >= DogJumpPhysics.FENCE_HEIGHT;
  }

  public SlotScales slotScales() {
    return this.slotScales;
  }

  public record SlotScales(float head, float torso, float legs, float tail) {
    public static final SlotScales NONE = new SlotScales(1.0f, 1.0f, 1.0f, 1.0f);
  }
}
