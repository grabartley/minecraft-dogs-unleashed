package com.grahambartley.dogsunleashed.gesture;

public enum PlayerGesture {
  WHISTLE(14),
  PAT(30);

  private final int ticks;

  PlayerGesture(final int ticks) {
    this.ticks = ticks;
  }

  public int ticks() {
    return this.ticks;
  }

  public boolean isPlaying(final double elapsedTicks) {
    return elapsedTicks >= 0.0 && elapsedTicks < this.ticks;
  }

  public float ramp(final float elapsedTicks, final float rampTicks) {
    if (!this.isPlaying(elapsedTicks)) {
      return 0.0f;
    }
    final float rising = elapsedTicks / rampTicks;
    final float falling = (this.ticks - elapsedTicks) / rampTicks;
    return Math.max(0.0f, Math.min(1.0f, Math.min(rising, falling)));
  }
}
