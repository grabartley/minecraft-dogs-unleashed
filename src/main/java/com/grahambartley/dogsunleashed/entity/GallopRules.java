package com.grahambartley.dogsunleashed.entity;

public final class GallopRules {

  static final float RIDDEN_LIMB_SPEED = 0.1f;
  static final float HITCHED_LIMB_SPEED = 0.5f;

  private GallopRules() {}

  public static boolean gallops(
      final boolean ridden,
      final boolean hitched,
      final float limbSpeed,
      final boolean hasRunClip) {
    if (ridden) {
      return limbSpeed > RIDDEN_LIMB_SPEED;
    }
    return hitched && hasRunClip && limbSpeed > HITCHED_LIMB_SPEED;
  }
}
