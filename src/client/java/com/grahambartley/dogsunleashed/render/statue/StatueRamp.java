package com.grahambartley.dogsunleashed.render.statue;

import java.util.Arrays;

public final class StatueRamp {

  public static final int STEPS = 16;

  private static final double LOW_PERCENTILE = 0.02;
  private static final double HIGH_PERCENTILE = 0.98;
  private static final double RED_WEIGHT = 0.2126;
  private static final double GREEN_WEIGHT = 0.7152;
  private static final double BLUE_WEIGHT = 0.0722;
  private static final double CHANNEL_MAX = 255.0;

  private StatueRamp() {}

  public static double luma(final int red, final int green, final int blue) {
    return (RED_WEIGHT * red + GREEN_WEIGHT * green + BLUE_WEIGHT * blue) / CHANNEL_MAX;
  }

  public static int[] steps(final double[] lumas) {
    if (lumas.length == 0) {
      return new int[0];
    }
    final double[] sorted = lumas.clone();
    Arrays.sort(sorted);
    final double low = percentile(sorted, LOW_PERCENTILE);
    final double high = percentile(sorted, HIGH_PERCENTILE);
    final int[] steps = new int[lumas.length];
    for (int i = 0; i < lumas.length; i++) {
      final double luma = lumas[i];
      final double stretched = high == low ? luma : (luma - low) / (high - low);
      final double mixed = Math.clamp((luma + stretched) / 2.0, 0.0, 1.0);
      steps[i] = (int) Math.round(mixed * (STEPS - 1));
    }
    return steps;
  }

  static double percentile(final double[] sorted, final double fraction) {
    return sorted[(int) Math.round(fraction * (sorted.length - 1))];
  }
}
