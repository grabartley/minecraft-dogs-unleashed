package com.grahambartley.dogsunleashed.render.gear;

public final class SaddlebagsFit {

  public static final float BUILT_BACK_WIDTH = 8.0f / 16.0f;
  public static final float ARMOUR_GAP = 0.1f / 16.0f;

  private SaddlebagsFit() {}

  public static float scale(final float torsoWidth, final float armourSideInflation) {
    return (torsoWidth + 2.0f * armourSideInflation) / BUILT_BACK_WIDTH;
  }

  public static float lift(final boolean armoured, final float armourTopInflation) {
    return armoured ? armourTopInflation + ARMOUR_GAP : 0.0f;
  }
}
