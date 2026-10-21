package com.grahambartley.dogsunleashed.trade;

import com.grahambartley.dogsunleashed.ModItemTags;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.world.World;

public final class CostumeRecipeIngredients {

  private CostumeRecipeIngredients() {}

  public static List<List<CostumeTradePrice.Tier>> recipesOf(
      final World world, final Item costume) {
    final List<List<CostumeTradePrice.Tier>> recipes = new ArrayList<>();
    for (final RecipeEntry<?> entry : world.getRecipeManager().values()) {
      if (entry.value().getResult(world.getRegistryManager()).isOf(costume)) {
        recipes.add(ingredientsOf(entry).stream().map(CostumeRecipeIngredients::tierOf).toList());
      }
    }
    return recipes;
  }

  private static Set<Item> ingredientsOf(final RecipeEntry<?> entry) {
    final Set<Item> items = new LinkedHashSet<>();
    for (final Ingredient ingredient : entry.value().getIngredients()) {
      Arrays.stream(ingredient.getMatchingStacks())
          .findFirst()
          .map(ItemStack::getItem)
          .ifPresent(items::add);
    }
    return items;
  }

  private static CostumeTradePrice.Tier tierOf(final Item ingredient) {
    final ItemStack stack = new ItemStack(ingredient);
    if (stack.isIn(ModItemTags.RARE_COSTUME_INGREDIENTS)) {
      return CostumeTradePrice.Tier.RARE;
    }
    return stack.isIn(ItemTags.WOOL) ? CostumeTradePrice.Tier.WOOL : CostumeTradePrice.Tier.COMMON;
  }
}
