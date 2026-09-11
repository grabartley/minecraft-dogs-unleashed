package com.grahambartley.dogsunleashed.entity;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public final class DogHouseComfort {

  static final int REGENERATION_DURATION_TICKS = 600;
  private static final int REGENERATION_AMPLIFIER = 0;

  private DogHouseComfort() {}

  public static StatusEffectInstance wakeEffect() {
    return new StatusEffectInstance(
        StatusEffects.REGENERATION, REGENERATION_DURATION_TICKS, REGENERATION_AMPLIFIER);
  }
}
