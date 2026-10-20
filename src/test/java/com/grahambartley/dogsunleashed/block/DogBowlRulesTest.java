package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogBowlRulesTest {

  @ParameterizedTest(name = "{0} foods shows a {1} bowl")
  @CsvSource({"-1, EMPTY", "0, EMPTY", "1, SOME", "8, SOME", "9, FULL", "16, FULL"})
  @DisplayName("the food bowl looks empty, part full or full by how much it holds")
  void foodFillFollowsTheCount(final int count, final BowlFill expected) {
    assertEquals(expected, DogBowlRules.foodFill(count));
  }

  @ParameterizedTest(name = "{0} foods leaves room for {1}")
  @CsvSource({"0, 16", "5, 11", "16, 0", "20, 0"})
  @DisplayName("the food bowl takes no more than sixteen foods")
  void foodRoomStopsAtCapacity(final int count, final int room) {
    assertEquals(room, DogBowlRules.foodRoom(count));
  }

  @ParameterizedTest(name = "{0} foods gives a comparator signal of {1}")
  @CsvSource({"0, 0", "1, 1", "8, 8", "15, 15", "16, 15", "30, 15"})
  @DisplayName("a comparator reads the food bowl from 0 when empty to 15 when full")
  void foodComparatorScalesToFifteen(final int count, final int signal) {
    assertEquals(signal, DogBowlRules.foodComparatorOutput(count));
  }

  @ParameterizedTest(name = "a bottle in a bowl with {0} drinks leaves {1}")
  @CsvSource({"0, 1", "1, 2", "2, 3", "3, 3"})
  @DisplayName("each water bottle adds one drink, up to a full bowl")
  void bottleAddsOneDrink(final int drinks, final int after) {
    assertEquals(after, DogBowlRules.waterAfterBottle(drinks));
  }

  @ParameterizedTest(name = "drinking from a bowl with {0} drinks leaves {1}")
  @CsvSource({"3, 2", "2, 1", "1, 0", "0, 0"})
  @DisplayName("each drink takes one from the bowl and never goes below empty")
  void drinkTakesOne(final int drinks, final int after) {
    assertEquals(after, DogBowlRules.waterAfterDrink(drinks));
  }

  @ParameterizedTest(name = "{0} drinks gives a comparator signal of {1}")
  @CsvSource({"0, 0", "1, 5", "2, 10", "3, 15"})
  @DisplayName("a comparator reads the water bowl like a cauldron")
  void waterComparatorScalesToFifteen(final int drinks, final int signal) {
    assertEquals(signal, DogBowlRules.waterComparatorOutput(drinks));
  }
}
