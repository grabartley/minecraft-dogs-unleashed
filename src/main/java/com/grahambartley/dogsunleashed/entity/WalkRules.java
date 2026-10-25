package com.grahambartley.dogsunleashed.entity;

public final class WalkRules {

  static final float START_LIMB_SPEED = 0.15f;
  static final float STOP_LIMB_SPEED = 0.1f;

  private WalkRules() {}

  public static boolean walks(final float limbSpeed, final boolean walkingNow) {
    return limbSpeed > (walkingNow ? STOP_LIMB_SPEED : START_LIMB_SPEED);
  }
}
