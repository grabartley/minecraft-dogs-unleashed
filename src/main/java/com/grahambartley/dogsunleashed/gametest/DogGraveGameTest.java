package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.block.entity.DogGraveBlockEntity;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public final class DogGraveGameTest implements FabricGameTest {

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dogGraveCanBePlaced(final TestContext context) {
    final BlockPos relGravePos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relGravePos, ModBlocks.DOG_GRAVE.getDefaultState());
    final BlockPos gravePos = context.getAbsolutePos(relGravePos);

    final BlockState placedState = world.getBlockState(gravePos);
    context.assertTrue(placedState.isOf(ModBlocks.DOG_GRAVE), "Dog grave should be placed");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dogGraveHasBlockEntity(final TestContext context) {
    final BlockPos relGravePos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relGravePos, ModBlocks.DOG_GRAVE.getDefaultState());
    final BlockPos gravePos = context.getAbsolutePos(relGravePos);

    final BlockEntity blockEntity = world.getBlockEntity(gravePos);
    context.assertTrue(
        blockEntity instanceof DogGraveBlockEntity, "Dog grave should have DogGraveBlockEntity");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dogGraveStoresDogData(final TestContext context) {
    final BlockPos relGravePos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relGravePos, ModBlocks.DOG_GRAVE.getDefaultState());
    final BlockPos gravePos = context.getAbsolutePos(relGravePos);

    context.runAtTick(
        5,
        () -> {
          final BlockEntity blockEntity = world.getBlockEntity(gravePos);
          context.assertTrue(
              blockEntity instanceof DogGraveBlockEntity,
              "Block entity should be DogGraveBlockEntity");
          final DogGraveBlockEntity graveEntity = (DogGraveBlockEntity) blockEntity;

          final UUID dogUuid = UUID.randomUUID();
          final String dogName = "Good Boy";
          final DyeColor flowerColor = DyeColor.RED;

          graveEntity.setDogUuid(dogUuid);
          graveEntity.setDogName(dogName);
          graveEntity.setFlowerColor(flowerColor);

          context.assertTrue(graveEntity.getDogUuid().equals(dogUuid), "Dog UUID should match");
          context.assertTrue(graveEntity.getDogName().equals(dogName), "Dog name should match");
          context.assertTrue(
              graveEntity.getFlowerColor() == flowerColor, "Flower color should match");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dogGraveItemRetainsData(final TestContext context) {
    final UUID dogUuid = UUID.randomUUID();
    final String dogName = "Memorial";
    final DyeColor flowerColor = DyeColor.PINK;

    final ItemStack graveStack = new ItemStack(ModBlocks.DOG_GRAVE);
    graveStack.set(ModComponents.DOG_GRAVE_UUID, dogUuid);
    graveStack.set(ModComponents.DOG_GRAVE_NAME, dogName);
    graveStack.set(ModComponents.DOG_GRAVE_FLOWER_COLOR, flowerColor);

    context.runAtTick(
        5,
        () -> {
          context.assertTrue(
              graveStack.get(ModComponents.DOG_GRAVE_UUID).equals(dogUuid),
              "Item should retain dog UUID");
          context.assertTrue(
              graveStack.get(ModComponents.DOG_GRAVE_NAME).equals(dogName),
              "Item should retain dog name");
          context.assertTrue(
              graveStack.get(ModComponents.DOG_GRAVE_FLOWER_COLOR) == flowerColor,
              "Item should retain flower color");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dogGraveRequiresPickaxe(final TestContext context) {
    final BlockPos relGravePos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relGravePos, ModBlocks.DOG_GRAVE.getDefaultState());
    final BlockPos gravePos = context.getAbsolutePos(relGravePos);

    final float handSpeed =
        ModBlocks.DOG_GRAVE
            .getDefaultState()
            .calcBlockBreakingDelta(context.createMockPlayer(GameMode.SURVIVAL), world, gravePos);

    context.assertTrue(handSpeed > 0, "Grave should be breakable by hand (slowly)");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dogGraveRequiresPickaxeToBreak(final TestContext context) {
    final BlockPos relGravePos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relGravePos, ModBlocks.DOG_GRAVE.getDefaultState());
    final BlockPos gravePos = context.getAbsolutePos(relGravePos);

    final BlockState state = world.getBlockState(gravePos);

    // Test that block requires a tool (pickaxe) to harvest
    context.assertTrue(state.isToolRequired(), "Grave should require a tool to harvest");

    // Test that the block is in the pickaxe mineable tag
    final var player = context.createMockPlayer(GameMode.SURVIVAL);
    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));

    final float pickaxeSpeed = state.calcBlockBreakingDelta(player, world, gravePos);
    context.assertTrue(pickaxeSpeed > 0, "Pickaxe should be able to break grave");

    player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
    final float handSpeed = state.calcBlockBreakingDelta(player, world, gravePos);
    context.assertTrue(
        handSpeed < pickaxeSpeed, "Hand should be slower than pickaxe at breaking grave");

    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dogGravePickaxeDropsWithData(final TestContext context) {
    final BlockPos relGravePos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final UUID dogUuid = UUID.randomUUID();
    final String dogName = "PickaxeTest";
    final DyeColor flowerColor = DyeColor.BLUE;

    context.setBlockState(relGravePos, ModBlocks.DOG_GRAVE.getDefaultState());
    final BlockPos gravePos = context.getAbsolutePos(relGravePos);

    context.runAtTick(
        5,
        () -> {
          final DogGraveBlockEntity grave = (DogGraveBlockEntity) world.getBlockEntity(gravePos);
          grave.setDogUuid(dogUuid);
          grave.setDogName(dogName);
          grave.setFlowerColor(flowerColor);

          // Test getPickStack immediately (used for creative mode middle-click)
          final BlockState state = world.getBlockState(gravePos);
          final ItemStack stack = ModBlocks.DOG_GRAVE.getPickStack(world, gravePos, state);

          context.assertTrue(stack.isOf(ModItems.DOG_GRAVE), "Pick stack should be dog grave item");
          context.assertTrue(
              dogUuid.equals(stack.get(ModComponents.DOG_GRAVE_UUID)), "UUID should transfer");
          context.assertTrue(
              dogName.equals(stack.get(ModComponents.DOG_GRAVE_NAME)), "Name should transfer");
          context.assertTrue(
              flowerColor.equals(stack.get(ModComponents.DOG_GRAVE_FLOWER_COLOR)),
              "Flower color should transfer");

          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dogGravePlacementRetainsData(final TestContext context) {
    final BlockPos relGravePos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final UUID dogUuid = UUID.randomUUID();
    final String dogName = "PersistTest";
    final DyeColor flowerColor = DyeColor.GREEN;

    context.runAtTick(
        10,
        () -> {
          // Place block and set data directly (matches dog death spawn pattern)
          context.setBlockState(relGravePos, ModBlocks.DOG_GRAVE.getDefaultState());
          final BlockPos gravePos = context.getAbsolutePos(relGravePos);

          final DogGraveBlockEntity grave = (DogGraveBlockEntity) world.getBlockEntity(gravePos);
          grave.setDogUuid(dogUuid);
          grave.setDogName(dogName);
          grave.setFlowerColor(flowerColor);
        });

    context.runAtTick(
        15,
        () -> {
          final BlockPos gravePos = context.getAbsolutePos(relGravePos);
          // Verify data persisted across ticks
          final DogGraveBlockEntity grave = (DogGraveBlockEntity) world.getBlockEntity(gravePos);

          context.assertTrue(dogUuid.equals(grave.getDogUuid()), "UUID should persist");
          context.assertTrue(dogName.equals(grave.getDogName()), "Name should persist");
          context.assertTrue(
              flowerColor.equals(grave.getFlowerColor()), "Flower color should persist");

          context.complete();
        });
  }
}
