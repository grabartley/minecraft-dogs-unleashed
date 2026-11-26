package com.grahambartley.dogsunleashed.wood;

import com.grahambartley.dogsunleashed.ModComponents;
import java.util.List;
import net.minecraft.block.WoodType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class WoodenItems {

  private WoodenItems() {}

  public static Item.Settings withDefault(final Item.Settings settings) {
    return settings.component(ModComponents.WOOD, WoodPlanks.DEFAULT);
  }

  public static WoodType of(final ItemStack stack) {
    return stack.getOrDefault(ModComponents.WOOD, WoodPlanks.DEFAULT);
  }

  public static List<ItemStack> stacksOf(final Item item) {
    return WoodPlanks.all().stream()
        .map(
            wood -> {
              final ItemStack stack = new ItemStack(item);
              stack.set(ModComponents.WOOD, wood);
              return stack;
            })
        .toList();
  }

  public static void appendTooltip(final ItemStack stack, final List<Text> tooltip) {
    tooltip.add(
        Text.translatable(WoodPlanks.planksOf(of(stack)).getTranslationKey())
            .formatted(Formatting.GRAY));
  }
}
