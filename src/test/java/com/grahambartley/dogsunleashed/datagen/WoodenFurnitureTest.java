package com.grahambartley.dogsunleashed.datagen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModItems;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.block.WoodType;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class WoodenFurnitureTest {

  static Stream<Arguments> ids() {
    return Stream.of(
        Arguments.of(
            WoodType.SPRUCE,
            "dog_house",
            RecipeCategory.DECORATIONS,
            "dogs-unleashed:spruce_dog_house",
            "dogs-unleashed:recipes/decorations/spruce_dog_house"),
        Arguments.of(
            WoodType.DARK_OAK,
            "dog_bed",
            RecipeCategory.DECORATIONS,
            "dogs-unleashed:dark_oak_dog_bed",
            "dogs-unleashed:recipes/decorations/dark_oak_dog_bed"),
        Arguments.of(
            WoodType.BAMBOO,
            "sled",
            RecipeCategory.TRANSPORTATION,
            "dogs-unleashed:bamboo_sled",
            "dogs-unleashed:recipes/transportation/bamboo_sled"));
  }

  @ParameterizedTest(name = "{3}")
  @MethodSource("ids")
  @DisplayName("each wood's recipe and unlock advancement are named after the wood and the item")
  void idsNameTheWood(
      final WoodType wood,
      final String group,
      final RecipeCategory category,
      final String recipe,
      final String advancement) {
    final WoodenFurniture piece =
        new WoodenFurniture(ModItems.DOG_HOUSE, group, category, List.of("PP"), Map.of());

    assertEquals(Identifier.of(recipe), piece.recipeId(wood));
    assertEquals(Identifier.of(advancement), piece.advancementId(wood));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("ids")
  @DisplayName("the planks key joins the item's other keys without replacing them")
  void planksJoinTheOtherKeys(
      final WoodType wood,
      final String group,
      final RecipeCategory category,
      final String recipe,
      final String advancement) {
    final Ingredient carpet = Ingredient.ofItems(Items.WHITE_CARPET);
    final Ingredient planks = Ingredient.ofItems(Items.SPRUCE_PLANKS);
    final WoodenFurniture piece =
        new WoodenFurniture(
            ModItems.DOG_BED, group, category, List.of("CC", "PP"), Map.of('C', carpet));

    final Map<Character, Ingredient> key = piece.keyWith(planks);

    assertEquals(2, key.size());
    assertSame(carpet, key.get('C'));
    assertSame(planks, key.get(WoodenFurniture.PLANKS_KEY));
  }
}
