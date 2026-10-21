package com.grahambartley.dogsunleashed.trade;

import java.util.Collection;

public final class CostumeTradePrice {

  public static final int BASE_EMERALDS = 3;
  public static final int COMMON_STEP = 2;
  public static final int RARE_STEP = 8;
  public static final int MAX_EMERALDS = 64;

  public enum Tier {
    WOOL,
    COMMON,
    RARE
  }

  private CostumeTradePrice() {}

  public static int emeralds(final Collection<Tier> distinctIngredients) {
    if (distinctIngredients.isEmpty()) {
      return BASE_EMERALDS + RARE_STEP;
    }
    int price = BASE_EMERALDS;
    for (final Tier tier : distinctIngredients) {
      price += step(tier);
    }
    return Math.min(price, MAX_EMERALDS);
  }

  private static int step(final Tier tier) {
    return switch (tier) {
      case WOOL -> 0;
      case COMMON -> COMMON_STEP;
      case RARE -> RARE_STEP;
    };
  }
}
