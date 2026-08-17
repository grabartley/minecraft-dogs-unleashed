package com.grahambartley.dogsunleashed.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;

public final class DogFoods {

  private static final Ingredient BREEDING_INGREDIENT =
      Ingredient.ofItems(
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
          Items.ROTTEN_FLESH);
  private static final Ingredient TAMING_INGREDIENT =
      Ingredient.ofItems(
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
          Items.ROTTEN_FLESH,
          Items.BONE);

  private DogFoods() {}

  public static Ingredient breedingIngredient() {
    return BREEDING_INGREDIENT;
  }

  public static Ingredient tamingIngredient() {
    return TAMING_INGREDIENT;
  }

  public static boolean isBreedingItem(final ItemStack stack) {
    return BREEDING_INGREDIENT.test(stack);
  }

  public static boolean isTamingItem(final ItemStack stack) {
    return TAMING_INGREDIENT.test(stack);
  }

  public static boolean isHoldingTamingOrBreedingItem(final PlayerEntity player) {
    return isTamingItem(player.getMainHandStack())
        || isTamingItem(player.getOffHandStack())
        || isBreedingItem(player.getMainHandStack())
        || isBreedingItem(player.getOffHandStack());
  }
}
