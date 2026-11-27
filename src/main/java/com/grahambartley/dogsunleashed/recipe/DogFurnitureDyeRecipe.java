package com.grahambartley.dogsunleashed.recipe;

import com.grahambartley.dogsunleashed.ModRecipeSerializers;
import com.grahambartley.dogsunleashed.item.DogFurnitureItem;
import java.util.Optional;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class DogFurnitureDyeRecipe extends SpecialCraftingRecipe {

  public DogFurnitureDyeRecipe(final CraftingRecipeCategory category) {
    super(category);
  }

  @Override
  public boolean matches(final CraftingRecipeInput input, final World world) {
    return dyed(input).isPresent();
  }

  @Override
  public ItemStack craft(
      final CraftingRecipeInput input, final RegistryWrapper.WrapperLookup lookup) {
    return dyed(input).orElse(ItemStack.EMPTY);
  }

  @Override
  public boolean fits(final int width, final int height) {
    return width * height >= 2;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return ModRecipeSerializers.DOG_FURNITURE_DYEING;
  }

  private static Optional<ItemStack> dyed(final CraftingRecipeInput input) {
    ItemStack stackOfFurniture = ItemStack.EMPTY;
    DogFurnitureItem furniture = null;
    DyeItem dye = null;
    for (int slot = 0; slot < input.getSize(); slot++) {
      final ItemStack stack = input.getStackInSlot(slot);
      if (stack.isEmpty()) {
        continue;
      }
      if (stack.getItem() instanceof DogFurnitureItem item && furniture == null) {
        furniture = item;
        stackOfFurniture = stack;
      } else if (stack.getItem() instanceof DyeItem dyeItem && dye == null) {
        dye = dyeItem;
      } else {
        return Optional.empty();
      }
    }
    if (furniture == null || dye == null) {
      return Optional.empty();
    }
    final ItemStack result = stackOfFurniture.copyWithCount(1);
    result.set(furniture.colourComponent(), dye.getColor());
    return Optional.of(result);
  }
}
