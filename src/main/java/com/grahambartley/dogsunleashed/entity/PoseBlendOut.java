package com.grahambartley.dogsunleashed.entity;

public final class PoseBlendOut {

  public enum Step {
    BLEND_BACK,
    SNAP,
    KEEP
  }

  private PoseBlendOut() {}

  public static Step step(final boolean poseHeld, final boolean movementRunning) {
    if (poseHeld) {
      return Step.BLEND_BACK;
    }
    return movementRunning ? Step.SNAP : Step.KEEP;
  }
}
