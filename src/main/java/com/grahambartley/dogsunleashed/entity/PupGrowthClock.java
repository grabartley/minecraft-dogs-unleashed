package com.grahambartley.dogsunleashed.entity;

public final class PupGrowthClock {

  private int progress;

  public int tick(final int growthRatePercent) {
    final int total = this.progress + growthRatePercent;
    this.progress = total % DogGrowthRules.PERCENT;
    return total / DogGrowthRules.PERCENT;
  }

  public void reset() {
    this.progress = 0;
  }
}
