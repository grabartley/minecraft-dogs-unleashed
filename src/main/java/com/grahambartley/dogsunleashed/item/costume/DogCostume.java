package com.grahambartley.dogsunleashed.item.costume;

public enum DogCostume {
  SPIDER("spider"),
  ZOMBIE("zombie"),
  SKELETON("skeleton"),
  ENDERMAN("enderman"),
  BEE("bee");

  private final String mob;

  DogCostume(final String mob) {
    this.mob = mob;
  }

  public String mob() {
    return this.mob;
  }

  public String itemId() {
    return this.mob + "_costume";
  }
}
