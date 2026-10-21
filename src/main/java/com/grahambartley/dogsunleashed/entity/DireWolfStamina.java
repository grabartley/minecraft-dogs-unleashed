package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.config.DireWolfStaminaSettings;

public final class DireWolfStamina {

  public static final float PRIME_CAPACITY = 100.0f;
  public static final float ALPHA_CAPACITY = 140.0f;
  public static final float SPRINT_DRAIN = 0.4f;
  public static final float RIDDEN_RECOVERY = 0.25f;
  public static final float RESTING_RECOVERY = 1.0f;
  public static final float LOCKOUT_RELEASE_FRACTION = 0.25f;
  public static final float SPRINT_SPEED_MULTIPLIER = 1.6f;
  private static final float PERCENT = 100.0f;

  private DireWolfStamina() {}

  public record State(float stamina, boolean lockedOut) {}

  public static float capacity(
      final DireWolfGrowthStage stage, final DireWolfStaminaSettings settings) {
    final float base =
        switch (stage) {
          case PRIME -> PRIME_CAPACITY;
          case ALPHA -> ALPHA_CAPACITY;
          default -> 0.0f;
        };
    return base * settings.capacityPercent() / PERCENT;
  }

  public static boolean sprints(
      final State state, final boolean sprintHeld, final boolean inWater) {
    return sprintHeld && !inWater && !state.lockedOut() && state.stamina() > 0.0f;
  }

  public static State next(
      final State state,
      final float capacity,
      final boolean ridden,
      final boolean sprinting,
      final DireWolfStaminaSettings settings) {
    return next(state, capacity, ridden, sprinting, settings, 1.0f);
  }

  public static State next(
      final State state,
      final float capacity,
      final boolean ridden,
      final boolean sprinting,
      final DireWolfStaminaSettings settings,
      final float drainScale) {
    final float recoveryScale = settings.recoveryPercent() / PERCENT;
    final float change;
    if (sprinting) {
      change = settings.drains() ? -SPRINT_DRAIN * drainScale : 0.0f;
    } else {
      change = (ridden ? RIDDEN_RECOVERY : RESTING_RECOVERY) * recoveryScale;
    }
    final float stamina = Math.max(0.0f, Math.min(capacity, state.stamina() + change));
    final boolean lockedOut =
        stamina <= 0.0f || (state.lockedOut() && stamina < capacity * LOCKOUT_RELEASE_FRACTION);
    return new State(stamina, lockedOut);
  }
}
