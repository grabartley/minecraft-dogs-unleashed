package com.grahambartley.dogsunleashed.entity;

import net.minecraft.util.math.MathHelper;

public final class ChargedJump {

  public static final int FULL_CHARGE_TICKS = 10;
  public static final int COOLDOWN_TICKS = 10;
  static final int FULL_POWER_PERCENT = 90;
  static final float MIN_POWER = 0.4f;
  private static final float CHARGE_PER_TICK = 0.1f;
  private static final float SETTLED_CHARGE = 0.8f;

  private int heldTicks;
  private int cooldownTicks;

  public static float charge(final int heldTicks) {
    if (heldTicks <= 0) {
      return 0.0f;
    }
    if (heldTicks < FULL_CHARGE_TICKS) {
      return heldTicks * CHARGE_PER_TICK;
    }
    return SETTLED_CHARGE + 2.0f / (heldTicks - (FULL_CHARGE_TICKS - 1)) * CHARGE_PER_TICK;
  }

  public static float power(final float charge) {
    final int percent = Math.max(0, MathHelper.floor(charge * 100.0f));
    if (percent >= FULL_POWER_PERCENT) {
      return 1.0f;
    }
    return MIN_POWER + MIN_POWER * percent / FULL_POWER_PERCENT;
  }

  public float tick(final boolean jumpHeld) {
    if (this.cooldownTicks > 0) {
      this.cooldownTicks--;
      return 0.0f;
    }
    if (jumpHeld) {
      this.heldTicks++;
      return 0.0f;
    }
    if (this.heldTicks == 0) {
      return 0.0f;
    }
    final float power = power(charge(this.heldTicks));
    this.heldTicks = 0;
    this.cooldownTicks = COOLDOWN_TICKS;
    return power;
  }

  public float charge() {
    return charge(this.heldTicks);
  }
}
