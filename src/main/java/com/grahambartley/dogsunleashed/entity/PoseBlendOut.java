package com.grahambartley.dogsunleashed.entity;

public final class PoseBlendOut {

  private boolean wasPosing = false;

  public boolean ended(final boolean posing) {
    final boolean ended = this.wasPosing && !posing;
    this.wasPosing = posing;
    return ended;
  }
}
