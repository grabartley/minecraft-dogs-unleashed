package com.grahambartley.dogsunleashed.wood;

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
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.WoodType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class WoodenItemsTest {

  static Stream<Item> woodenItems() {
    return Stream.of(ModItems.DOG_HOUSE, ModItems.DOG_BED, ModItems.SLED);
  }

  static Stream<Arguments> woodsOnItems() {
    return woodenItems()
        .flatMap(
            item ->
                Stream.of(
                    Arguments.of(item, WoodType.SPRUCE, Blocks.SPRUCE_PLANKS),
                    Arguments.of(item, WoodType.CHERRY, Blocks.CHERRY_PLANKS),
                    Arguments.of(item, WoodType.WARPED, Blocks.WARPED_PLANKS)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("woodenItems")
  @DisplayName("a wooden item has one stack per vanilla wood, in the order vanilla lists them")
  void stacksListEveryWood(final Item item) {
    final List<ItemStack> stacks = WoodenItems.stacksOf(item);

    assertEquals(WoodPlanks.all(), stacks.stream().map(WoodenItems::of).toList());
    stacks.forEach(stack -> assertSame(item, stack.getItem()));
  }

  @ParameterizedTest(name = "{0} in {1}")
  @MethodSource("woodsOnItems")
  @DisplayName("an item reports the wood stored on it and names it with vanilla's planks name")
  void itemNamesItsWood(final Item item, final WoodType wood, final Block planks) {
    final ItemStack stack = new ItemStack(item);
    stack.set(ModComponents.WOOD, wood);
    final List<Text> tooltip = new ArrayList<>();

    WoodenItems.appendTooltip(stack, tooltip);

    assertSame(wood, WoodenItems.of(stack));
    assertEquals(
        List.of(planks.getTranslationKey()),
        tooltip.stream()
            .map(line -> ((TranslatableTextContent) line.getContent()).getKey())
            .toList());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("woodenItems")
  @DisplayName("oak is the default, so an oak item stacks with a plain one and other woods do not")
  void oakIsTheDefault(final Item item) {
    final ItemStack oak = new ItemStack(item);
    oak.set(ModComponents.WOOD, WoodType.OAK);
    final ItemStack spruce = new ItemStack(item);
    spruce.set(ModComponents.WOOD, WoodType.SPRUCE);

    assertSame(WoodType.OAK, WoodenItems.of(new ItemStack(item)));
    assertTrue(ItemStack.areItemsAndComponentsEqual(new ItemStack(item), oak));
    assertFalse(ItemStack.areItemsAndComponentsEqual(new ItemStack(item), spruce));
  }
}
