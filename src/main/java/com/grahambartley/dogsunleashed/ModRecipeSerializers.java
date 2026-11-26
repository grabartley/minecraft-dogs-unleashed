package com.grahambartley.dogsunleashed;

import com.grahambartley.dogsunleashed.recipe.DogFurnitureDyeRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModRecipeSerializers {

  public static final RecipeSerializer<DogFurnitureDyeRecipe> DOG_FURNITURE_DYEING =
      Registry.register(
          Registries.RECIPE_SERIALIZER,
          Identifier.of(DogsUnleashed.MOD_ID, "dog_furniture_dyeing"),
          new SpecialRecipeSerializer<>(DogFurnitureDyeRecipe::new));

  private ModRecipeSerializers() {}

  public static void initialize() {}
}
