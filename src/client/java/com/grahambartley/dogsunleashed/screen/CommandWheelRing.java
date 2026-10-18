package com.grahambartley.dogsunleashed.screen;

final class CommandWheelRing {

  private final int mainActionCount;
  private final int subActionCount;
  private boolean subRingOpen;

  CommandWheelRing(final int mainActionCount, final int subActionCount) {
    this.mainActionCount = mainActionCount;
    this.subActionCount = subActionCount;
  }

  boolean isSubRingOpen() {
    return this.subRingOpen;
  }

  boolean hasActionsSector() {
    return this.subActionCount > 0;
  }

  int sectorCount() {
    if (this.subRingOpen) {
      return this.subActionCount;
    }
    return this.mainActionCount + (this.hasActionsSector() ? 1 : 0);
  }

  boolean isActionsSector(final int index) {
    return !this.subRingOpen && this.hasActionsSector() && index == this.mainActionCount;
  }

  boolean openSubRing() {
    if (this.subRingOpen || !this.hasActionsSector()) {
      return false;
    }
    this.subRingOpen = true;
    return true;
  }

  boolean returnToMainRing() {
    if (!this.subRingOpen) {
      return false;
    }
    this.subRingOpen = false;
    return true;
  }

  boolean returnToMainRingIfInHub(final double dx, final double dy, final double hubRadius) {
    return dx * dx + dy * dy < hubRadius * hubRadius && this.returnToMainRing();
  }
}
