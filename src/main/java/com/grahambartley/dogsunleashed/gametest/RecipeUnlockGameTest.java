package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModPendantItems;
import com.grahambartley.dogsunleashed.gametest.util.GeneratedGameTest;
import com.grahambartley.dogsunleashed.item.pendant.PendantType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.Identifier;

public final class RecipeUnlockGameTest implements FabricGameTest {

  private static final String BATCH = "recipe-unlocks";
  private static final int TICK_LIMIT = 20;
  private static final Identifier BLANK_PENDANT_RECIPE =
      Identifier.of(DogsUnleashed.MOD_ID, "blank_pendant");

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH)
  public void everyModRecipeHasAnUnlockAdvancement(final TestContext context) {
    final MinecraftServer server = context.getWorld().getServer();
    final Set<Identifier> unlocked =
        server.getAdvancementLoader().getAdvancements().stream()
            .flatMap(entry -> entry.value().rewards().recipes().stream())
            .collect(Collectors.toSet());

    final Set<Identifier> locked =
        server.getRecipeManager().values().stream()
            .map(RecipeEntry::id)
            .filter(id -> id.getNamespace().equals(DogsUnleashed.MOD_ID))
            .filter(id -> !unlocked.contains(id))
            .collect(Collectors.toCollection(TreeSet::new));

    context.assertTrue(
        locked.isEmpty(),
        "Recipes with no unlock advancement under advancement/recipes: " + locked);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH)
  public void holdingABlankPendantUnlocksEveryPendantRecipe(final TestContext context) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

    pickUp(player, ModPendantItems.BLANK_PENDANT);

    final List<Identifier> missing =
        Arrays.stream(PendantType.values())
            .map(type -> Identifier.of(DogsUnleashed.MOD_ID, type.itemId()))
            .filter(id -> !hasRecipe(player, id))
            .toList();
    context.assertTrue(missing.isEmpty(), "Blank Pendant left these recipes locked: " + missing);
    context.complete();
  }

  @CustomTestProvider
  public Collection<TestFunction> blankPendantUnlocksFromEitherIngredient() {
    return Stream.of(Items.GOLD_NUGGET, Items.LEATHER)
        .map(
            ingredient ->
                GeneratedGameTest.of(
                    BATCH,
                    "recipeunlockgametest.blankpendant."
                        + Registries.ITEM.getId(ingredient).getPath(),
                    FabricGameTest.EMPTY_STRUCTURE,
                    TICK_LIMIT,
                    context -> testBlankPendantUnlocksFrom(context, ingredient)))
        .toList();
  }

  private static void testBlankPendantUnlocksFrom(final TestContext context, final Item item) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    context.assertFalse(
        hasRecipe(player, BLANK_PENDANT_RECIPE), "Blank Pendant should start locked");

    pickUp(player, item);

    context.assertTrue(
        hasRecipe(player, BLANK_PENDANT_RECIPE),
        "Picking up " + item + " should unlock the Blank Pendant recipe");
    context.complete();
  }

  private static void pickUp(final ServerPlayerEntity player, final Item item) {
    final ItemStack stack = new ItemStack(item);
    player.getInventory().insertStack(stack.copy());
    Criteria.INVENTORY_CHANGED.trigger(player, player.getInventory(), stack);
  }

  private static boolean hasRecipe(final ServerPlayerEntity player, final Identifier id) {
    return player.getRecipeBook().contains(id);
  }
}
