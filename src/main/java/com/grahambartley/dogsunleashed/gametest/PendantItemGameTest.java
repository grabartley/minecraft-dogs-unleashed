package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModPendantItems;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.GeneratedGameTest;
import com.grahambartley.dogsunleashed.gametest.util.PendantTestHelper;
import com.grahambartley.dogsunleashed.item.pendant.PendantType;
import com.grahambartley.dogsunleashed.screenhandler.DogEquipmentScreenHandler;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.village.raid.Raid;

public final class PendantItemGameTest implements FabricGameTest {

  private static final String BATCH = "pendant-items";
  private static final int TICK_LIMIT = 20;

  @CustomTestProvider
  public Collection<TestFunction> equipmentScreenTakesEachPendant() {
    return Arrays.stream(PendantType.values())
        .map(
            type ->
                GeneratedGameTest.of(
                    BATCH,
                    "pendantitemgametest.screen." + type.id(),
                    PendantTestHelper.ARENA,
                    TICK_LIMIT,
                    context -> testEquipmentScreen(context, type)))
        .toList();
  }

  private static void testEquipmentScreen(final TestContext context, final PendantType type) {
    final ServerPlayerEntity owner =
        PendantTestHelper.survivalOwnerAt(context, PendantTestHelper.OWNER_POS);
    final UnleashedDogEntity dog =
        PendantTestHelper.dogWearing(context, null, owner, PendantTestHelper.DOG_POS);
    owner.getInventory().setStack(0, new ItemStack(ModPendantItems.of(type)));
    final DogEquipmentScreenHandler handler =
        new DogEquipmentScreenHandler(1, owner.getInventory(), dog);

    handler.quickMove(owner, DogEquipmentScreenHandler.ROUTING.hotbarStart());

    context.assertTrue(
        dog.getPendants().worn() == type,
        "Shift-clicking " + type.id() + " in the equipment screen should hang it on the collar");
    context.complete();
  }

  @CustomTestProvider
  public Collection<TestFunction> chargingRecipeMakesEachPendant() {
    return Arrays.stream(PendantType.values())
        .map(
            type ->
                GeneratedGameTest.of(
                    BATCH,
                    "pendantitemgametest.chargingrecipe." + type.id(),
                    PendantTestHelper.ARENA,
                    TICK_LIMIT,
                    context -> testChargingRecipe(context, type)))
        .toList();
  }

  private static void testChargingRecipe(final TestContext context, final PendantType type) {
    final ServerWorld world = context.getWorld();
    final ItemStack reagent = reagentFor(type, world);

    final ItemStack result =
        craft(world, List.of(new ItemStack(ModPendantItems.BLANK_PENDANT), reagent));

    context.assertTrue(
        result.isOf(ModPendantItems.of(type)),
        "A blank pendant and " + reagent + " should charge into " + type.id() + ", got " + result);
    context.complete();
  }

  @GameTest(templateName = PendantTestHelper.ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void plainWhiteBannerDoesNotChargeTheIllagerWard(final TestContext context) {
    final ItemStack result =
        craft(
            context.getWorld(),
            List.of(
                new ItemStack(ModPendantItems.BLANK_PENDANT), new ItemStack(Items.WHITE_BANNER)));

    context.assertTrue(
        result.isEmpty(), "Only the ominous banner should charge the illager ward, got " + result);
    context.complete();
  }

  @GameTest(templateName = PendantTestHelper.ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void goldNuggetsAndLeatherMakeABlankPendant(final TestContext context) {
    final ItemStack nugget = new ItemStack(Items.GOLD_NUGGET);
    final List<ItemStack> grid =
        List.of(
            ItemStack.EMPTY,
            nugget,
            ItemStack.EMPTY,
            nugget,
            new ItemStack(Items.LEATHER),
            nugget,
            ItemStack.EMPTY,
            nugget,
            ItemStack.EMPTY);

    final ItemStack result =
        context
            .getWorld()
            .getRecipeManager()
            .getFirstMatch(
                RecipeType.CRAFTING, CraftingRecipeInput.create(3, 3, grid), context.getWorld())
            .map(
                entry ->
                    entry
                        .value()
                        .craft(
                            CraftingRecipeInput.create(3, 3, grid),
                            context.getWorld().getRegistryManager()))
            .orElse(ItemStack.EMPTY);

    context.assertTrue(
        result.isOf(ModPendantItems.BLANK_PENDANT), "The blank pendant recipe should match");
    context.complete();
  }

  @GameTest(templateName = PendantTestHelper.ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void blankPendantCannotBeWornButEveryChargedOneCan(final TestContext context) {
    context.assertFalse(
        DogEquipmentSlot.PENDANT.canHold(new ItemStack(ModPendantItems.BLANK_PENDANT)),
        "An uncharged blank must not fit the pendant slot");
    for (final PendantType type : PendantType.values()) {
      context.assertTrue(
          DogEquipmentSlot.PENDANT.canHold(new ItemStack(ModPendantItems.of(type))),
          type.id() + " should fit the pendant slot");
    }
    context.complete();
  }

  private static ItemStack reagentFor(final PendantType type, final ServerWorld world) {
    return switch (type) {
      case WARDING_UNDEAD -> new ItemStack(Items.BONE_BLOCK);
      case WARDING_ARTHROPOD -> new ItemStack(Items.COBWEB);
      case WARDING_ILLAGER -> ominousBanner(world);
      case WARDING_CREEPER -> new ItemStack(Items.COD);
      case GUARDIAN_FIRE -> new ItemStack(Items.MAGMA_CREAM);
      case GUARDIAN_FALL -> new ItemStack(Items.PHANTOM_MEMBRANE);
      case GUARDIAN_DROWNING -> new ItemStack(Items.PUFFERFISH);
      case VITALITY -> new ItemStack(Items.GLISTERING_MELON_SLICE);
      case LOYALTY -> new ItemStack(Items.TOTEM_OF_UNDYING);
    };
  }

  private static ItemStack ominousBanner(final ServerWorld world) {
    return Raid.getOminousBanner(
        world.getRegistryManager().getWrapperOrThrow(RegistryKeys.BANNER_PATTERN));
  }

  private static ItemStack craft(final ServerWorld world, final List<ItemStack> ingredients) {
    final CraftingRecipeInput input = CraftingRecipeInput.create(2, 1, ingredients);
    final Optional<RecipeEntry<CraftingRecipe>> match =
        world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, input, world);
    return match
        .map(entry -> entry.value().craft(input, world.getRegistryManager()))
        .orElse(ItemStack.EMPTY);
  }
}
