package com.grahambartley.dogsunleashed.entity.sled;

import com.grahambartley.dogsunleashed.config.DireWolfStaminaSettings;
import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.DireWolfStamina;
import java.util.List;

public final class SledTeamStamina {

  static final float PAIR = 2.0f;
  private static final float PERCENT = 100.0f;

  private SledTeamStamina() {}

  public static float capacity(
      final DireWolfGrowthStage stage, final DireWolfStaminaSettings settings) {
    final float base =
        stage == DireWolfGrowthStage.ALPHA
            ? DireWolfStamina.ALPHA_CAPACITY
            : DireWolfStamina.PRIME_CAPACITY;
    return base * settings.capacityPercent() / PERCENT;
  }

  public static float drainScale(final int teamSize) {
    return teamSize <= 0 ? 0.0f : PAIR / teamSize;
  }

  public static boolean canSprint(final List<DireWolfStamina.State> team) {
    return !team.isEmpty()
        && team.stream().allMatch(state -> !state.lockedOut() && state.stamina() > 0.0f);
  }

  public static float fraction(
      final List<DireWolfStamina.State> team, final List<Float> capacities) {
    float stamina = 0.0f;
    float capacity = 0.0f;
    for (int index = 0; index < team.size(); index++) {
      stamina += team.get(index).stamina();
      capacity += capacities.get(index);
    }
    return capacity > 0.0f ? stamina / capacity : 0.0f;
  }
}
