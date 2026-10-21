package com.grahambartley.dogsunleashed.trade;

import static com.grahambartley.dogsunleashed.trade.CostumeTradePrice.Tier.COMMON;
import static com.grahambartley.dogsunleashed.trade.CostumeTradePrice.Tier.RARE;
import static com.grahambartley.dogsunleashed.trade.CostumeTradePrice.Tier.WOOL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CostumeTradePriceTest {

  static Stream<Arguments> recipes() {
    return Stream.of(
        Arguments.of(List.of(WOOL), 3),
        Arguments.of(List.of(WOOL, COMMON), 5),
        Arguments.of(List.of(WOOL, COMMON, COMMON), 7),
        Arguments.of(List.of(COMMON), 5),
        Arguments.of(List.of(WOOL, RARE), 11),
        Arguments.of(List.of(RARE, COMMON), 13),
        Arguments.of(List.of(WOOL, RARE, COMMON), 13),
        Arguments.of(List.of(), 11),
        Arguments.of(Collections.nCopies(10, RARE), 64));
  }

  @ParameterizedTest
  @MethodSource("recipes")
  void pricesARecipeByItsIngredients(final List<CostumeTradePrice.Tier> tiers, final int emeralds) {
    assertEquals(emeralds, CostumeTradePrice.emeralds(tiers));
  }

  static Stream<Arguments> harderRecipes() {
    return Stream.of(
        Arguments.of(List.of(WOOL), List.of(WOOL, COMMON)),
        Arguments.of(List.of(WOOL, COMMON), List.of(WOOL, COMMON, COMMON)),
        Arguments.of(List.of(WOOL, COMMON, COMMON), List.of(WOOL, RARE)),
        Arguments.of(List.of(WOOL, RARE), List.of(RARE, COMMON)));
  }

  @ParameterizedTest
  @MethodSource("harderRecipes")
  void aHarderRecipeCostsMore(
      final List<CostumeTradePrice.Tier> easier, final List<CostumeTradePrice.Tier> harder) {
    assertTrue(CostumeTradePrice.emeralds(harder) > CostumeTradePrice.emeralds(easier));
  }
}
