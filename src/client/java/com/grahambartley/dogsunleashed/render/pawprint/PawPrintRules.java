package com.grahambartley.dogsunleashed.render.pawprint;

public final class PawPrintRules {

  public static final int LIFETIME_TICKS = 600;
  public static final double STRIDE_BLOCKS = 0.5;
  public static final int MAX_PRINTS = 512;
  public static final double RENDER_DISTANCE_BLOCKS = 48.0;
  static final float SIZE_PER_DOG_WIDTH = 0.4f;
  static final float SIDE_OFFSET_PER_DOG_WIDTH = 0.18f;

  private PawPrintRules() {}

  public static boolean stamps(final double movedX, final double movedZ) {
    return movedX * movedX + movedZ * movedZ >= STRIDE_BLOCKS * STRIDE_BLOCKS;
  }

  public static boolean expired(final long ageTicks) {
    return ageTicks >= LIFETIME_TICKS;
  }

  public static float alpha(final long ageTicks) {
    if (ageTicks <= 0) {
      return 1.0f;
    }
    return Math.max(0.0f, 1.0f - (float) ageTicks / LIFETIME_TICKS);
  }

  public static float size(final float dogWidth) {
    return dogWidth * SIZE_PER_DOG_WIDTH;
  }

  public static float sideOffset(final boolean left, final float dogWidth) {
    return (left ? -1.0f : 1.0f) * dogWidth * SIDE_OFFSET_PER_DOG_WIDTH;
  }

  public static boolean inRange(final double distanceSquared) {
    return distanceSquared <= RENDER_DISTANCE_BLOCKS * RENDER_DISTANCE_BLOCKS;
  }
}
