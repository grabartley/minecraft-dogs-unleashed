package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModCosmeticItems;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.DogVocalization;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.CreativeTestPlayer;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmeticItem;
import com.grahambartley.dogsunleashed.item.cosmetic.MobHead;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BannerPatterns;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
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
  public void dyeColoursOnlyBandanasAndBows(final TestContext context) {
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

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void everyCosmeticGoesOnWithARightClick(final TestContext context) {
    final List<ItemStack> wearables = new ArrayList<>();
    for (final DogCosmetic cosmetic : DogCosmetic.values()) {
      wearables.add(cosmetic(cosmetic));
    }
    for (final MobHead head : MobHead.values()) {
      wearables.add(new ItemStack(head.item()));
    }
    final ServerPlayerEntity owner = CreativeTestPlayer.join(context);
    owner.changeGameMode(GameMode.SURVIVAL);
    for (final ItemStack wearable : wearables) {
      final UnleashedDogEntity dog =
          DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS, owner.getUuid());
      dog.setAiDisabled(true);
      owner.setStackInHand(Hand.MAIN_HAND, wearable.copyWithCount(2));

      dog.interactMob(owner, Hand.MAIN_HAND);

      context.assertTrue(
          ItemStack.areItemsEqual(
              dog.getEquipmentHolder().getStack(DogEquipmentSlot.COSMETIC), wearable),
          wearable.getItem() + " should go on the dog with a right click");
      context.assertTrue(
          owner.getStackInHand(Hand.MAIN_HAND).getCount() == 1,
          wearable.getItem() + " should cost one item from the hand");
      dog.discard();
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void everyMobHeadFitsTheCosmeticSlotAndNoOther(final TestContext context) {
    for (final MobHead head : MobHead.values()) {
      final ItemStack stack = new ItemStack(head.item());
      context.assertTrue(
          DogEquipmentSlot.COSMETIC.canHold(stack), head + " should fit the cosmetic slot");
      context.assertFalse(
          DogEquipmentSlot.PENDANT.canHold(stack), head + " should not fit the pendant slot");
      context.assertTrue(
          DogEquipmentSlot.directEquipSlotFor(stack) == DogEquipmentSlot.COSMETIC,
          head + " should go straight on with a right click");
      context.assertTrue(
          MobHead.of(stack).equals(Optional.of(head)), head + " stack should name its head");
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aWornCarvedPumpkinIsCosmeticOnly(final TestContext context) {
    final UnleashedDogEntity bare =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS);
    bare.setAiDisabled(true);
    final UnleashedDogEntity dressed =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS);
    dressed.setAiDisabled(true);
    dressed
        .getEquipmentHolder()
        .setStack(DogEquipmentSlot.COSMETIC, new ItemStack(Items.CARVED_PUMPKIN));

    context.assertTrue(
        MobHead.of(dressed.getEquipmentHolder().getStack(DogEquipmentSlot.COSMETIC))
            .equals(Optional.of(MobHead.CARVED_PUMPKIN)),
        "The dog should wear the carved pumpkin");
    context.assertTrue(
        dressed.getEquippedStack(EquipmentSlot.HEAD).isEmpty(),
        "A worn pumpkin should not take the vanilla head slot that endermen check");
    context.assertTrue(
        dressed.getArmor() == bare.getArmor(), "A worn pumpkin should give no armour");
    context.assertTrue(
        dressed.getMaxHealth() == bare.getMaxHealth(), "A worn pumpkin should give no health");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aRealBarkRestartsTheBarkClock(final TestContext context) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.BEAGLE, DOG_POS);
    dog.setAiDisabled(true);
    context.assertTrue(
        dog.getVocalization().ticksSinceBark(0.0f) > 1000.0f,
        "A dog that never barked should read as long past its last bark");

    dog.damage(dog.getDamageSources().generic(), 1.0f);

    context.assertTrue(
        dog.getVocalization().getBarkCooldownTicks() > 0, "A hurt beagle should have barked");
    context.assertTrue(
        dog.getVocalization().ticksSinceBark(0.5f) == 0.5f,
        "A bark should restart the bark clock but reads "
            + dog.getVocalization().ticksSinceBark(0.5f));
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aBarkStatusRestartsTheBarkClockOnTheClient(final TestContext context) {
    final UnleashedDogEntity dog = DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS);
    dog.setAiDisabled(true);

    dog.handleStatus(DogVocalization.BARK_STATUS);

    context.assertTrue(
        dog.getVocalization().ticksSinceBark(0.5f) == 0.5f,
        "A bark status should restart the bark clock but reads "
            + dog.getVocalization().ticksSinceBark(0.5f));
    context.complete();
  }
}
