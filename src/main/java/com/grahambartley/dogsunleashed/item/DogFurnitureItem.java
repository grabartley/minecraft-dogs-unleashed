package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.wood.WoodenItems;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.component.ComponentType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

public class DogFurnitureItem extends BlockItem {

  public static final DyeColor DEFAULT_COLOUR = DyeColor.WHITE;

  private final ComponentType<DyeColor> colour;

  public DogFurnitureItem(
      final Block block, final ComponentType<DyeColor> colour, final Settings settings) {
    super(block, WoodenItems.withDefault(settings).component(colour, DEFAULT_COLOUR));
    this.colour = colour;
  }

  public ComponentType<DyeColor> colourComponent() {
    return this.colour;
  }

  public DyeColor colourOf(final ItemStack stack) {
    return stack.getOrDefault(this.colour, DEFAULT_COLOUR);
  }

  @Override
  public Text getName(final ItemStack stack) {
    final String name = this.colourOf(stack).getName();
    final String colourName =
        name.substring(0, 1).toUpperCase() + name.substring(1).replace('_', ' ');
    return Text.translatable(this.getTranslationKey() + ".colored", colourName);
  }

  @Override
  public void appendTooltip(
      final ItemStack stack,
      final TooltipContext context,
      final List<Text> tooltip,
      final TooltipType type) {
    super.appendTooltip(stack, context, tooltip, type);
    WoodenItems.appendTooltip(stack, tooltip);
  }
}
