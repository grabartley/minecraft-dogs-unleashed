package com.grahambartley.dogsunleashed.datagen;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.recipe.DogFurnitureDyeRecipe;
import com.grahambartley.dogsunleashed.wood.WoodPlanks;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.block.WoodType;
import net.minecraft.data.server.recipe.ComplexRecipeJsonBuilder;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

public class WoodenFurnitureRecipeProvider extends FabricRecipeProvider {

  public static final String DYEING_RECIPE = "dog_furniture_dyeing";

  public WoodenFurnitureRecipeProvider(
      final FabricDataOutput output,
      final CompletableFuture<RegistryWrapper.WrapperLookup> registries) {
    super(output, registries);
  }

  public static List<WoodenFurniture> furniture() {
    return List.of(
        new WoodenFurniture(
            ModItems.DOG_HOUSE,
            RecipeCategory.DECORATIONS,
            List.of("PPP", "PCP", "WPW"),
            Map.of(
                'C',
                Ingredient.ofItems(Items.WHITE_CARPET),
                'W',
                Ingredient.fromTag(ItemTags.WOOL))),
        new WoodenFurniture(
            ModItems.DOG_BED,
            RecipeCategory.DECORATIONS,
            List.of("CC", "PP"),
            Map.of('C', Ingredient.ofItems(Items.WHITE_CARPET))),
        new WoodenFurniture(
            ModItems.SLED,
            RecipeCategory.TRANSPORTATION,
            List.of("P P", "PPP", "I I"),
            Map.of('I', Ingredient.ofItems(Items.IRON_INGOT))));
  }

  @Override
  public void generate(final RecipeExporter exporter) {
    for (final WoodenFurniture piece : furniture()) {
      for (final WoodType wood : WoodPlanks.all()) {
        offer(exporter, piece, wood);
      }
    }
    ComplexRecipeJsonBuilder.create(DogFurnitureDyeRecipe::new)
        .offerTo(exporter, Identifier.of(DogsUnleashed.MOD_ID, DYEING_RECIPE));
  }

  private static void offer(
      final RecipeExporter exporter, final WoodenFurniture piece, final WoodType wood) {
    final Item planks = WoodPlanks.planksOf(wood).asItem();
    final Identifier id = piece.recipeId(wood);
    final ItemStack result = new ItemStack(piece.item());
    result.set(ModComponents.WOOD, wood);
    final ShapedRecipe recipe =
        new ShapedRecipe(
            piece.name(),
            CraftingRecipeJsonBuilder.toCraftingCategory(piece.category()),
            RawShapedRecipe.create(piece.keyWith(Ingredient.ofItems(planks)), piece.pattern()),
            result,
            true);
    exporter.accept(
        id,
        recipe,
        exporter
            .getAdvancementBuilder()
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(id))
            .criterion(hasItem(planks), conditionsFromItem(planks))
            .rewards(AdvancementRewards.Builder.recipe(id))
            .criteriaMerger(AdvancementRequirements.CriterionMerger.OR)
            .build(piece.advancementId(wood)));
  }
}
