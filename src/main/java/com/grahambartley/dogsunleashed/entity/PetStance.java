package com.grahambartley.dogsunleashed.entity;

public final class PetStance {

  public static final float KNEEL_BELOW_BLOCKS = 0.95f;
  public static final float STAND_ABOVE_BLOCKS = 1.15f;

  private PetStance() {}

  public static float kneelWeight(final float headTopBlocks) {
    final float t =
        Math.max(
            0.0f,
            Math.min(
                1.0f,
                (STAND_ABOVE_BLOCKS - headTopBlocks) / (STAND_ABOVE_BLOCKS - KNEEL_BELOW_BLOCKS)));
    return t * t * (3.0f - 2.0f * t);
  }
}
