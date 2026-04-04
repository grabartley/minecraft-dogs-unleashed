package com.grahambartley.gametest;

import com.grahambartley.ModBlocks;
import com.grahambartley.ModEntities;
import com.grahambartley.block.DogBedBlock;
import com.grahambartley.block.entity.DogBedBlockEntity;
import com.grahambartley.entity.HuskyEntity;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

public final class DogBedBlockGameTest implements FabricGameTest {

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dogBedCanBePlaced(final TestContext context) {
    final BlockPos bedPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    world.setBlockState(bedPos, ModBlocks.DOG_BED.getDefaultState());

    final BlockState placedState = world.getBlockState(bedPos);
    context.assertTrue(placedState.isOf(ModBlocks.DOG_BED), "Dog bed should be placed");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dogBedHasBlockEntity(final TestContext context) {
    final BlockPos bedPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    world.setBlockState(bedPos, ModBlocks.DOG_BED.getDefaultState());

    final BlockEntity blockEntity = world.getBlockEntity(bedPos);
    context.assertTrue(
        blockEntity instanceof DogBedBlockEntity, "Dog bed should have DogBedBlockEntity");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dogBedColorCanBeSetAndRetrieved(final TestContext context) {
    final BlockPos bedPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    world.setBlockState(bedPos, ModBlocks.DOG_BED.getDefaultState());

    context.runAtTick(
        5,
        () -> {
          final BlockEntity blockEntity = world.getBlockEntity(bedPos);
          context.assertTrue(
              blockEntity instanceof DogBedBlockEntity, "Block entity should be DogBedBlockEntity");
          final DogBedBlockEntity dogBedEntity = (DogBedBlockEntity) blockEntity;

          dogBedEntity.setColor(DyeColor.GREEN);
          context.assertTrue(
              dogBedEntity.getColor() == DyeColor.GREEN, "Dog bed color should be green after set");

          dogBedEntity.setColor(DyeColor.RED);
          context.assertTrue(
              dogBedEntity.getColor() == DyeColor.RED, "Dog bed color should be red after set");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dogBedColorCanBeChanged(final TestContext context) {
    final BlockPos bedPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    world.setBlockState(bedPos, ModBlocks.DOG_BED.getDefaultState());

    final BlockEntity blockEntity = world.getBlockEntity(bedPos);
    context.assertTrue(
        blockEntity instanceof DogBedBlockEntity, "Block entity should be DogBedBlockEntity");
    final DogBedBlockEntity dogBedEntity = (DogBedBlockEntity) blockEntity;
    dogBedEntity.setColor(DyeColor.BLUE);
    context.assertTrue(
        dogBedEntity.getColor() == DyeColor.BLUE, "Dog bed color should be changed to blue");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dogCanBeAssignedToBed(final TestContext context) {
    final BlockPos bedPos = new BlockPos(0, 1, 0);
    final BlockPos dogPos = new BlockPos(2, 1, 0);
    final ServerWorld world = context.getWorld();

    world.setBlockState(bedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(dogPos, 0.0f, 0.0f);
    husky.setTamed(true, true);
    husky.setNoAi(true); // Prevent wandering into other test structures
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          final BlockEntity blockEntity = world.getBlockEntity(bedPos);
          context.assertTrue(
              blockEntity instanceof DogBedBlockEntity, "Block entity should be DogBedBlockEntity");
          final DogBedBlockEntity dogBedEntity = (DogBedBlockEntity) blockEntity;

          dogBedEntity.setAssignedDog(husky);
          husky.setAssignedBedPos(bedPos);

          context.assertTrue(dogBedEntity.hasAssignedDog(), "Bed should have assigned dog");
          context.assertTrue(husky.hasAssignedBed(), "Dog should have assigned bed");
          context.assertTrue(
              husky.getAssignedBedPos().isPresent()
                  && husky.getAssignedBedPos().get().equals(bedPos),
              "Dog's assigned bed position should match");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dogCanBeCommandedToSleep(final TestContext context) {
    final BlockPos bedPos = new BlockPos(0, 1, 0);
    final BlockPos dogPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    world.setBlockState(bedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(dogPos, 0.0f, 0.0f);
    husky.setTamed(true, true);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(bedPos);
          husky.startSleepingInBed(bedPos);

          context.assertTrue(
              husky.isSleepingInBed(), "Dog should be sleeping after startSleepingInBed");
          context.assertTrue(
              husky.getAssignedBedPos().isPresent()
                  && husky.getAssignedBedPos().get().equals(bedPos),
              "Dog should have bed position set");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dogWakesUpWhenDamaged(final TestContext context) {
    final BlockPos bedPos = new BlockPos(0, 1, 0);
    final BlockPos dogPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    world.setBlockState(bedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(dogPos, 0.0f, 0.0f);
    husky.setTamed(true, true);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(bedPos);
          husky.startSleepingInBed(bedPos);
          context.assertTrue(husky.isSleepingInBed(), "Dog should be sleeping");
        });

    context.runAtTick(
        20,
        () -> {
          husky.damage(world.getDamageSources().generic(), 1.0f);

          context.assertTrue(!husky.isSleepingInBed(), "Dog should wake up when damaged");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void clearAssignedBedWorks(final TestContext context) {
    final BlockPos bedPos = new BlockPos(0, 1, 0);
    final BlockPos dogPos = new BlockPos(2, 1, 0);
    final ServerWorld world = context.getWorld();

    world.setBlockState(bedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(dogPos, 0.0f, 0.0f);
    husky.setTamed(true, true);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(bedPos);
          context.assertTrue(husky.hasAssignedBed(), "Dog should have assigned bed");

          husky.clearAssignedBed();
          context.assertTrue(
              !husky.hasAssignedBed(), "Dog should not have assigned bed after clearAssignedBed");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dogBedIsInAxeMineableTag(final TestContext context) {
    final BlockState dogBedState = ModBlocks.DOG_BED.getDefaultState();
    context.assertTrue(
        dogBedState.isIn(net.minecraft.registry.tag.BlockTags.AXE_MINEABLE),
        "Dog bed should be in axe mineable tag");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dogBedHasCorrectHardness(final TestContext context) {
    final float hardness = ModBlocks.DOG_BED.getHardness();
    context.assertTrue(hardness == 2.0f, "Dog bed should have hardness of 2.0 (same as oak wood)");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void pendingAssignmentCanBeSetAndConsumed(final TestContext context) {
    final UUID playerUuid = UUID.randomUUID();
    final UUID dogUuid = UUID.randomUUID();

    DogBedBlock.setPendingAssignment(playerUuid, dogUuid);

    final UUID consumed = DogBedBlock.consumePendingAssignment(playerUuid);
    context.assertTrue(
        dogUuid.equals(consumed), "Consumed pending assignment should match dog UUID");

    final UUID secondConsume = DogBedBlock.consumePendingAssignment(playerUuid);
    context.assertTrue(
        secondConsume == null, "Pending assignment should be null after consumption");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void reAssigningDogToNewBedClearsOldBed(final TestContext context) {
    final BlockPos oldBedPos = new BlockPos(0, 1, 0);
    final BlockPos newBedPos = new BlockPos(3, 1, 0);
    final BlockPos dogPos = new BlockPos(1, 1, 0);
    final ServerWorld world = context.getWorld();

    world.setBlockState(oldBedPos, ModBlocks.DOG_BED.getDefaultState());
    world.setBlockState(newBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(dogPos, 0.0f, 0.0f);
    husky.setTamed(true, true);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          final DogBedBlockEntity oldBedEntity =
              (DogBedBlockEntity) world.getBlockEntity(oldBedPos);
          oldBedEntity.setAssignedDog(husky);
          husky.setAssignedBedPos(oldBedPos);

          context.assertTrue(oldBedEntity.hasAssignedDog(), "Old bed should have assigned dog");

          final DogBedBlockEntity newBedEntity =
              (DogBedBlockEntity) world.getBlockEntity(newBedPos);
          final DogBedBlockEntity oldBedCheck = (DogBedBlockEntity) world.getBlockEntity(oldBedPos);

          husky
              .getAssignedBedPos()
              .ifPresent(
                  pos -> {
                    if (world.getBlockEntity(pos) instanceof DogBedBlockEntity oldEntity) {
                      oldEntity.clearAssignedDog(null);
                    }
                  });
          newBedEntity.setAssignedDog(husky);
          husky.setAssignedBedPos(newBedPos);

          context.assertTrue(newBedEntity.hasAssignedDog(), "New bed should have assigned dog");
          context.assertTrue(
              !oldBedCheck.hasAssignedDog(), "Old bed should no longer have assigned dog");
          context.assertTrue(
              husky.getAssignedBedPos().isPresent()
                  && husky.getAssignedBedPos().get().equals(newBedPos),
              "Dog's bed pos should be new bed");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dogDeathFreesAssignedBed(final TestContext context) {
    final BlockPos bedPos = new BlockPos(0, 1, 0);
    final BlockPos dogPos = new BlockPos(1, 1, 0);
    final ServerWorld world = context.getWorld();

    world.setBlockState(bedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(dogPos, 0.0f, 0.0f);
    husky.setTamed(true, true);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          final DogBedBlockEntity bedEntity = (DogBedBlockEntity) world.getBlockEntity(bedPos);
          bedEntity.setAssignedDog(husky);
          husky.setAssignedBedPos(bedPos);

          context.assertTrue(bedEntity.hasAssignedDog(), "Bed should have assigned dog");
        });

    context.runAtTick(
        20,
        () -> {
          husky.damage(world.getDamageSources().generic(), 999.0f);
        });

    context.runAtTick(
        30,
        () -> {
          final DogBedBlockEntity bedEntity = (DogBedBlockEntity) world.getBlockEntity(bedPos);
          context.assertTrue(!bedEntity.hasAssignedDog(), "Bed should be free after dog death");
          context.complete();
        });
  }
}
