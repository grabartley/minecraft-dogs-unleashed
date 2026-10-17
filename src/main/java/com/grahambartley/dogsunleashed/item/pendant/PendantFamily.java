package com.grahambartley.dogsunleashed.item.pendant;

public enum PendantFamily {
  WARDING(true),
  GUARDIAN(false),
  VITALITY(true),
  LOYALTY(false);

  private final boolean ownerFacing;

  PendantFamily(final boolean ownerFacing) {
    this.ownerFacing = ownerFacing;
  }

  public boolean ownerFacing() {
    return this.ownerFacing;
  }
}
