package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.ModComponents;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

public class DogGraveItem extends BlockItem {

  public DogGraveItem(Block block, Settings settings) {
    super(block, settings);
  }

  @Override
  public Text getName(ItemStack stack) {
    final String dogName = stack.getOrDefault(ModComponents.DOG_GRAVE_NAME, "");
    if (!dogName.isEmpty()) {
      return Text.translatable("block.dogs-unleashed.dog_grave.named", dogName);
    }
    return super.getName(stack);
  }

  @Override
  public void appendTooltip(
      ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
    super.appendTooltip(stack, context, tooltip, type);
    final String dogName = stack.getOrDefault(ModComponents.DOG_GRAVE_NAME, "");
    if (!dogName.isEmpty()) {
      tooltip.add(Text.literal(dogName).formatted(net.minecraft.util.Formatting.GRAY));
    }
  }
}
