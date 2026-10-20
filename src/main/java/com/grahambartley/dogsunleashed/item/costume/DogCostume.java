package com.grahambartley.dogsunleashed.item.costume;

import java.util.OptionalInt;

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
  SHEEP("sheep", OptionalInt.of(0xFFFFFF)),
  MOOSHROOM("mooshroom"),
  CHICKEN("chicken"),
  AXOLOTL("axolotl"),
  FROG("frog"),
  PANDA("panda"),
  TURTLE("turtle"),
  ALLAY("allay");

  private final String mob;
  private final OptionalInt undyedColor;

  DogCostume(final String mob) {
    this(mob, OptionalInt.empty());
  }

  DogCostume(final String mob, final OptionalInt undyedColor) {
    this.mob = mob;
    this.undyedColor = undyedColor;
  }

  public String mob() {
    return this.mob;
  }

  public String itemId() {
    return this.mob + "_costume";
  }

  public boolean isDyeable() {
    return this.undyedColor.isPresent();
  }

  public int undyedColor() {
    return this.undyedColor.orElse(0xFFFFFF);
  }
}
