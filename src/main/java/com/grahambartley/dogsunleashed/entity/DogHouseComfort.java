package com.grahambartley.dogsunleashed.entity;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * The comfort a dog house gives over a plain dog bed: a dog that wakes in one is rested rather than
 * merely no longer asleep.
 */
public final class DogHouseComfort {

  static final int REGENERATION_DURATION_TICKS = 600;
  private static final int REGENERATION_AMPLIFIER = 0;

  private DogHouseComfort() {}

  public static StatusEffectInstance wakeEffect() {
    return new StatusEffectInstance(
        StatusEffects.REGENERATION, REGENERATION_DURATION_TICKS, REGENERATION_AMPLIFIER);
  }
}
