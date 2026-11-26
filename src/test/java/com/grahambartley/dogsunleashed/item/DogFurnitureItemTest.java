package com.grahambartley.dogsunleashed.item;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.block.WoodType;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogFurnitureItemTest {

  static Stream<Arguments> furniture() {
    return Stream.of(
        Arguments.of(ModItems.DOG_HOUSE, ModComponents.DOG_HOUSE_COLOR, "dog_house"),
        Arguments.of(ModItems.DOG_BED, ModComponents.DOG_BED_COLOR, "dog_bed"));
  }

  static Stream<Arguments> colours() {
    return furniture()
        .flatMap(
            furniture ->
                Stream.of(
                    Arguments.of(furniture.get()[0], DyeColor.WHITE, "White"),
                    Arguments.of(furniture.get()[0], DyeColor.LIGHT_BLUE, "Light blue"),
                    Arguments.of(furniture.get()[0], DyeColor.RED, "Red")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("furniture")
  @DisplayName("each piece keeps its colour in its own component")
  void colourLivesInItsComponent(
      final DogFurnitureItem item, final ComponentType<DyeColor> colour, final String path) {
    final ItemStack stack = new ItemStack(item);
    stack.set(colour, DyeColor.CYAN);

    assertSame(colour, item.colourComponent());
    assertSame(DyeColor.CYAN, item.colourOf(stack));
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("colours")
  @DisplayName("the name says the colour")
  void nameSaysTheColour(final DogFurnitureItem item, final DyeColor colour, final String name) {
    final ItemStack stack = new ItemStack(item);
    stack.set(item.colourComponent(), colour);

    final TranslatableTextContent content =
        (TranslatableTextContent) item.getName(stack).getContent();

    assertEquals(item.getTranslationKey() + ".colored", content.getKey());
    assertArrayEquals(new Object[] {name}, content.getArgs());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("furniture")
  @DisplayName("white oak is the default, so a crafted, mined or old item stacks with a plain one")
  void whiteOakIsTheDefault(
      final DogFurnitureItem item, final ComponentType<DyeColor> colour, final String path) {
    final ItemStack marked = new ItemStack(item);
    marked.set(colour, DyeColor.WHITE);
    marked.set(ModComponents.WOOD, WoodType.OAK);
    final ItemStack red = new ItemStack(item);
    red.set(colour, DyeColor.RED);

    assertSame(DogFurnitureItem.DEFAULT_COLOUR, item.colourOf(new ItemStack(item)));
    assertTrue(ItemStack.areItemsAndComponentsEqual(new ItemStack(item), marked));
    assertFalse(ItemStack.areItemsAndComponentsEqual(new ItemStack(item), red));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("furniture")
  @DisplayName("the tooltip names the wood with vanilla's planks name")
  void tooltipNamesTheWood(
      final DogFurnitureItem item, final ComponentType<DyeColor> colour, final String path) {
    final ItemStack stack = new ItemStack(item);
    stack.set(ModComponents.WOOD, WoodType.BIRCH);
    final List<Text> tooltip = new ArrayList<>();

    item.appendTooltip(stack, Item.TooltipContext.DEFAULT, tooltip, TooltipType.BASIC);

    assertEquals(
        List.of("block.minecraft.birch_planks"),
        tooltip.stream()
            .map(line -> ((TranslatableTextContent) line.getContent()).getKey())
            .toList());
  }
}
