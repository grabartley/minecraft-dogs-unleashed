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
}
