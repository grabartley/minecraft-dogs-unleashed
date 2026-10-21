package com.grahambartley.dogsunleashed.entity.sled;

import com.grahambartley.dogsunleashed.entity.DireWolfStamina;

public final class SledSpeed {

  public static final double TOP_SPEED = 0.42;
  public static final double ACCELERATION = 0.012;
  public static final double COAST_DECELERATION = 0.02;
  public static final double BRAKE_DECELERATION = 0.05;
  public static final double MIN_DOG_SPEED = 0.6;
  public static final double MAX_DOG_SPEED = 3.0;
  static final double TEAM_HEADROOM = 2.0;
  static final double GROUND_GAIN = 0.98 / (1.0 - 0.6 * 0.91);

  private SledSpeed() {}

  public static double teamFactor(final int teamSize) {
    if (teamSize <= 0) {
      return 0.0;
    }
    final double full = SledFormation.MAX_TEAM / (SledFormation.MAX_TEAM + TEAM_HEADROOM);
    return teamSize / (teamSize + TEAM_HEADROOM) / full;
  }

  public static double cruise(
      final int teamSize, final double surfaceFactor, final boolean sprinting) {
    final double trot = TOP_SPEED * teamFactor(teamSize) * surfaceFactor;
    return sprinting ? trot * DireWolfStamina.SPRINT_SPEED_MULTIPLIER : trot;
  }

  public static double approach(final double current, final double target, final boolean braking) {
    if (current < target) {
      return Math.min(target, current + ACCELERATION);
    }
    final double slowing = braking ? BRAKE_DECELERATION : COAST_DECELERATION;
    return Math.max(target, current - slowing);
  }

  public static double dogSpeedMultiplier(
      final double blocksPerTick, final double movementAttribute) {
    if (movementAttribute <= 0.0) {
      return MIN_DOG_SPEED;
    }
    final double multiplier =
        Math.sqrt(Math.max(0.0, blocksPerTick) / GROUND_GAIN) / movementAttribute;
    return Math.max(MIN_DOG_SPEED, Math.min(MAX_DOG_SPEED, multiplier));
  }
}
