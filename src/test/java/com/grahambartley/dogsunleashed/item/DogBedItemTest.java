package com.grahambartley.dogsunleashed.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogBedItemTest {

  static Stream<Arguments> woods() {
    return Stream.of(
        Arguments.of(null, Blocks.OAK_PLANKS),
        Arguments.of(WoodType.SPRUCE, Blocks.SPRUCE_PLANKS),
        Arguments.of(WoodType.JUNGLE, Blocks.JUNGLE_PLANKS),
        Arguments.of(WoodType.WARPED, Blocks.WARPED_PLANKS));
  }

  @ParameterizedTest(name = "{0} shows {1}")
  @MethodSource("woods")
  @DisplayName("the tooltip names the wood, and an item with none counts as oak")
  void tooltipNamesTheWood(final WoodType wood, final Block planks) {
    final ItemStack stack = new ItemStack(ModItems.DOG_BED);
    if (wood != null) {
      stack.set(ModComponents.WOOD, wood);
    }
    final List<Text> tooltip = new ArrayList<>();

    stack.getItem().appendTooltip(stack, Item.TooltipContext.DEFAULT, tooltip, TooltipType.BASIC);

    assertEquals(
        List.of(planks.getTranslationKey()),
        tooltip.stream()
            .map(line -> ((TranslatableTextContent) line.getContent()).getKey())
            .toList());
  }
}
