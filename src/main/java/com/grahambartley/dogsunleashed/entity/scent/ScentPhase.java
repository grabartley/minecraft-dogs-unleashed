package com.grahambartley.dogsunleashed.entity.scent;

public enum ScentPhase {
  NONE,
  SNIFFING,
  POINTING,
  DIGGING;

  public static ScentPhase fromId(final int id) {
    final ScentPhase[] phases = values();
    return id >= 0 && id < phases.length ? phases[id] : NONE;
  }

  public int id() {
    return this.ordinal();
  }
}
