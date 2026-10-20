package com.grahambartley.dogsunleashed.render.costume;

import java.util.Optional;

public final class CostumeMotion {

  public static final String JUMP = "jump";
  public static final String SWIM = "swim";
  public static final String BARK = "bark";
  static final float BARK_TICKS = 10.0f;

  private CostumeMotion() {}

  public static Optional<String> bodyClip(
      final boolean onGround, final boolean inWater, final boolean riding) {
    if (inWater) {
      return Optional.of(SWIM);
    }
    if (!onGround && !riding) {
      return Optional.of(JUMP);
    }
    return Optional.empty();
  }

  public static boolean barking(final float ticksSinceBark) {
    return ticksSinceBark >= 0.0f && ticksSinceBark < BARK_TICKS;
  }
}
