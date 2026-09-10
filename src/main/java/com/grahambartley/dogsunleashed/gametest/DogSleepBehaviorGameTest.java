package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.block.DogBedBlock;
import com.grahambartley.dogsunleashed.entity.DogPlaySession;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;

public final class DogSleepBehaviorGameTest implements FabricGameTest {

  private static void prepareSleepBatch(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, world.getServer());
    DogPlaySession.clearActivePlaySessions();
    DogBedBlock.clearPendingAssignments();
  }

  private static void teardownSleepBatch(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, world.getServer());
    DogPlaySession.clearActivePlaySessions();
    DogBedBlock.clearPendingAssignments();
  }

  @BeforeBatch(batchId = "sleep-flags")
  public void beforeFlagsBatch(final ServerWorld world) {
    prepareSleepBatch(world);
  }

  @AfterBatch(batchId = "sleep-flags")
  public void afterFlagsBatch(final ServerWorld world) {
    teardownSleepBatch(world);
  }

  @BeforeBatch(batchId = "sleep-stay-asleep")
  public void beforeStayAsleepBatch(final ServerWorld world) {
    prepareSleepBatch(world);
  }

  @AfterBatch(batchId = "sleep-stay-asleep")
  public void afterStayAsleepBatch(final ServerWorld world) {
    teardownSleepBatch(world);
  }

  @BeforeBatch(batchId = "sleep-wake-at-sunrise")
  public void beforeWakeAtSunriseBatch(final ServerWorld world) {
    prepareSleepBatch(world);
  }

  @AfterBatch(batchId = "sleep-wake-at-sunrise")
  public void afterWakeAtSunriseBatch(final ServerWorld world) {
    teardownSleepBatch(world);
  }

  @BeforeBatch(batchId = "sleep-suppress")
  public void beforeSuppressBatch(final ServerWorld world) {
    prepareSleepBatch(world);
  }

  @AfterBatch(batchId = "sleep-suppress")
  public void afterSuppressBatch(final ServerWorld world) {
    teardownSleepBatch(world);
  }

  @BeforeBatch(batchId = "sleep-resleep")
  public void beforeResleepBatch(final ServerWorld world) {
    prepareSleepBatch(world);
  }

  @AfterBatch(batchId = "sleep-resleep")
  public void afterResleepBatch(final ServerWorld world) {
    teardownSleepBatch(world);
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-flags", tickLimit = 200)
  public void commandedToSleepSetsCorrectFlags(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final BlockPos relDogPos = new BlockPos(0, 1, 0);

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, relDogPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(!husky.isCommandedToSleep(), "Dog should not be commanded initially");
          context.assertTrue(!husky.isSleepingInBed(), "Dog should not be sleeping initially");

          husky.getSleepController().commandToSleep(absBedPos);

          context.assertTrue(
              husky.isCommandedToSleep(), "commandToSleep should set COMMANDED_TO_SLEEP");
          context.assertTrue(husky.hasAssignedBed(), "commandToSleep should set assigned bed");
          context.assertTrue(
              husky.getAssignedBedPos().get().equals(absBedPos),
              "Assigned bed should match given position");
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-flags", tickLimit = 200)
  public void startSleepingInBedSetsSleepingFlag(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(!husky.isSleepingInBed(), "Dog should not be sleeping initially");

          husky.setAssignedBedPos(absBedPos);
          husky.getSleepController().startSleepingInBed(absBedPos);

          context.assertTrue(
              husky.isSleepingInBed(), "startSleepingInBed should set SLEEPING_IN_BED");
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-flags", tickLimit = 200)
  public void wakeUpClearsAllSleepFlags(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(absBedPos);
          husky.getSleepController().startSleepingInBed(absBedPos);
          context.assertTrue(husky.isSleepingInBed(), "Dog should be sleeping");

          husky.wakeUp();

          context.assertTrue(!husky.isSleepingInBed(), "wakeUp should clear SLEEPING_IN_BED");
          context.assertTrue(!husky.isCommandedToSleep(), "wakeUp should clear COMMANDED_TO_SLEEP");
          context.complete();
        });
  }

  @GameTest(
      templateName = "dogs-unleashed:dog_arena",
      batchId = "sleep-stay-asleep",
      tickLimit = 60)
  public void sleepingDogStaysSleepingAcrossMultipleTicks(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);
    husky.setAiDisabled(true);

    context.runAtTick(
        10,
        () -> {
          world.setTimeOfDay(13000);
          husky.setAssignedBedPos(absBedPos);
          husky.getSleepController().commandToSleep(absBedPos);
          husky.getSleepController().startSleepingInBed(absBedPos);
          context.assertTrue(husky.isSleepingInBed(), "Dog should be sleeping at tick 10");
          context.assertTrue(
              !husky.isCommandedToSleep(),
              "COMMANDED_TO_SLEEP should be cleared once sleeping starts");
        });

    context.runAtTick(
        25,
        () ->
            context.assertTrue(husky.isSleepingInBed(), "Dog should STILL be sleeping at tick 25"));

    context.runAtTick(
        59,
        () -> {
          context.assertTrue(
              husky.isSleepingInBed(), "Dog should still be sleeping at the end of the window");
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-flags", tickLimit = 200)
  public void commandedSleepDogStaysInPositionAcrossMultipleTicks(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(absBedPos);
          husky.getSleepController().commandToSleep(absBedPos);
          husky.getSleepController().startSleepingInBed(absBedPos);
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

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-flags", tickLimit = 200)
  public void damageClearsSleepState(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(absBedPos);
          husky.getSleepController().commandToSleep(absBedPos);
          husky.getSleepController().startSleepingInBed(absBedPos);
          context.assertTrue(husky.isSleepingInBed(), "Dog should be sleeping before damage");
        });

    context.runAtTick(
        50,
        () -> {
          husky.damage(world.getDamageSources().generic(), 0.5f);
          context.assertTrue(!husky.isSleepingInBed(), "Dog should NOT be sleeping after damage");
          context.complete();
        });
  }

  @GameTest(
      templateName = "dogs-unleashed:dog_arena",
      batchId = "sleep-wake-at-sunrise",
      tickLimit = 200)
  public void commandedSleepAutoWakesAtSunrise(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);
    husky.setInvulnerable(true);
    @SuppressWarnings("removal")
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    husky.setOwnerUuid(owner.getUuid());
    final AtomicBoolean armedForWake = new AtomicBoolean(false);

    context.runAtTick(
        10,
        () -> {
          world.setTimeOfDay(13000);
          husky.setAssignedBedPos(absBedPos);
          husky.getSleepController().commandToSleep(absBedPos);
          husky.getSleepController().startSleepingInBed(absBedPos);
          context.assertTrue(husky.isSleepingInBed(), "Dog should be sleeping");
          context.assertTrue(!husky.isCommandedToSleep(), "Dog should clear command once sleeping");
        });

    context.runAtTick(
        50,
        () -> {
          world.setTimeOfDay(1000);
          armedForWake.set(true);
        });

    context.runAtEveryTick(
        () -> {
          if (!armedForWake.get()) return;
          if (!husky.isSleepingInBed()) {
            context.complete();
          }
        });

    context.runAtTick(
        199,
        () -> {
          context.assertTrue(
              !husky.isSleepingInBed(),
              "Commanded sleeping dog should auto-wake at sunrise within the test window");
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-suppress", tickLimit = 300)
  public void manualNightWakeSuppressesAutoSleepUntilMorning(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);
    @SuppressWarnings("removal")
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    husky.setOwnerUuid(owner.getUuid());

    context.runAtTick(
        10,
        () -> {
          pinNight(world);
          husky.setAssignedBedPos(absBedPos);
          husky.getSleepController().commandToSleep(absBedPos);
          husky.getSleepController().startSleepingInBed(absBedPos);
          context.assertTrue(husky.isSleepingInBed(), "Dog should be sleeping at night");
        });

    context.runAtTick(
        40,
        () -> {
          pinNight(world);
          husky.getSleepController().markManuallyWoken();
          husky.wakeUp();
          context.assertTrue(!husky.isSleepingInBed(), "Dog should wake manually");
          context.assertTrue(
              husky.getSleepController().isAutoSleepSuppressed(),
              "Manual wake at night should suppress auto-sleep");
        });

    context.runAtTick(
        70,
        () -> {
          pinNight(world);
          context.assertTrue(
              husky.getSleepController().isAutoSleepSuppressed(),
              "Suppression should hold for the remainder of night");
          context.assertTrue(!husky.isSleepingInBed(), "Dog should stay awake during suppression");
        });

    context.runAtTick(
        90,
        () -> {
          pinDay(world);
          context.assertTrue(
              !husky.getSleepController().isAutoSleepSuppressed(),
              "Suppression should clear after sunrise");
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-resleep", tickLimit = 600)
  public void manualNightWakeDogAutoSleepsNextNight(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);
    husky.setInvulnerable(true);
    @SuppressWarnings("removal")
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    husky.setOwnerUuid(owner.getUuid());
    final AtomicBoolean armedForAutoSleep = new AtomicBoolean(false);
    final AtomicBoolean hasAutoSlept = new AtomicBoolean(false);

    context.runAtTick(
        10,
        () -> {
          pinNight(world);
          husky.setAssignedBedPos(absBedPos);
          husky.getSleepController().commandToSleep(absBedPos);
          husky.getSleepController().startSleepingInBed(absBedPos);
          husky.getSleepController().markManuallyWoken();
          husky.wakeUp();
          context.assertTrue(!husky.isSleepingInBed(), "Dog should be awake after manual wake");
          husky.setAiDisabled(true);
        });

    context.runAtTick(
        50,
        () -> {
          pinNight(world);
          context.assertTrue(
              husky.getSleepController().isAutoSleepSuppressed(),
              "Suppression should still be active at night");
        });

    context.runAtTick(
        90,
        () -> {
          pinDay(world);
          context.assertTrue(
              !husky.getSleepController().isAutoSleepSuppressed(), "Suppression clears at sunrise");
        });

    context.runAtTick(
        130,
        () -> {
          pinNight(world);
          husky.refreshPositionAndAngles(
              absBedPos.getX() + 0.5, absBedPos.getY(), absBedPos.getZ() + 0.5, 0.0f, 0.0f);
          husky.setVelocity(0, 0, 0);
          husky.setSitting(false);
          husky.setTarget(null);
          husky.setAngerTime(0);
          husky.setHealth(husky.getMaxHealth());
          husky.setAiDisabled(false);
          armedForAutoSleep.set(true);
        });

    context.runAtEveryTick(
        () -> {
          if (!armedForAutoSleep.get()) return;
          if (husky.isSleepingInBed()) {
            hasAutoSlept.set(true);
          }
        });

    context.runAtTick(
        599,
        () -> {
          context.assertTrue(
              hasAutoSlept.get(),
              "AutoSleepGoal should re-fire and put the dog to sleep on the next night within"
                  + " the test window");
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-flags", tickLimit = 200)
  public void wakeUpFromBedClearsSleepingFlag(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);
    husky.setAiDisabled(true);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(absBedPos);
          husky.getSleepController().commandToSleep(absBedPos);
          husky.getSleepController().startSleepingInBed(absBedPos);
          context.assertTrue(husky.isSleepingInBed(), "Dog should be sleeping");
          context.assertTrue(
              !husky.isCommandedToSleep(),
              "COMMANDED_TO_SLEEP should be cleared once sleeping starts");
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

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-flags", tickLimit = 100)
  public void commandToSleepSetsThenClearsOnArrival(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(absBedPos);
          husky.getSleepController().commandToSleep(absBedPos);
          context.assertTrue(
              husky.isCommandedToSleep(), "commandToSleep should set COMMANDED_TO_SLEEP");
          context.assertTrue(
              !husky.isSleepingInBed(),
              "Dog should not yet be sleeping immediately after commandToSleep");

          husky.getSleepController().startSleepingInBed(absBedPos);
          context.assertTrue(
              husky.isSleepingInBed(), "startSleepingInBed should set SLEEPING_IN_BED");
          context.assertTrue(
              !husky.isCommandedToSleep(),
              "startSleepingInBed should clear COMMANDED_TO_SLEEP on arrival");
          context.complete();
        });
  }

  private static void pinNight(final ServerWorld world) {
    world.setTimeOfDay(15000);
  }

  private static void pinDay(final ServerWorld world) {
    world.setTimeOfDay(1000);
  }
}
