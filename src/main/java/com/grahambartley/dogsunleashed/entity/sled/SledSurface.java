package com.grahambartley.dogsunleashed.entity.sled;

import java.util.Collection;

public enum SledSurface {
  ICE(1.0),
  SNOW(0.8),
  GROUND(0.4),
  SAND(0.15);

  public static final float ICE_SLIPPERINESS = 0.9f;

  private final double speedFactor;

  SledSurface(final double speedFactor) {
    this.speedFactor = speedFactor;
  }

  public double speedFactor() {
    return this.speedFactor;
  }

  public static SledSurface classify(
      final boolean snow, final float slipperiness, final boolean sand) {
    if (slipperiness >= ICE_SLIPPERINESS) {
      return ICE;
    }
    if (snow) {
      return SNOW;
    }
    return sand ? SAND : GROUND;
  }

  public static double averageFactor(final Collection<SledSurface> surfaces) {
    return surfaces.stream().mapToDouble(SledSurface::speedFactor).average().orElse(0.0);
  }
}
