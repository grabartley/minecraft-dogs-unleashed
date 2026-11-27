package com.grahambartley.dogsunleashed.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.ModRecipeSerializers;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.block.WoodType;
import net.minecraft.component.ComponentType;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogFurnitureDyeRecipeTest {

  private final DogFurnitureDyeRecipe recipe =
      new DogFurnitureDyeRecipe(CraftingRecipeCategory.MISC);

  static Stream<Arguments> dyeings() {
    return Stream.of(
        Arguments.of(
            ModItems.DOG_HOUSE, ModComponents.DOG_HOUSE_COLOR, WoodType.SPRUCE, DyeColor.RED),
        Arguments.of(
            ModItems.DOG_HOUSE, ModComponents.DOG_HOUSE_COLOR, WoodType.CRIMSON, DyeColor.BLACK),
        Arguments.of(ModItems.DOG_BED, ModComponents.DOG_BED_COLOR, WoodType.BAMBOO, DyeColor.LIME),
        Arguments.of(ModItems.DOG_BED, ModComponents.DOG_BED_COLOR, WoodType.OAK, DyeColor.WHITE));
  }

  @ParameterizedTest(name = "{2} {0} + {3} dye")
  @MethodSource("dyeings")
  @DisplayName("dyeing a house or bed in the grid sets its colour and keeps its wood")
  void dyeingKeepsTheWood(
      final Item item,
      final ComponentType<DyeColor> colour,
      final WoodType wood,
      final DyeColor dye) {
    final ItemStack furniture = new ItemStack(item);
    furniture.set(ModComponents.WOOD, wood);
    final CraftingRecipeInput input = grid(furniture, new ItemStack(DyeItem.byColor(dye)));

    assertTrue(this.recipe.matches(input, null));
    final ItemStack result = this.recipe.craft(input, null);
    assertSame(item, result.getItem());
    assertEquals(1, result.getCount());
    assertEquals(dye, result.get(colour));
    assertEquals(wood, result.get(ModComponents.WOOD));
  }

  static Stream<Arguments> rejectedGrids() {
    return Stream.of(
        Arguments.of("dye alone", List.of(new ItemStack(Items.RED_DYE))),
        Arguments.of("house alone", List.of(new ItemStack(ModItems.DOG_HOUSE))),
        Arguments.of(
            "two dyes",
            List.of(
                new ItemStack(ModItems.DOG_BED),
                new ItemStack(Items.RED_DYE),
                new ItemStack(Items.BLUE_DYE))),
        Arguments.of(
            "two beds",
            List.of(
                new ItemStack(ModItems.DOG_BED),
                new ItemStack(ModItems.DOG_BED),
                new ItemStack(Items.RED_DYE))),
        Arguments.of(
            "sled and dye", List.of(new ItemStack(ModItems.SLED), new ItemStack(Items.RED_DYE))),
        Arguments.of(
            "bed, dye and a stick",
            List.of(
                new ItemStack(ModItems.DOG_BED),
                new ItemStack(Items.RED_DYE),
                new ItemStack(Items.STICK))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rejectedGrids")
  @DisplayName("only one house or bed with one dye matches")
  void otherGridsDoNotMatch(final String name, final List<ItemStack> stacks) {
    final CraftingRecipeInput input = grid(stacks.toArray(ItemStack[]::new));

    assertFalse(this.recipe.matches(input, null));
    assertTrue(this.recipe.craft(input, null).isEmpty());
  }

  @Test
  @DisplayName("the recipe uses its registered serializer and fits any grid of two or more slots")
  void serializerAndFit() {
    assertSame(ModRecipeSerializers.DOG_FURNITURE_DYEING, this.recipe.getSerializer());
    assertTrue(this.recipe.fits(2, 1));
    assertFalse(this.recipe.fits(1, 1));
  }

  private static CraftingRecipeInput grid(final ItemStack... stacks) {
    final List<ItemStack> slots = new ArrayList<>(List.of(stacks));
    while (slots.size() < 9) {
      slots.add(ItemStack.EMPTY);
    }
    return CraftingRecipeInput.create(3, 3, slots);
  }
}
