package com.grahambartley.dogsunleashed.entity;

public final class PoseBlendOut {

  public static final int TICKS = 5;
  static final int RESET_TICKS = 1;

  public enum Step {
    BLEND_BACK,
    SNAP,
    KEEP
  }

  private long handedBackAt = -TICKS - 1;

  public static Step step(final boolean poseHeld, final boolean movementRunning) {
    if (poseHeld) {
      return Step.BLEND_BACK;
    }
    return movementRunning ? Step.SNAP : Step.KEEP;
  }

  public void handBack(final long now) {
    this.handedBackAt = now;
  }

  public double boneResetTicks(final long now) {
    final long since = now - this.handedBackAt;
    return since >= 0 && since <= TICKS ? TICKS : RESET_TICKS;
  }
}
