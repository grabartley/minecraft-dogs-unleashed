package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.goal.DogSleepWindow;
import com.grahambartley.dogsunleashed.gametest.util.SleepBatchFixture;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class DogSleepBehaviorGameTest implements FabricGameTest {

  @BeforeBatch(batchId = "sleep-flags")
  public void beforeFlagsBatch(final ServerWorld world) {
    SleepBatchFixture.prepare(world);
    SleepBatchFixture.pinNight(world);
  }

  @AfterBatch(batchId = "sleep-flags")
  public void afterFlagsBatch(final ServerWorld world) {
    SleepBatchFixture.teardown(world);
  }

  @BeforeBatch(batchId = "sleep-stay-asleep")
  public void beforeStayAsleepBatch(final ServerWorld world) {
    SleepBatchFixture.prepare(world);
  }

  @AfterBatch(batchId = "sleep-stay-asleep")
  public void afterStayAsleepBatch(final ServerWorld world) {
    SleepBatchFixture.teardown(world);
  }

  @BeforeBatch(batchId = "sleep-wake-at-sunrise")
  public void beforeWakeAtSunriseBatch(final ServerWorld world) {
    SleepBatchFixture.prepare(world);
  }

  @AfterBatch(batchId = "sleep-wake-at-sunrise")
  public void afterWakeAtSunriseBatch(final ServerWorld world) {
    SleepBatchFixture.teardown(world);
  }

  @BeforeBatch(batchId = "sleep-suppress")
  public void beforeSuppressBatch(final ServerWorld world) {
    SleepBatchFixture.prepare(world);
  }

  @AfterBatch(batchId = "sleep-suppress")
  public void afterSuppressBatch(final ServerWorld world) {
    SleepBatchFixture.teardown(world);
  }

  @BeforeBatch(batchId = "sleep-refuse-daylight")
  public void beforeRefuseDaylightBatch(final ServerWorld world) {
    SleepBatchFixture.prepare(world);
  }

  @AfterBatch(batchId = "sleep-refuse-daylight")
  public void afterRefuseDaylightBatch(final ServerWorld world) {
    SleepBatchFixture.teardown(world);
  }

  @BeforeBatch(batchId = "sleep-resleep")
  public void beforeResleepBatch(final ServerWorld world) {
    SleepBatchFixture.prepare(world);
  }

  @AfterBatch(batchId = "sleep-resleep")
  public void afterResleepBatch(final ServerWorld world) {
    SleepBatchFixture.teardown(world);
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
          context.assertTrue(
              !husky.getSleepController().isCommandedToSleep(),
              "Dog should not be commanded initially");
          context.assertTrue(
              !husky.getSleepController().isSleepingInBed(),
              "Dog should not be sleeping initially");

          husky.getSleepController().commandToSleep(absBedPos);

          context.assertTrue(
              husky.getSleepController().isCommandedToSleep(),
              "commandToSleep should set COMMANDED_TO_SLEEP");
          context.assertTrue(
              husky.getSleepController().getAssignedBedPos().isPresent(),
              "commandToSleep should set assigned bed");
          context.assertTrue(
              husky.getSleepController().getAssignedBedPos().get().equals(absBedPos),
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
          context.assertTrue(
              !husky.getSleepController().isSleepingInBed(),
              "Dog should not be sleeping initially");

          husky.getSleepController().setAssignedBedPos(absBedPos);
          husky.getSleepController().startSleepingInBed(absBedPos);

          context.assertTrue(
              husky.getSleepController().isSleepingInBed(),
              "startSleepingInBed should set SLEEPING_IN_BED");
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
          husky.getSleepController().setAssignedBedPos(absBedPos);
          husky.getSleepController().startSleepingInBed(absBedPos);
          context.assertTrue(
              husky.getSleepController().isSleepingInBed(), "Dog should be sleeping");

          husky.wakeUp();

          context.assertTrue(
              !husky.getSleepController().isSleepingInBed(), "wakeUp should clear SLEEPING_IN_BED");
          context.assertTrue(
              !husky.getSleepController().isCommandedToSleep(),
              "wakeUp should clear COMMANDED_TO_SLEEP");
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
          husky.getSleepController().setAssignedBedPos(absBedPos);
          husky.getSleepController().commandToSleep(absBedPos);
          husky.getSleepController().startSleepingInBed(absBedPos);
          context.assertTrue(
              husky.getSleepController().isSleepingInBed(), "Dog should be sleeping at tick 10");
          context.assertTrue(
              !husky.getSleepController().isCommandedToSleep(),
              "COMMANDED_TO_SLEEP should be cleared once sleeping starts");
        });

    context.runAtTick(
        25,
        () ->
            context.assertTrue(
                husky.getSleepController().isSleepingInBed(),
                "Dog should STILL be sleeping at tick 25"));

    context.runAtTick(
        59,
        () -> {
          context.assertTrue(
              husky.getSleepController().isSleepingInBed(),
              "Dog should still be sleeping at the end of the window");
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
          husky.getSleepController().setAssignedBedPos(absBedPos);
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
          husky.getSleepController().setAssignedBedPos(absBedPos);
          husky.getSleepController().commandToSleep(absBedPos);
          husky.getSleepController().startSleepingInBed(absBedPos);
          context.assertTrue(
              husky.getSleepController().isSleepingInBed(), "Dog should be sleeping before damage");
        });

    context.runAtTick(
        50,
        () -> {
          husky.damage(world.getDamageSources().generic(), 0.5f);
          context.assertTrue(
              !husky.getSleepController().isSleepingInBed(),
              "Dog should NOT be sleeping after damage");
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
          husky.getSleepController().setAssignedBedPos(absBedPos);
          husky.getSleepController().commandToSleep(absBedPos);
          husky.getSleepController().startSleepingInBed(absBedPos);
          context.assertTrue(
              husky.getSleepController().isSleepingInBed(), "Dog should be sleeping");
          context.assertTrue(
              !husky.getSleepController().isCommandedToSleep(),
              "Dog should clear command once sleeping");
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
          if (!husky.getSleepController().isSleepingInBed()) {
            context.complete();
          }
        });

    context.runAtTick(
        199,
        () -> {
          context.assertTrue(
              !husky.getSleepController().isSleepingInBed(),
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
          SleepBatchFixture.pinNight(world);
          husky.getSleepController().setAssignedBedPos(absBedPos);
          husky.getSleepController().commandToSleep(absBedPos);
          husky.getSleepController().startSleepingInBed(absBedPos);
          context.assertTrue(
              husky.getSleepController().isSleepingInBed(), "Dog should be sleeping at night");
        });

    context.runAtTick(
        40,
        () -> {
          SleepBatchFixture.pinNight(world);
          husky.getSleepController().markManuallyWoken();
          husky.wakeUp();
          context.assertTrue(
              !husky.getSleepController().isSleepingInBed(), "Dog should wake manually");
          context.assertTrue(
              husky.getSleepController().isAutoSleepSuppressed(),
              "Manual wake at night should suppress auto-sleep");
        });

    context.runAtTick(
        70,
        () -> {
          SleepBatchFixture.pinNight(world);
          context.assertTrue(
              husky.getSleepController().isAutoSleepSuppressed(),
              "Suppression should hold for the remainder of night");
          context.assertTrue(
              !husky.getSleepController().isSleepingInBed(),
              "Dog should stay awake during suppression");
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

  @GameTest(
      templateName = "dogs-unleashed:dog_arena",
      batchId = "sleep-refuse-daylight",
      tickLimit = 100)
  public void goToBedInDaylightIsRefusedAndTheDogNeverLiesDown(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, new BlockPos(2, 1, 2));
    husky.setTamed(true, true);
    @SuppressWarnings("removal")
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    husky.setOwnerUuid(owner.getUuid());

    context.runAtTick(
        5,
        () -> {
          pinDay(world);
          husky.getSleepController().setAssignedBedPos(absBedPos);
          context.assertFalse(
              husky.getSleepController().commandToSleep(absBedPos),
              "Go to Bed must be refused outside the dog's sleep time");
          context.assertFalse(
              husky.getSleepController().isCommandedToSleep(),
              "A refused Go to Bed must not leave the dog heading to bed");
        });

    context.runAtTick(
        90,
        () -> {
          context.assertFalse(
              husky.getSleepController().isSleepingInBed(),
              "A dog told to sleep in daylight must never lie down");
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
          SleepBatchFixture.pinNight(world);
          husky.getSleepController().setAssignedBedPos(absBedPos);
          husky.getSleepController().commandToSleep(absBedPos);
          husky.getSleepController().startSleepingInBed(absBedPos);
          husky.getSleepController().markManuallyWoken();
          husky.wakeUp();
          context.assertTrue(
              !husky.getSleepController().isSleepingInBed(),
              "Dog should be awake after manual wake");
          husky.setAiDisabled(true);
        });

    context.runAtTick(
        50,
        () -> {
          SleepBatchFixture.pinNight(world);
          context.assertTrue(
              husky.getSleepController().isAutoSleepSuppressed(),
              "Suppression should still be active at night");
        });

    context.runAtTick(
        130,
        () -> {
          world.setTimeOfDay(DogSleepWindow.DAY_LENGTH_TICKS + SleepBatchFixture.NIGHT_TIME);
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
          if (husky.getSleepController().isSleepingInBed()) {
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
          husky.getSleepController().setAssignedBedPos(absBedPos);
          husky.getSleepController().commandToSleep(absBedPos);
          husky.getSleepController().startSleepingInBed(absBedPos);
          context.assertTrue(
              husky.getSleepController().isSleepingInBed(), "Dog should be sleeping");
          context.assertTrue(
              !husky.getSleepController().isCommandedToSleep(),
              "COMMANDED_TO_SLEEP should be cleared once sleeping starts");
        });

    context.runAtTick(
        50,
        () -> {
          context.assertTrue(
              husky.getSleepController().isSleepingInBed(), "Dog should still be sleeping");
          husky.wakeUp();
          context.assertTrue(
              !husky.getSleepController().isSleepingInBed(),
              "Dog should NOT be sleeping after wakeUp");
          context.assertTrue(
              !husky.getSleepController().isCommandedToSleep(),
              "Dog should NOT be commanded after wakeUp");
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
          husky.getSleepController().setAssignedBedPos(absBedPos);
          husky.getSleepController().commandToSleep(absBedPos);
          context.assertTrue(
              husky.getSleepController().isCommandedToSleep(),
              "commandToSleep should set COMMANDED_TO_SLEEP");
          context.assertTrue(
              !husky.getSleepController().isSleepingInBed(),
              "Dog should not yet be sleeping immediately after commandToSleep");

          husky.getSleepController().startSleepingInBed(absBedPos);
          context.assertTrue(
              husky.getSleepController().isSleepingInBed(),
              "startSleepingInBed should set SLEEPING_IN_BED");
          context.assertTrue(
              !husky.getSleepController().isCommandedToSleep(),
              "startSleepingInBed should clear COMMANDED_TO_SLEEP on arrival");
          context.complete();
        });
  }

  private static void pinDay(final ServerWorld world) {
    world.setTimeOfDay(1000);
  }
}
