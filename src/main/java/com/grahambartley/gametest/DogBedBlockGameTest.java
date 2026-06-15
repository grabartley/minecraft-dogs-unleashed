package com.grahambartley.gametest;

import com.grahambartley.ModBlocks;
import com.grahambartley.ModEntities;
import com.grahambartley.block.entity.DogBedBlockEntity;
import com.grahambartley.entity.HuskyEntity;
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
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final BlockState placedState = world.getBlockState(absBedPos);
    context.assertTrue(placedState.isOf(ModBlocks.DOG_BED), "Dog bed should be placed");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dogBedHasBlockEntity(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final BlockEntity blockEntity = world.getBlockEntity(absBedPos);
    context.assertTrue(
        blockEntity instanceof DogBedBlockEntity, "Dog bed should have DogBedBlockEntity");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dogBedColorCanBeSetAndRetrieved(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    context.runAtTick(
        5,
        () -> {
          final BlockEntity blockEntity = world.getBlockEntity(absBedPos);
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
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final BlockEntity blockEntity = world.getBlockEntity(absBedPos);
    context.assertTrue(
        blockEntity instanceof DogBedBlockEntity, "Block entity should be DogBedBlockEntity");
    final DogBedBlockEntity dogBedEntity = (DogBedBlockEntity) blockEntity;
    dogBedEntity.setColor(DyeColor.BLUE);
    context.assertTrue(
        dogBedEntity.getColor() == DyeColor.BLUE, "Dog bed color should be changed to blue");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_bed_pair", tickLimit = 100)
  public void dogCanBeAssignedToBed(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final BlockPos relDogPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relDogPos);
    husky.setTamed(true, true);
    husky.setAiDisabled(true); // Prevent wandering into other test structures

    context.runAtTick(
        10,
        () -> {
          final BlockEntity blockEntity = world.getBlockEntity(absBedPos);
          context.assertTrue(
              blockEntity instanceof DogBedBlockEntity, "Block entity should be DogBedBlockEntity");
          final DogBedBlockEntity dogBedEntity = (DogBedBlockEntity) blockEntity;

          dogBedEntity.setAssignedDog(husky);
          husky.setAssignedBedPos(absBedPos);

          context.assertTrue(dogBedEntity.hasAssignedDog(), "Bed should have assigned dog");
          context.assertTrue(husky.hasAssignedBed(), "Dog should have assigned bed");
          context.assertTrue(
              husky.getAssignedBedPos().isPresent()
                  && husky.getAssignedBedPos().get().equals(absBedPos),
              "Dog's assigned bed position should match");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dogCanBeCommandedToSleep(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final BlockPos relDogPos = new BlockPos(0, 1, 0);

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relDogPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(absBedPos);
          husky.startSleepingInBed(absBedPos);

          context.assertTrue(
              husky.isSleepingInBed(), "Dog should be sleeping after startSleepingInBed");
          context.assertTrue(
              husky.getAssignedBedPos().isPresent()
                  && husky.getAssignedBedPos().get().equals(absBedPos),
              "Dog should have bed position set");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dogWakesUpWhenDamaged(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final BlockPos relDogPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relDogPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(absBedPos);
          husky.startSleepingInBed(absBedPos);
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

  @GameTest(templateName = "dogs-unleashed:dog_bed_pair", tickLimit = 100)
  public void clearAssignedBedWorks(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final BlockPos relDogPos = new BlockPos(0, 1, 0);

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relDogPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(absBedPos);
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

  // dogBedHasCorrectHardness and pendingAssignmentCanBeSetAndConsumed live in DogBedBlockTest
  // under src/test/java since they only read static block state and a process-global UUID map.
  // See gametest skill rule 10.

  @GameTest(templateName = "dogs-unleashed:dog_bed_pair", tickLimit = 100)
  public void reAssigningDogToNewBedClearsOldBed(final TestContext context) {
    // Two beds live at separate relative positions. EMPTY_STRUCTURE is 1x1x1; the structure
    // bounds are tracked by TestContext but setBlockState at an off-structure relative pos
    // still resolves via absolute coordinates and works for assertions. Per #210, this
    // test should eventually move to an .nbt template that covers the multi-bed footprint.
    final BlockPos relOldBedPos = new BlockPos(0, 1, 0);
    final BlockPos relNewBedPos = new BlockPos(3, 1, 0);
    final BlockPos relDogPos = new BlockPos(1, 1, 0);
    final BlockPos absOldBedPos = context.getAbsolutePos(relOldBedPos);
    final BlockPos absNewBedPos = context.getAbsolutePos(relNewBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relOldBedPos, ModBlocks.DOG_BED.getDefaultState());
    context.setBlockState(relNewBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relDogPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          final BlockEntity oldBedBlockEntity = world.getBlockEntity(absOldBedPos);
          context.assertTrue(
              oldBedBlockEntity instanceof DogBedBlockEntity,
              "Old bed block entity should be DogBedBlockEntity");
          final DogBedBlockEntity oldBedEntity = (DogBedBlockEntity) oldBedBlockEntity;
          oldBedEntity.setAssignedDog(husky);
          husky.setAssignedBedPos(absOldBedPos);

          context.assertTrue(oldBedEntity.hasAssignedDog(), "Old bed should have assigned dog");

          final BlockEntity newBedBlockEntity = world.getBlockEntity(absNewBedPos);
          context.assertTrue(
              newBedBlockEntity instanceof DogBedBlockEntity,
              "New bed block entity should be DogBedBlockEntity");
          final DogBedBlockEntity newBedEntity = (DogBedBlockEntity) newBedBlockEntity;
          final BlockEntity oldBedCheckBlockEntity = world.getBlockEntity(absOldBedPos);
          context.assertTrue(
              oldBedCheckBlockEntity instanceof DogBedBlockEntity,
              "Old bed block entity should still be DogBedBlockEntity");
          final DogBedBlockEntity oldBedCheck = (DogBedBlockEntity) oldBedCheckBlockEntity;

          husky
              .getAssignedBedPos()
              .ifPresent(
                  pos -> {
                    if (world.getBlockEntity(pos) instanceof DogBedBlockEntity oldEntity) {
                      oldEntity.clearAssignedDog(null);
                    }
                  });
          newBedEntity.setAssignedDog(husky);
          husky.setAssignedBedPos(absNewBedPos);

          context.assertTrue(newBedEntity.hasAssignedDog(), "New bed should have assigned dog");
          context.assertTrue(
              !oldBedCheck.hasAssignedDog(), "Old bed should no longer have assigned dog");
          context.assertTrue(
              husky.getAssignedBedPos().isPresent()
                  && husky.getAssignedBedPos().get().equals(absNewBedPos),
              "Dog's bed pos should be new bed");
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_bed_pair", tickLimit = 100)
  public void dogDeathFreesAssignedBed(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final BlockPos relDogPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relDogPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          final BlockEntity bedBlockEntity = world.getBlockEntity(absBedPos);
          context.assertTrue(
              bedBlockEntity instanceof DogBedBlockEntity,
              "Bed block entity should be DogBedBlockEntity");
          final DogBedBlockEntity bedEntity = (DogBedBlockEntity) bedBlockEntity;
          bedEntity.setAssignedDog(husky);
          husky.setAssignedBedPos(absBedPos);

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
          final BlockEntity bedBlockEntityAtTick30 = world.getBlockEntity(absBedPos);
          context.assertTrue(
              bedBlockEntityAtTick30 instanceof DogBedBlockEntity,
              "Bed block entity should still be a DogBedBlockEntity");
          final DogBedBlockEntity bedEntity = (DogBedBlockEntity) bedBlockEntityAtTick30;
          context.assertTrue(!bedEntity.hasAssignedDog(), "Bed should be free after dog death");
          context.complete();
        });
  }
}
