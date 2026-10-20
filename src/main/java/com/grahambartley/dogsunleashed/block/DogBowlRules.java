package com.grahambartley.dogsunleashed.block;

public final class DogBowlRules {

  public static final int FOOD_CAPACITY = 16;
  public static final int WATER_DRINKS = 3;
  private static final int MAX_COMPARATOR_OUTPUT = 15;

  private DogBowlRules() {}

  public static BowlFill foodFill(final int count) {
    if (count <= 0) {
      return BowlFill.EMPTY;
    }
    return count * 2 <= FOOD_CAPACITY ? BowlFill.SOME : BowlFill.FULL;
  }

  public static int foodRoom(final int count) {
    return Math.max(0, FOOD_CAPACITY - count);
  }

  public static int foodComparatorOutput(final int count) {
    return comparatorOutput(count, FOOD_CAPACITY);
  }

  public static int waterAfterBottle(final int drinks) {
    return Math.min(WATER_DRINKS, drinks + 1);
  }

  public static int waterAfterDrink(final int drinks) {
    return Math.max(0, drinks - 1);
  }

  public static int waterComparatorOutput(final int drinks) {
    return comparatorOutput(drinks, WATER_DRINKS);
  }

  private static int comparatorOutput(final int amount, final int capacity) {
    final int clamped = Math.clamp(amount, 0, capacity);
    return Math.ceilDiv(clamped * MAX_COMPARATOR_OUTPUT, capacity);
  }
}
