package com.grahambartley.dogsunleashed.entity.scent;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record ScentLandmark(int x, int z) {

  public static ScentLandmark at(final double x, final double z) {
    return new ScentLandmark((int) Math.floor(x), (int) Math.floor(z));
  }

  public static Optional<ScentLandmark> nearest(
      final List<ScentLandmark> landmarks, final double fromX, final double fromZ) {
    return landmarks.stream()
        .min(Comparator.comparingDouble(landmark -> landmark.squaredDistanceTo(fromX, fromZ)));
  }

  public double squaredDistanceTo(final double fromX, final double fromZ) {
    final double dx = this.x + 0.5 - fromX;
    final double dz = this.z + 0.5 - fromZ;
    return dx * dx + dz * dz;
  }
}
