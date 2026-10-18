package com.grahambartley.dogsunleashed.entity;

public record CarryProfile(double verticalOffset, double forwardOffset) {
  public CarryProfile {
    if (!Double.isFinite(verticalOffset) || !Double.isFinite(forwardOffset)) {
      throw new IllegalArgumentException("CarryProfile offsets must be finite");
    }
  }
}
