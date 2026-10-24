package com.grahambartley.dogsunleashed.entity.trick;

public record TrickReadiness(
    boolean asleep,
    boolean hitched,
    boolean mounted,
    boolean fighting,
    boolean hurt,
    boolean inWater,
    boolean onErrand,
    boolean performing) {

  public boolean interruptsTrick() {
    return this.asleep
        || this.hitched
        || this.mounted
        || this.fighting
        || this.hurt
        || this.inWater;
  }

  public boolean mayStart() {
    return !this.interruptsTrick() && !this.onErrand && !this.performing;
  }
}
