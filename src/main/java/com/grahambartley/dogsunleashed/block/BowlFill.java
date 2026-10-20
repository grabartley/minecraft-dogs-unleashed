package com.grahambartley.dogsunleashed.block;

import net.minecraft.util.StringIdentifiable;

public enum BowlFill implements StringIdentifiable {
  EMPTY("empty"),
  SOME("some"),
  FULL("full");

  private final String name;

  BowlFill(final String name) {
    this.name = name;
  }

  @Override
  public String asString() {
    return this.name;
  }
}
