package com.grahambartley.dogsunleashed.render.growth;

import java.util.List;

public record GrowthGroundFit(float lift, float heightScale) {

  public static final GrowthGroundFit NONE = new GrowthGroundFit(0.0f, 1.0f);

  public record Leg(
      float parentPivotY, float parentScale, float pivotY, float bottomY, float legScale) {

    float scaledBottomY() {
      final float pivot = this.parentPivotY + (this.pivotY - this.parentPivotY) * this.parentScale;
      return pivot - (this.pivotY - this.bottomY) * this.legScale;
    }
  }

  public static GrowthGroundFit of(
      final List<Leg> legs, final float topY, final float headPivotY, final float headScale) {
    float lift = 0.0f;
    boolean first = true;
    for (final Leg leg : legs) {
      final float needed = leg.bottomY() - leg.scaledBottomY();
      lift = first ? needed : Math.max(lift, needed);
      first = false;
    }
    final float grownTop = topY + lift + (headScale - 1.0f) * (topY - headPivotY);
    return new GrowthGroundFit(lift, grownTop > 0.0f ? topY / grownTop : 1.0f);
  }
}
