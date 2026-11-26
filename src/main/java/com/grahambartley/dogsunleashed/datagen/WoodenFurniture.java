package com.grahambartley.dogsunleashed.datagen;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.WoodType;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public record WoodenFurniture(
    Item item, RecipeCategory category, List<String> pattern, Map<Character, Ingredient> keys) {

  public static final char PLANKS_KEY = 'P';

  public String name() {
    return Registries.ITEM.getId(this.item).getPath();
  }

  public Identifier recipeId(final WoodType wood) {
    return Identifier.of(DogsUnleashed.MOD_ID, wood.name() + "_" + this.name());
  }

  public Identifier advancementId(final WoodType wood) {
    return this.recipeId(wood).withPrefixedPath("recipes/" + this.category.getName() + "/");
  }

  public Map<Character, Ingredient> keyWith(final Ingredient planks) {
    final Map<Character, Ingredient> key = new HashMap<>(this.keys);
    key.put(PLANKS_KEY, planks);
    return key;
  }
}
