package com.grahambartley.dogsunleashed.entity.scent;

public final class ScentProgress {

  public static final int GIVE_UP_TICKS = 2 * 60 * 20;
  static final double MIN_GAIN = 0.5;

  private double closest = Double.MAX_VALUE;
  private int ticksWithoutGain;

  public void record(final double distance) {
    if (distance < this.closest - MIN_GAIN) {
      this.closest = distance;
      this.ticksWithoutGain = 0;
    } else {
      this.ticksWithoutGain++;
    }
  }

  public void reset() {
    this.closest = Double.MAX_VALUE;
    this.ticksWithoutGain = 0;
  }

  public boolean hasStalled() {
    return this.ticksWithoutGain >= GIVE_UP_TICKS;
  }
}
