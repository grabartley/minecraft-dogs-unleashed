package com.grahambartley.dogsunleashed.entity;

import java.util.Locale;

public enum DogBondStep {
  STRANGER(0, 1.0, 1.0, 1.0, 1.0),
  FAMILIAR(50, 1.1, 0.9, 1.25, 1.05),
  FRIEND(150, 1.2, 0.8, 1.5, 1.1),
  COMPANION(350, 1.35, 0.7, 1.75, 1.15),
  DEVOTED(700, 1.5, 0.6, 2.0, 1.2);

  private final int minPoints;
  private final double wagSpeed;
  private final double followDistanceFactor;
  private final double followRangeFactor;
  private final double responseSpeedFactor;

  DogBondStep(
      final int minPoints,
      final double wagSpeed,
      final double followDistanceFactor,
      final double followRangeFactor,
      final double responseSpeedFactor) {
    this.minPoints = minPoints;
    this.wagSpeed = wagSpeed;
    this.followDistanceFactor = followDistanceFactor;
    this.followRangeFactor = followRangeFactor;
    this.responseSpeedFactor = responseSpeedFactor;
  }

  public static DogBondStep ofPoints(final int points) {
    DogBondStep reached = STRANGER;
    for (final DogBondStep step : values()) {
      if (points >= step.minPoints) {
        reached = step;
      }
    }
    return reached;
  }

  public static DogBondStep byId(final int id) {
    final DogBondStep[] steps = values();
    return id >= 0 && id < steps.length ? steps[id] : STRANGER;
  }

  public DogBondStep raisedBy(final int steps) {
    return byId(Math.min(values().length - 1, this.ordinal() + Math.max(0, steps)));
  }

  public int minPoints() {
    return this.minPoints;
  }

  public double wagSpeed() {
    return this.wagSpeed;
  }

  public double followDistance(final double baseDistance) {
    return baseDistance * this.followDistanceFactor;
  }

  public double followRange(final double baseRange) {
    return baseRange * this.followRangeFactor;
  }

  public double responseSpeed(final double baseSpeed) {
    return baseSpeed * this.responseSpeedFactor;
  }

  public String translationKey() {
    return "bond.dogs-unleashed." + this.name().toLowerCase(Locale.ROOT);
  }
}
