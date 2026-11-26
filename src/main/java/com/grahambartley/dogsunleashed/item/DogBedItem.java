package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.block.WoodPlanks;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

public class DogBedItem extends BlockItem {

  public DogBedItem(Block block, Settings settings) {
    super(block, settings);
  }

  @Override
  public Text getName(ItemStack stack) {
    DyeColor color = stack.getOrDefault(ModComponents.DOG_BED_COLOR, DyeColor.WHITE);
    String colorName =
        color.getName().substring(0, 1).toUpperCase()
            + color.getName().substring(1).replace('_', ' ');
    return Text.translatable("block.dogs-unleashed.dog_bed.colored", colorName);
  }

  @Override
  public void appendTooltip(
      final ItemStack stack,
      final TooltipContext context,
      final List<Text> tooltip,
      final TooltipType type) {
    super.appendTooltip(stack, context, tooltip, type);
    WoodPlanks.appendTooltip(stack, tooltip);
  }
}
