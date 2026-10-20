package com.grahambartley.dogsunleashed.item.costume;

public enum DogCostume {
  SPIDER("spider"),
  ZOMBIE("zombie"),
  SKELETON("skeleton"),
  ENDERMAN("enderman"),
  BEE("bee"),
  ENDER_DRAGON("ender_dragon"),
  CREEPER("creeper"),
  WITHER("wither"),
  WARDEN("warden"),
  SHEEP("sheep", true),
  MOOSHROOM("mooshroom"),
  CHICKEN("chicken"),
  AXOLOTL("axolotl"),
  FROG("frog"),
  PANDA("panda"),
  TURTLE("turtle"),
  ALLAY("allay");

  public static final int UNDYED_COLOR = 0xFFFFFF;

  private final String mob;
  private final boolean dyeable;

  DogCostume(final String mob) {
    this(mob, false);
  }

  DogCostume(final String mob, final boolean dyeable) {
    this.mob = mob;
    this.dyeable = dyeable;
  }

  public String mob() {
    return this.mob;
  }

  public String itemId() {
    return this.mob + "_costume";
  }

  public boolean isDyeable() {
    return this.dyeable;
  }
}
