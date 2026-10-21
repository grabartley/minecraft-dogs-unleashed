package com.grahambartley.dogsunleashed.render.cross;

import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage.SlotScales;
import java.util.List;

public final class CrossProportions {

  public record SlotSizes(float head, float torso, float legs, float tail) {}

  public record Pull(SlotSizes sizes, float weight) {}

  static final float MIN_RATIO = 0.75f;
  static final float MAX_RATIO = 1.0f / MIN_RATIO;
  private static final float STEPS = 100.0f;

  private CrossProportions() {}

  public static SlotScales scalesOf(final SlotSizes own, final List<Pull> pulls) {
    return new SlotScales(
        scale(own.head(), pulls, SlotSizes::head),
        scale(own.torso(), pulls, SlotSizes::torso),
        scale(own.legs(), pulls, SlotSizes::legs),
        scale(own.tail(), pulls, SlotSizes::tail));
  }

  private interface Part {
    float of(SlotSizes sizes);
  }

  private static float scale(final float own, final List<Pull> pulls, final Part part) {
    if (own <= 0.0f) {
      return 1.0f;
    }
    double logScale = 0.0;
    for (final Pull pull : pulls) {
      final float other = part.of(pull.sizes());
      if (other <= 0.0f) {
        continue;
      }
      final float ratio = Math.clamp(other / own, MIN_RATIO, MAX_RATIO);
      logScale += pull.weight() * Math.log(ratio);
    }
    return Math.round((float) Math.exp(logScale) * STEPS) / STEPS;
  }
}
