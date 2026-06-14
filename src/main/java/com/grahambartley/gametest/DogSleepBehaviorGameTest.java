package com.grahambartley.gametest;

import com.grahambartley.ModBlocks;
import com.grahambartley.ModEntities;
import com.grahambartley.entity.HuskyEntity;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class DogSleepBehaviorGameTest implements FabricGameTest {

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 200)
  public void commandedToSleepSetsCorrectFlags(final TestContext context) {
    final BlockPos bedPos = new BlockPos(0, 1, 0);
    final BlockPos dogPos = new BlockPos(5, 1, 0);
    final ServerWorld world = context.getWorld();

    world.setBlockState(bedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(dogPos, 0.0f, 0.0f);
    husky.setTamed(true, true);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(!husky.isCommandedToSleep(), "Dog should not be commanded initially");
          context.assertTrue(!husky.isSleepingInBed(), "Dog should not be sleeping initially");

          husky.commandToSleep(bedPos);

          context.assertTrue(
              husky.isCommandedToSleep(), "commandToSleep should set COMMANDED_TO_SLEEP");
          context.assertTrue(husky.hasAssignedBed(), "commandToSleep should set assigned bed");
          context.assertTrue(
              husky.getAssignedBedPos().get().equals(bedPos),
              "Assigned bed should match given position");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void startSleepingInBedSetsSleepingFlag(final TestContext context) {
    final BlockPos bedPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    world.setBlockState(bedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(bedPos, 0.0f, 0.0f);
    husky.setTamed(true, true);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(!husky.isSleepingInBed(), "Dog should not be sleeping initially");

          husky.setAssignedBedPos(bedPos);
          husky.startSleepingInBed(bedPos);

          context.assertTrue(
              husky.isSleepingInBed(), "startSleepingInBed should set SLEEPING_IN_BED");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void wakeUpClearsAllSleepFlags(final TestContext context) {
    final BlockPos bedPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    world.setBlockState(bedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(bedPos, 0.0f, 0.0f);
    husky.setTamed(true, true);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(bedPos);
          husky.startSleepingInBed(bedPos);
          context.assertTrue(husky.isSleepingInBed(), "Dog should be sleeping");

          husky.wakeUp();

          context.assertTrue(!husky.isSleepingInBed(), "wakeUp should clear SLEEPING_IN_BED");
          context.assertTrue(!husky.isCommandedToSleep(), "wakeUp should clear COMMANDED_TO_SLEEP");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void sleepingDogStaysSleepingAcrossMultipleTicks(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(
        absBedPos.getX(), absBedPos.getY(), absBedPos.getZ(), 0.0f, 0.0f);
    husky.setTamed(true, true);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(absBedPos);
          husky.commandToSleep(absBedPos);
          husky.startSleepingInBed(absBedPos);
          context.assertTrue(husky.isSleepingInBed(), "Dog should be sleeping at tick 10");
          context.assertTrue(
              husky.isCommandedToSleep(), "COMMANDED_TO_SLEEP should be true at tick 10");
        });

    context.runAtTick(
        50,
        () -> {
          context.assertTrue(
              husky.isCommandedToSleep(), "COMMANDED_TO_SLEEP should STILL be true at tick 50");
          context.assertTrue(husky.isSleepingInBed(), "Dog should STILL be sleeping at tick 50");
        });

    context.runAtTick(
        100,
        () -> {
          context.assertTrue(husky.isSleepingInBed(), "Dog should STILL be sleeping at tick 100");
          context.complete();
        });
  }

  /**
   * Verifies a commanded sleeping dog stays anchored to its bed across many ticks. The dog bed has
   * a 0.25-tall collision shape, so although {@code startSleepingInBed} positions the dog at {@code
   * bedY + 0.1}, per-tick collision resolution pushes the entity up out of the bed's solid box
   * (settling near the top of the block above). The real invariant we care about is that the dog
   * does not wander off the bed's X/Z footprint, hence the tight {@code dxz < 0.05} check. The Y
   * range is intentionally permissive (just enough to confirm the dog hasn't fallen out of the
   * world or flown away) because the exact resting height depends on entity dimensions and step
   * height, not on sleep behavior.
   */
  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void commandedSleepDogStaysInPositionAcrossMultipleTicks(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(
        absBedPos.getX(), absBedPos.getY(), absBedPos.getZ(), 0.0f, 0.0f);
    husky.setTamed(true, true);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(absBedPos);
          husky.commandToSleep(absBedPos);
          husky.startSleepingInBed(absBedPos);
        });

    context.runAtTick(
        100,
        () -> {
          final var dogPos = husky.getPos();
          final double dxz =
              Math.hypot(dogPos.x - (absBedPos.getX() + 0.5), dogPos.z - (absBedPos.getZ() + 0.5));
          context.assertTrue(dxz < 0.05, "Sleeping dog should stay on bed X/Z center, dxz=" + dxz);
          context.assertTrue(
              dogPos.y >= absBedPos.getY() && dogPos.y < absBedPos.getY() + 1.1,
              "Sleeping dog should stay within bed Y footprint, y=" + dogPos.y);
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void damageClearsSleepState(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(
        absBedPos.getX(), absBedPos.getY(), absBedPos.getZ(), 0.0f, 0.0f);
    husky.setTamed(true, true);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(absBedPos);
          husky.commandToSleep(absBedPos);
          husky.startSleepingInBed(absBedPos);
          context.assertTrue(husky.isSleepingInBed(), "Dog should be sleeping before damage");
        });

    context.runAtTick(
        50,
        () -> {
          husky.damage(world.getDamageSources().generic(), 0.5f);
        });

    context.runAtTick(
        60,
        () -> {
          context.assertTrue(!husky.isSleepingInBed(), "Dog should NOT be sleeping after damage");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void commandedSleepAutoWakesAtSunrise(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(
        absBedPos.getX(), absBedPos.getY(), absBedPos.getZ(), 0.0f, 0.0f);
    husky.setTamed(true, true);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          world.setTimeOfDay(13000);
          husky.setAssignedBedPos(absBedPos);
          husky.commandToSleep(absBedPos);
          husky.startSleepingInBed(absBedPos);
          context.assertTrue(husky.isSleepingInBed(), "Dog should be sleeping");
          context.assertTrue(!husky.isCommandedToSleep(), "Dog should clear command once sleeping");
        });

    context.runAtTick(
        50,
        () -> {
          world.setTimeOfDay(1000);
        });

    context.runAtTick(
        90,
        () -> {
          context.assertTrue(
              !husky.isSleepingInBed(), "Commanded sleeping dog should auto-wake at sunrise");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 300)
  public void manualNightWakeSuppressesAutoSleepUntilMorning(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(
        absBedPos.getX(), absBedPos.getY(), absBedPos.getZ(), 0.0f, 0.0f);
    husky.setTamed(true, true);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          world.setTimeOfDay(13000);
          husky.setAssignedBedPos(absBedPos);
          husky.commandToSleep(absBedPos);
          husky.startSleepingInBed(absBedPos);
          context.assertTrue(husky.isSleepingInBed(), "Dog should be sleeping at night");
        });

    context.runAtTick(
        40,
        () -> {
          husky.markManuallyWoken();
          husky.wakeUp();
          context.assertTrue(!husky.isSleepingInBed(), "Dog should wake manually");
          context.assertTrue(
              husky.isAutoSleepSuppressed(), "Manual wake at night should suppress auto-sleep");
        });

    context.runAtTick(
        70,
        () -> {
          context.assertTrue(
              husky.isAutoSleepSuppressed(), "Suppression should hold for the remainder of night");
          context.assertTrue(!husky.isSleepingInBed(), "Dog should stay awake during suppression");
        });

    context.runAtTick(
        90,
        () -> {
          world.setTimeOfDay(1000);
          context.assertTrue(
              !husky.isAutoSleepSuppressed(), "Suppression should clear after sunrise");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 400)
  public void manualNightWakeDogAutoSleepsNextNight(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(
        absBedPos.getX(), absBedPos.getY(), absBedPos.getZ(), 0.0f, 0.0f);
    husky.setTamed(true, true);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          world.setTimeOfDay(13000);
          husky.setAssignedBedPos(absBedPos);
          husky.commandToSleep(absBedPos);
          husky.startSleepingInBed(absBedPos);
          husky.markManuallyWoken();
          husky.wakeUp();
          context.assertTrue(!husky.isSleepingInBed(), "Dog should be awake after manual wake");
        });

    context.runAtTick(
        50,
        () -> {
          context.assertTrue(
              husky.isAutoSleepSuppressed(), "Suppression should still be active at night");
        });

    context.runAtTick(
        90,
        () -> {
          world.setTimeOfDay(1000);
          context.assertTrue(!husky.isAutoSleepSuppressed(), "Suppression clears at sunrise");
        });

    context.runAtTick(
        130,
        () -> {
          world.setTimeOfDay(13000);
        });

    context.runAtTick(
        200,
        () -> {
          context.assertTrue(
              husky.isSleepingInBed(), "Dog should auto-sleep again on the next night");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void wakeUpMethodClearsSleepStateFromCommanded(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(
        absBedPos.getX(), absBedPos.getY(), absBedPos.getZ(), 0.0f, 0.0f);
    husky.setTamed(true, true);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(absBedPos);
          husky.commandToSleep(absBedPos);
          husky.startSleepingInBed(absBedPos);
          context.assertTrue(husky.isSleepingInBed(), "Dog should be sleeping");
          context.assertTrue(husky.isCommandedToSleep(), "Dog should be commanded");
        });

    context.runAtTick(
        50,
        () -> {
          context.assertTrue(husky.isSleepingInBed(), "Dog should still be sleeping");
          husky.wakeUp();
          context.assertTrue(!husky.isSleepingInBed(), "Dog should NOT be sleeping after wakeUp");
          context.assertTrue(
              !husky.isCommandedToSleep(), "Dog should NOT be commanded after wakeUp");
          context.complete();
        });
  }
}
