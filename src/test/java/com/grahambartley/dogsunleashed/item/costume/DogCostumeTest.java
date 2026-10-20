package com.grahambartley.dogsunleashed.item.costume;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogCostumeTest {

  @ParameterizedTest(name = "{0} is {1}")
  @CsvSource({
    "SPIDER, spider, spider_costume",
    "ZOMBIE, zombie, zombie_costume",
    "SKELETON, skeleton, skeleton_costume",
    "ENDERMAN, enderman, enderman_costume",
    "BEE, bee, bee_costume"
  })
  @DisplayName("each costume is named after the mob it dresses the dog up as")
  void itemIdNamesTheMob(final DogCostume costume, final String mob, final String itemId) {
    assertEquals(mob, costume.mob());
    assertEquals(itemId, costume.itemId());
  }
}
