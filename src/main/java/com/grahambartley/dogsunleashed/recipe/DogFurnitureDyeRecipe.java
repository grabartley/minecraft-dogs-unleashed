package com.grahambartley.dogsunleashed.recipe;

import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.ModRecipeSerializers;
import java.util.Map;
import java.util.Optional;
import net.minecraft.component.ComponentType;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.DyeColor;
import net.minecraft.world.World;

public class DogFurnitureDyeRecipe extends SpecialCraftingRecipe {

  private static final Map<Item, ComponentType<DyeColor>> COLOURS =
      Map.of(
          ModItems.DOG_HOUSE, ModComponents.DOG_HOUSE_COLOR,
          ModItems.DOG_BED, ModComponents.DOG_BED_COLOR);

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
    ItemStack furniture = ItemStack.EMPTY;
    DyeItem dye = null;
    for (int slot = 0; slot < input.getSize(); slot++) {
      final ItemStack stack = input.getStackInSlot(slot);
      if (stack.isEmpty()) {
        continue;
      }
      if (colourOf(stack.getItem()) != null && furniture.isEmpty()) {
        furniture = stack;
      } else if (stack.getItem() instanceof DyeItem dyeItem && dye == null) {
        dye = dyeItem;
      } else {
        return Optional.empty();
      }
    }
    if (furniture.isEmpty() || dye == null) {
      return Optional.empty();
    }
    final ItemStack result = furniture.copyWithCount(1);
    result.set(colourOf(furniture.getItem()), dye.getColor());
    return Optional.of(result);
  }

  private static ComponentType<DyeColor> colourOf(final Item item) {
    return COLOURS.get(item);
  }
}
