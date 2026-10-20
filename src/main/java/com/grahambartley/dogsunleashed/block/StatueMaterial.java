package com.grahambartley.dogsunleashed.block;

public enum StatueMaterial {
  STONE("stone"),
  GOLD("gold"),
  COPPER("copper"),
  EXPOSED_COPPER("exposed_copper"),
  WEATHERED_COPPER("weathered_copper"),
  OXIDIZED_COPPER("oxidized_copper");

  private final String id;

  StatueMaterial(final String id) {
    this.id = id;
  }

  public String id() {
    return this.id;
  }
}
