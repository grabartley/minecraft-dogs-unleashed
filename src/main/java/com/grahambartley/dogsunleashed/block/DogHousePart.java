package com.grahambartley.dogsunleashed.block;

import net.minecraft.util.StringIdentifiable;

public enum DogHousePart implements StringIdentifiable {
  FRONT_LEFT_LOWER("front_left_lower", 0, 0, 0),
  FRONT_RIGHT_LOWER("front_right_lower", 1, 0, 0),
  BACK_LEFT_LOWER("back_left_lower", 0, 0, 1),
  BACK_RIGHT_LOWER("back_right_lower", 1, 0, 1),
  FRONT_LEFT_UPPER("front_left_upper", 0, 1, 0),
  FRONT_RIGHT_UPPER("front_right_upper", 1, 1, 0),
  BACK_LEFT_UPPER("back_left_upper", 0, 1, 1),
  BACK_RIGHT_UPPER("back_right_upper", 1, 1, 1);

  public static final DogHousePart ORIGIN = FRONT_LEFT_LOWER;

  private final String name;
  private final int right;
  private final int up;
  private final int back;

  DogHousePart(final String name, final int right, final int up, final int back) {
    this.name = name;
    this.right = right;
    this.up = up;
    this.back = back;
  }

  public int right() {
    return this.right;
  }

  public int up() {
    return this.up;
  }

  public int back() {
    return this.back;
  }

  public boolean isLower() {
    return this.up == 0;
  }

  @Override
  public String asString() {
    return this.name;
  }
}
