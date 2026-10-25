package com.grahambartley.dogsunleashed.entity;

public final class WalkRules {

  static final float WALKING_LIMB_SPEED = 0.15f;

  private WalkRules() {}

  public static boolean walks(final float limbSpeed) {
    return limbSpeed > WALKING_LIMB_SPEED;
  }
}
