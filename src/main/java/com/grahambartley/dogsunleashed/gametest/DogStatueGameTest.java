package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModStatueBlocks;
import com.grahambartley.dogsunleashed.block.DogStatue;
import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.block.DogStatuePose;
import com.grahambartley.dogsunleashed.block.OxidizableDogStatueBlock;
import com.grahambartley.dogsunleashed.block.entity.DogGraveBlockEntity;
import com.grahambartley.dogsunleashed.block.entity.DogStatueBlockEntity;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.gametest.util.SurvivalTestPlayer;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class DogStatueGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos REL_BASE = new BlockPos(1, 2, 1);
  private static final BlockPos REL_DOG = new BlockPos(3, 2, 1);
  private static final BlockPos REL_GRAVE = new BlockPos(4, 2, 4);

  private static ServerPlayerEntity sneakingPlayer(final TestContext context) {
    final ServerPlayerEntity player = DogTestHelper.mockPlayerStandingClearInArena(context);
    player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
    player.setSneaking(true);
    return player;
  }

  private static DogStatueBlockEntity placeBase(final TestContext context, final BlockState state) {
    context.setBlockState(REL_BASE, state);
    return context.getBlockEntity(REL_BASE);
  }

  private static DogStatue huskyCarving() {
    return DogStatue.carved(
        new DogStatueLook(UnleashedDogBreed.HUSKY, false, 1, 0, 2, 0, null, null), "Rex");
  }

  @GameTest(templateName = ARENA, batchId = "statue")
  public void sneakUsingABaseCarvesYourNearbyDogWithItsName(final TestContext context) {
    final DogStatueBlockEntity base = placeBase(context, ModStatueBlocks.STONE.getDefaultState());
    final ServerPlayerEntity player = sneakingPlayer(context);
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, REL_DOG, player.getUuid());
    dog.setAiDisabled(true);
    dog.setCustomName(Text.literal("Rex"));

    context.useBlock(REL_BASE, player);

    final DogStatue statue = base.statue();
    context.assertTrue(statue != null, "Sneak using the base should carve the nearby dog");
    context.assertEquals(statue.look(), DogStatueLook.of(dog), "carved look");
    context.assertEquals(statue.name(), "Rex", "engraved name");
    context.assertEquals(statue.pose(), DogStatuePose.SIT, "first pose");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = "statue")
  public void anUnnamedDogIsEngravedWithItsPetManagerName(final TestContext context) {
    final DogStatueBlockEntity base = placeBase(context, ModStatueBlocks.STONE.getDefaultState());
    final ServerPlayerEntity player = sneakingPlayer(context);
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.BEAGLE, REL_DOG, player.getUuid());
    dog.setAiDisabled(true);
    final PetData pet =
        new PetData(
            dog.getUuid(),
            player.getUuid(),
            dog.getBreed(),
            "Biscuit",
            dog.getHealth(),
            dog.getMaxHealth(),
            dog.getBlockPos(),
            context.getWorld().getRegistryKey().getValue().toString(),
            PetLifeState.LIVING);
    PetManager.get(context.getWorld().getServer()).registerPet(pet);

    context.useBlock(REL_BASE, player);

    context.assertEquals(base.statue().name(), "Biscuit", "engraved name");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = "statue")
  public void anotherPlayersDogIsNeverCarved(final TestContext context) {
    final DogStatueBlockEntity base = placeBase(context, ModStatueBlocks.STONE.getDefaultState());
    final ServerPlayerEntity player = sneakingPlayer(context);
    DogTestHelper.spawnTamedDog(context, DogTestData.BEAGLE, REL_DOG, UUID.randomUUID())
        .setAiDisabled(true);

    context.useBlock(REL_BASE, player);

    context.assertTrue(base.statue() == null, "Only the player's own dogs can be carved");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = "statue")
  public void aLostDogIsCarvedFromItsGrave(final TestContext context) {
    final DogStatueBlockEntity base = placeBase(context, ModStatueBlocks.GOLD.getDefaultState());
    final ServerPlayerEntity player = sneakingPlayer(context);
    final UUID petId = UUID.randomUUID();
    final PetData pet =
        new PetData(
            petId,
            player.getUuid(),
            UnleashedDogBreed.BEAGLE,
            "Pepper",
            0.0f,
            20.0f,
            context.getAbsolutePos(REL_GRAVE),
            context.getWorld().getRegistryKey().getValue().toString(),
            PetLifeState.DECEASED);
    PetManager.get(context.getWorld().getServer()).registerPet(pet);
    context.setBlockState(REL_GRAVE, ModBlocks.DOG_GRAVE.getDefaultState());
    final DogGraveBlockEntity grave = context.getBlockEntity(REL_GRAVE);
    grave.setDogUuid(petId);
    grave.setDogName("Bentley");

    context.useBlock(REL_BASE, player);

    final DogStatue statue = base.statue();
    context.assertTrue(statue != null, "The grave's dog should be carved when no dog is near");
    context.assertEquals(statue.look(), DogStatueLook.of(pet), "carved look");
    context.assertEquals(statue.name(), "Bentley", "engraved name");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = "statue")
  public void usingACarvedStatueCyclesItsPose(final TestContext context) {
    final DogStatueBlockEntity base = placeBase(context, ModStatueBlocks.STONE.getDefaultState());
    base.carve(huskyCarving());
    final ServerPlayerEntity player = DogTestHelper.mockPlayerStandingClearInArena(context);
    player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);

    context.useBlock(REL_BASE, player);
    context.assertEquals(base.statue().pose(), DogStatuePose.STAND, "pose after one use");
    context.useBlock(REL_BASE, player);
    context.assertEquals(base.statue().pose(), DogStatuePose.HOWL, "pose after two uses");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = "statue")
  public void aStatueKeepsItsDogAcrossAReload(final TestContext context) {
    final DogStatueBlockEntity base = placeBase(context, ModStatueBlocks.STONE.getDefaultState());
    base.carve(huskyCarving());
    base.cyclePose();

    final BlockEntity reloaded =
        BlockEntity.createFromNbt(
            context.getAbsolutePos(REL_BASE),
            base.getCachedState(),
            base.createNbtWithIdentifyingData(context.getWorld().getRegistryManager()),
            context.getWorld().getRegistryManager());

    context.assertTrue(
        reloaded instanceof DogStatueBlockEntity loaded
            && huskyCarving().withPose(DogStatuePose.STAND).equals(loaded.statue()),
        "The reloaded statue should be the same dog in the same pose");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = "statue")
  public void waxingAndScrapingKeepTheStatue(final TestContext context) {
    final DogStatueBlockEntity base =
        placeBase(context, ModStatueBlocks.EXPOSED_COPPER.getDefaultState());
    base.carve(huskyCarving());
    final ServerPlayerEntity player = DogTestHelper.mockPlayerStandingClearInArena(context);

    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.HONEYCOMB));
    context.useBlock(REL_BASE, player);
    context.checkBlock(
        REL_BASE, block -> block == ModStatueBlocks.WAXED_EXPOSED_COPPER, "waxed base");
    context.assertEquals(
        ((DogStatueBlockEntity) context.getBlockEntity(REL_BASE)).statue(),
        huskyCarving(),
        "statue after waxing");

    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
    context.useBlock(REL_BASE, player);
    context.useBlock(REL_BASE, player);
    context.checkBlock(REL_BASE, block -> block == ModStatueBlocks.COPPER, "scraped base");
    context.assertEquals(
        ((DogStatueBlockEntity) context.getBlockEntity(REL_BASE)).statue(),
        huskyCarving(),
        "statue after scraping");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = "statue")
  public void ageingKeepsTheStatue(final TestContext context) {
    final BlockState copper = ModStatueBlocks.COPPER.getDefaultState();
    final DogStatueBlockEntity base = placeBase(context, copper);
    base.carve(huskyCarving());

    context.setBlockState(
        REL_BASE,
        ((OxidizableDogStatueBlock) ModStatueBlocks.COPPER)
            .getDegradationResult(copper)
            .orElseThrow());

    context.checkBlock(REL_BASE, block -> block == ModStatueBlocks.EXPOSED_COPPER, "aged base");
    context.assertEquals(
        ((DogStatueBlockEntity) context.getBlockEntity(REL_BASE)).statue(),
        huskyCarving(),
        "statue after ageing");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = "statue-break")
  public void twoPlayersBreakingOneStatueDropOneStatue(final TestContext context) {
    final DogStatueBlockEntity base = placeBase(context, ModStatueBlocks.STONE.getDefaultState());
    base.carve(huskyCarving());
    final ServerPlayerEntity first = SurvivalTestPlayer.joinAt(context, new Vec3d(2.5, 2, 3.5));
    final ServerPlayerEntity second = SurvivalTestPlayer.joinAt(context, new Vec3d(3.5, 2, 2.5));
    first.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
    second.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
    final BlockPos absBase = context.getAbsolutePos(REL_BASE);

    first.interactionManager.tryBreakBlock(absBase);
    second.interactionManager.tryBreakBlock(absBase);

    context.runAtTick(
        5,
        () -> {
          final List<ItemStack> drops =
              DogTestHelper.droppedStacksOf(context, ModStatueBlocks.STONE.asItem());
          SurvivalTestPlayer.leave(context, first);
          SurvivalTestPlayer.leave(context, second);
          context.checkBlock(REL_BASE, block -> block == Blocks.AIR, "broken base");
          context.assertEquals(drops.size(), 1, "dropped statue stacks");
          context.assertEquals(drops.get(0).getCount(), 1, "statues in the drop");
          context.assertEquals(
              drops.get(0).get(ModComponents.DOG_STATUE), huskyCarving(), "statue on the item");
          context.complete();
        });
  }
}
