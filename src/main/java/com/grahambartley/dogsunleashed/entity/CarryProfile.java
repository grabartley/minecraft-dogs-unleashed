package com.grahambartley.dogsunleashed.entity;

public record CarryProfile(double verticalOffset, double forwardOffset, float scale) {
  public CarryProfile {
    if (!Float.isFinite(scale) || scale <= 0f) {
      throw new IllegalArgumentException("CarryProfile scale must be positive, got " + scale);
    }
    if (!Double.isFinite(verticalOffset) || !Double.isFinite(forwardOffset)) {
      throw new IllegalArgumentException("CarryProfile offsets must be finite");
    }
  }
}
