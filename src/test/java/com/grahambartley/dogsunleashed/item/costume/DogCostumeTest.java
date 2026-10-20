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
    "BEE, bee, bee_costume",
    "ENDER_DRAGON, ender_dragon, ender_dragon_costume",
    "CREEPER, creeper, creeper_costume",
    "WITHER, wither, wither_costume",
    "WARDEN, warden, warden_costume",
    "SHEEP, sheep, sheep_costume",
    "MOOSHROOM, mooshroom, mooshroom_costume",
    "CHICKEN, chicken, chicken_costume",
    "AXOLOTL, axolotl, axolotl_costume",
    "FROG, frog, frog_costume",
    "PANDA, panda, panda_costume",
    "TURTLE, turtle, turtle_costume",
    "ALLAY, allay, allay_costume"
  })
  @DisplayName("each costume is named after the mob it dresses the dog up as")
  void itemIdNamesTheMob(final DogCostume costume, final String mob, final String itemId) {
    assertEquals(mob, costume.mob());
    assertEquals(itemId, costume.itemId());
  }

  @ParameterizedTest(name = "{0} dyeable: {1}")
  @CsvSource({
    "SHEEP, true",
    "SPIDER, false",
    "BEE, false",
    "ENDER_DRAGON, false",
    "PANDA, false",
    "ALLAY, false"
  })
  @DisplayName("only the sheep costume takes dye, like leather armour")
  void onlyTheSheepTakesDye(final DogCostume costume, final boolean dyeable) {
    assertEquals(dyeable, costume.isDyeable());
  }

  @ParameterizedTest(name = "{0} undyed colour {1}")
  @CsvSource({"SHEEP, 16777215", "SPIDER, 16777215"})
  @DisplayName("an undyed costume draws untinted")
  void undyedCostumesAreUntinted(final DogCostume costume, final int undyed) {
    assertEquals(undyed, costume.undyedColor());
  }
}
