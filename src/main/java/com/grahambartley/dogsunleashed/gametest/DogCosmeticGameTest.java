package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModCosmeticItems;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmeticItem;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BannerPatterns;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public final class DogCosmeticGameTest implements FabricGameTest {

  private static final String BATCH = "dog-cosmetics";
  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos DOG_POS = new BlockPos(3, 2, 3);
  private static final int TICK_LIMIT = 20;
  private static final int BLUE = 0x3C44AA;
  private static final int FIRST_HOTBAR_SLOT = 31;

  private static ItemStack cosmetic(final DogCosmetic cosmetic) {
    return new ItemStack(ModCosmeticItems.of(cosmetic));
  }

  private static RegistryEntry<BannerPattern> pattern(
      final ServerWorld world, final net.minecraft.registry.RegistryKey<BannerPattern> key) {
    return world.getRegistryManager().get(RegistryKeys.BANNER_PATTERN).entryOf(key);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void everyCosmeticFitsTheCosmeticSlotAndNoOther(final TestContext context) {
    for (final DogCosmetic cosmetic : DogCosmetic.values()) {
      final ItemStack stack = cosmetic(cosmetic);
      context.assertTrue(
          DogEquipmentSlot.COSMETIC.canHold(stack), cosmetic + " should fit the cosmetic slot");
      context.assertFalse(
          DogEquipmentSlot.PENDANT.canHold(stack), cosmetic + " should not fit the pendant slot");
      context.assertTrue(
          DogCosmeticItem.cosmeticOf(stack).equals(Optional.of(cosmetic)),
          cosmetic + " stack should name its cosmetic");
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void dyedPatternedBandanaKeepsItsLookAcrossAReload(final TestContext context) {
    final ServerWorld world = context.getWorld();
    final ItemStack bandana = cosmetic(DogCosmetic.BANDANA);
    bandana.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(BLUE, true));
    bandana.set(
        DataComponentTypes.BANNER_PATTERNS,
        new BannerPatternsComponent.Builder()
            .add(pattern(world, BannerPatterns.STRIPE_LEFT), DyeColor.YELLOW)
            .add(pattern(world, BannerPatterns.CREEPER), DyeColor.BLACK)
            .build());
    final UnleashedDogEntity dog = DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS);
    dog.setAiDisabled(true);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.COSMETIC, bandana.copy());

    final NbtCompound nbt = new NbtCompound();
    dog.writeCustomDataToNbt(nbt);
    final UnleashedDogEntity reloaded =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS);
    reloaded.setAiDisabled(true);
    reloaded.readCustomDataFromNbt(nbt);

    final ItemStack worn = reloaded.getEquipmentHolder().getStack(DogEquipmentSlot.COSMETIC);
    context.assertTrue(
        ItemStack.areItemsAndComponentsEqual(bandana, worn),
        "The reloaded dog should wear the same dyed, patterned bandana but wears " + worn);
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void loomBannerSlotTakesABandanaButNotABow(final TestContext context) {
    final PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
    final LoomScreenHandler loom = new LoomScreenHandler(0, player.getInventory());

    context.assertTrue(
        loom.getBannerSlot().canInsert(cosmetic(DogCosmetic.BANDANA)),
        "A bandana should go in the loom's banner slot");
    context.assertFalse(
        loom.getBannerSlot().canInsert(cosmetic(DogCosmetic.COLLAR_BOW)),
        "A bow should not go in the loom's banner slot");
    context.assertTrue(
        loom.getBannerSlot().canInsert(new ItemStack(Items.WHITE_BANNER)),
        "Banners should still go in the loom's banner slot");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void shiftClickSendsABandanaToTheLoomBannerSlot(final TestContext context) {
    final PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
    final LoomScreenHandler loom = new LoomScreenHandler(0, player.getInventory());
    loom.getSlot(FIRST_HOTBAR_SLOT).setStack(cosmetic(DogCosmetic.BANDANA));

    loom.quickMove(player, FIRST_HOTBAR_SLOT);

    context.assertTrue(
        DogCosmeticItem.takesBannerPatterns(loom.getBannerSlot().getStack()),
        "Shift-clicking a bandana should move it into the loom's banner slot");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void loomPrintsAPatternOnADyedBandana(final TestContext context) {
    final PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
    final LoomScreenHandler loom = new LoomScreenHandler(0, player.getInventory());
    final ItemStack bandana = cosmetic(DogCosmetic.BANDANA);
    bandana.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(BLUE, true));
    loom.getBannerSlot().setStack(bandana);
    loom.getDyeSlot().setStack(new ItemStack(Items.YELLOW_DYE));

    final RegistryEntry<BannerPattern> chosen = loom.getBannerPatterns().get(0);
    loom.onButtonClick(player, 0);

    final ItemStack printed = loom.getOutputSlot().getStack();
    final BannerPatternsComponent layers =
        printed.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT);
    context.assertTrue(
        printed.isOf(ModCosmeticItems.of(DogCosmetic.BANDANA)),
        "The loom should hand back a bandana but made " + printed);
    context.assertTrue(
        layers.layers().size() == 1
            && layers.layers().get(0).pattern().equals(chosen)
            && layers.layers().get(0).color() == DyeColor.YELLOW,
        "The bandana should carry the chosen yellow pattern but has " + layers);
    context.assertTrue(
        DyedColorComponent.getColor(printed, 0) == DyedColorComponent.getColor(bandana, 0),
        "Printing a pattern should keep the bandana's dye");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void dyeColoursClothCosmeticsButNotHats(final TestContext context) {
    for (final DogCosmetic cosmetic : DogCosmetic.values()) {
      final ItemStack result = craft(context.getWorld(), cosmetic(cosmetic));
      if (cosmetic.isDyeable()) {
        context.assertTrue(
            result.isOf(ModCosmeticItems.of(cosmetic))
                && result.contains(DataComponentTypes.DYED_COLOR),
            cosmetic + " with blue dye should craft a dyed " + cosmetic + " but made " + result);
      } else {
        context.assertTrue(
            result.isEmpty(), cosmetic + " should not take dye but crafted " + result);
      }
    }
    context.complete();
  }

  private static ItemStack craft(final ServerWorld world, final ItemStack cosmetic) {
    final CraftingRecipeInput input =
        CraftingRecipeInput.create(2, 1, List.of(cosmetic, new ItemStack(Items.BLUE_DYE)));
    return world
        .getRecipeManager()
        .getFirstMatch(RecipeType.CRAFTING, input, world)
        .map(entry -> entry.value().craft(input, world.getRegistryManager()))
        .orElse(ItemStack.EMPTY);
  }
}
