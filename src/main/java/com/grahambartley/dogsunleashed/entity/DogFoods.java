package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModItems;
import java.util.stream.Stream;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;

public final class DogFoods {

  private static final Item[] BREEDING_ITEMS = {
    Items.CHICKEN,
    Items.COOKED_CHICKEN,
    Items.BEEF,
    Items.COOKED_BEEF,
    Items.PORKCHOP,
    Items.COOKED_PORKCHOP,
    Items.MUTTON,
    Items.COOKED_MUTTON,
    Items.RABBIT,
    Items.COOKED_RABBIT,
    Items.ROTTEN_FLESH
  };
  private static final Item[] TAMING_ITEMS = plus(BREEDING_ITEMS, Items.BONE);
  private static final Item[] ATTENTION_ITEMS = plus(TAMING_ITEMS, ModItems.DOG_TREAT);

  private static final Ingredient BREEDING_INGREDIENT = Ingredient.ofItems(BREEDING_ITEMS);
  private static final Ingredient TAMING_INGREDIENT = Ingredient.ofItems(TAMING_ITEMS);
  private static final Ingredient TREAT_INGREDIENT = Ingredient.ofItems(ModItems.DOG_TREAT);
  private static final Ingredient ATTENTION_INGREDIENT = Ingredient.ofItems(ATTENTION_ITEMS);

  private DogFoods() {}

  public static Ingredient breedingIngredient() {
    return BREEDING_INGREDIENT;
  }

  public static Ingredient tamingIngredient() {
    return TAMING_INGREDIENT;
  }

  public static Ingredient attentionIngredient() {
    return ATTENTION_INGREDIENT;
  }

  public static boolean isBreedingItem(final ItemStack stack) {
    return BREEDING_INGREDIENT.test(stack);
  }

  public static boolean isTamingItem(final ItemStack stack) {
    return TAMING_INGREDIENT.test(stack);
  }

  public static boolean isTreatItem(final ItemStack stack) {
    return TREAT_INGREDIENT.test(stack);
  }

  public static boolean isAttentionItem(final ItemStack stack) {
    return ATTENTION_INGREDIENT.test(stack);
  }

  public static boolean isHoldingAttentionItem(final PlayerEntity player) {
    return isAttentionItem(player.getMainHandStack()) || isAttentionItem(player.getOffHandStack());
  }

  public static boolean isHoldingTreat(final PlayerEntity player) {
    return isTreatItem(player.getMainHandStack()) || isTreatItem(player.getOffHandStack());
  }

  private static Item[] plus(final Item[] items, final Item extra) {
    return Stream.concat(Stream.of(items), Stream.of(extra)).toArray(Item[]::new);
  }
}
