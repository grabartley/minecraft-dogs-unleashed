package com.grahambartley.dogsunleashed.entity;

public record CarryProfile(float scale) {
  public CarryProfile {
    if (!Float.isFinite(scale) || scale <= 0f) {
      throw new IllegalArgumentException("CarryProfile scale must be positive, got " + scale);
    }
  }
}
