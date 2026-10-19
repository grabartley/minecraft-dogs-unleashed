package com.grahambartley.dogsunleashed.render.cosmetic;

import com.grahambartley.dogsunleashed.item.cosmetic.MobHead;

public final class MobHeadMotion {

  static final float BARK_TICKS = 10.0f;
  static final float JAW_SHUT = 7.5f;
  static final float JAW_CYCLE = 10.0f;

  private MobHeadMotion() {}

  public static float progress(
      final MobHead head, final float limbPosition, final float ticksSinceBark) {
    if (head.opensJawOnBark()) {
      return JAW_SHUT + Math.clamp(ticksSinceBark, 0.0f, BARK_TICKS) / BARK_TICKS * JAW_CYCLE;
    }
    return limbPosition;
  }
}
