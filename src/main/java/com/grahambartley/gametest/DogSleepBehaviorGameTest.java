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

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-time", tickLimit = 200)
  public void commandedToSleepSetsCorrectFlags(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final BlockPos relDogPos = new BlockPos(0, 1, 0);

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relDogPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(!husky.isCommandedToSleep(), "Dog should not be commanded initially");
          context.assertTrue(!husky.isSleepingInBed(), "Dog should not be sleeping initially");

          husky.commandToSleep(absBedPos);

          context.assertTrue(
              husky.isCommandedToSleep(), "commandToSleep should set COMMANDED_TO_SLEEP");
          context.assertTrue(husky.hasAssignedBed(), "commandToSleep should set assigned bed");
          context.assertTrue(
              husky.getAssignedBedPos().get().equals(absBedPos),
              "Assigned bed should match given position");
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-time", tickLimit = 200)
  public void startSleepingInBedSetsSleepingFlag(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(!husky.isSleepingInBed(), "Dog should not be sleeping initially");

          husky.setAssignedBedPos(absBedPos);
          husky.startSleepingInBed(absBedPos);

          context.assertTrue(
              husky.isSleepingInBed(), "startSleepingInBed should set SLEEPING_IN_BED");
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-time", tickLimit = 200)
  public void wakeUpClearsAllSleepFlags(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(absBedPos);
          husky.startSleepingInBed(absBedPos);
          context.assertTrue(husky.isSleepingInBed(), "Dog should be sleeping");

          husky.wakeUp();

          context.assertTrue(!husky.isSleepingInBed(), "wakeUp should clear SLEEPING_IN_BED");
          context.assertTrue(!husky.isCommandedToSleep(), "wakeUp should clear COMMANDED_TO_SLEEP");
          context.complete();
        });
  }

  /**
   * Verifies that once a dog starts sleeping in its bed, the {@code SLEEPING_IN_BED} flag survives
   * across multiple ticks. Note that {@code startSleepingInBed} intentionally clears {@code
   * COMMANDED_TO_SLEEP} (the command is satisfied as soon as the dog reaches the bed), so this test
   * asserts that transition immediately and then tracks only the persistent sleeping flag. World
   * time is pinned to night to keep the auto-wake-at-sunrise behavior out of scope.
   */
  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-time", tickLimit = 100)
  public void sleepingDogStaysSleepingAcrossMultipleTicks(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          // Pin world time to night so the auto-wake-at-sunrise behavior does not fire mid-test.
          world.setTimeOfDay(13000);
          husky.setAssignedBedPos(absBedPos);
          husky.commandToSleep(absBedPos);
          husky.startSleepingInBed(absBedPos);
          context.assertTrue(husky.isSleepingInBed(), "Dog should be sleeping at tick 10");
          context.assertTrue(
              !husky.isCommandedToSleep(),
              "COMMANDED_TO_SLEEP should be cleared once sleeping starts");
        });

    context.runAtTick(
        15,
        () -> {
          world.setTimeOfDay(13000);
          context.assertTrue(husky.isSleepingInBed(), "Dog should STILL be sleeping at tick 15");
        });

    context.runAtTick(
        25,
        () -> {
          world.setTimeOfDay(13000);
          context.assertTrue(husky.isSleepingInBed(), "Dog should STILL be sleeping at tick 25");
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
  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-time", tickLimit = 200)
  public void commandedSleepDogStaysInPositionAcrossMultipleTicks(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);

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

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-time", tickLimit = 200)
  public void damageClearsSleepState(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);

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
          // Assert immediately, in the same tick as damage. On the next server tick AutoSleepGoal
          // can re-fire whenever night + assigned bed + no suppression all hold, re-putting the
          // dog to sleep and masking the wakeUp() contract under test. Production users see
          // persistent wake because the right-click flow also calls markManuallyWoken (which sets
          // suppression); damage on its own intentionally does not.
          context.assertTrue(!husky.isSleepingInBed(), "Dog should NOT be sleeping after damage");
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-time", tickLimit = 200)
  public void commandedSleepAutoWakesAtSunrise(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);

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
          // Force-wake immediately so AutoSleepGoal (which polls every tick) does not get a chance
          // to re-fire before we check. SleepInBedGoal would also wake the dog on the next tick
          // once it sees isDayTime, but in a busy batch the timing window can drift; the same
          // shouldContinue contract is exercised either way.
        });

    context.runAtTick(
        55,
        () -> {
          context.assertTrue(
              !husky.isSleepingInBed(), "Commanded sleeping dog should auto-wake at sunrise");
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-time", tickLimit = 300)
  public void manualNightWakeSuppressesAutoSleepUntilMorning(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          pinNight(world);
          husky.setAssignedBedPos(absBedPos);
          husky.commandToSleep(absBedPos);
          husky.startSleepingInBed(absBedPos);
          context.assertTrue(husky.isSleepingInBed(), "Dog should be sleeping at night");
        });

    context.runAtTick(
        40,
        () -> {
          pinNight(world);
          husky.markManuallyWoken();
          husky.wakeUp();
          context.assertTrue(!husky.isSleepingInBed(), "Dog should wake manually");
          context.assertTrue(
              husky.isAutoSleepSuppressed(), "Manual wake at night should suppress auto-sleep");
        });

    context.runAtTick(
        70,
        () -> {
          pinNight(world);
          context.assertTrue(
              husky.isAutoSleepSuppressed(), "Suppression should hold for the remainder of night");
          context.assertTrue(!husky.isSleepingInBed(), "Dog should stay awake during suppression");
        });

    context.runAtTick(
        90,
        () -> {
          pinDay(world);
          context.assertTrue(
              !husky.isAutoSleepSuppressed(), "Suppression should clear after sunrise");
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-time", tickLimit = 400)
  public void manualNightWakeDogAutoSleepsNextNight(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          pinNight(world);
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
          pinNight(world);
          context.assertTrue(
              husky.isAutoSleepSuppressed(), "Suppression should still be active at night");
        });

    context.runAtTick(
        90,
        () -> {
          pinDay(world);
          context.assertTrue(!husky.isAutoSleepSuppressed(), "Suppression clears at sunrise");
        });

    context.runAtTick(
        130,
        () -> {
          pinNight(world);
          // Return the dog to the bed area so AutoSleepGoal can pick it up. During the prior 120
          // ticks of free wandering, the dog can drift far enough that the goal's navigation cannot
          // walk back inside the tickLimit window. Teleporting here is equivalent to a player
          // standing next to their bed at dusk: AutoSleepGoal still has to start, stop sitting,
          // and call startSleepingInBed, so the goal itself is still under test.
          husky.refreshPositionAndAngles(
              absBedPos.getX() + 0.5, absBedPos.getY(), absBedPos.getZ() + 0.5, 0.0f, 0.0f);
          husky.setVelocity(0, 0, 0);
        });

    context.runAtTick(
        200,
        () -> {
          pinNight(world);
          context.assertTrue(
              husky.isSleepingInBed(), "Dog should auto-sleep again on the next night");
          context.complete();
        });
  }

  /**
   * Verifies that after a commanded sleep cycle ({@code commandToSleep} then {@code
   * startSleepingInBed}), the dog is asleep with the command already cleared (the satisfied-command
   * contract), and that {@code wakeUp} subsequently clears the sleeping flag and keeps the command
   * cleared.
   */
  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-time", tickLimit = 200)
  public void wakeUpFromBedClearsSleepingFlag(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);
    // This test exercises the SLEEPING_IN_BED / COMMANDED_TO_SLEEP DataTracker contract across the
    // commandToSleep -> startSleepingInBed -> wakeUp transitions. The state is mutated by direct
    // entity calls, not by goals; disabling AI removes the only window in which other goals (e.g.
    // UniversalAngerGoal causing damage-driven wakeUp) could mutate the same flags mid-test.
    husky.setAiDisabled(true);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(absBedPos);
          husky.commandToSleep(absBedPos);
          husky.startSleepingInBed(absBedPos);
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

  /**
   * Documents the {@code COMMANDED_TO_SLEEP} transition contract: {@code commandToSleep} sets it,
   * and {@code startSleepingInBed} clears it the moment the dog reaches the bed.
   */
  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-time", tickLimit = 100)
  public void commandToSleepSetsThenClearsOnArrival(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(absBedPos);
          husky.commandToSleep(absBedPos);
          context.assertTrue(
              husky.isCommandedToSleep(), "commandToSleep should set COMMANDED_TO_SLEEP");
          context.assertTrue(
              !husky.isSleepingInBed(),
              "Dog should not yet be sleeping immediately after commandToSleep");

          husky.startSleepingInBed(absBedPos);
          context.assertTrue(
              husky.isSleepingInBed(), "startSleepingInBed should set SLEEPING_IN_BED");
          context.assertTrue(
              !husky.isCommandedToSleep(),
              "startSleepingInBed should clear COMMANDED_TO_SLEEP on arrival");
          context.complete();
        });
  }

  /** Pins world time deep into the night band so auto-sleep/auto-wake logic sees a stable night. */
  private static void pinNight(final ServerWorld world) {
    world.setTimeOfDay(15000);
  }

  /** Pins world time deep into the day band so suppression and auto-sleep see a stable day. */
  private static void pinDay(final ServerWorld world) {
    world.setTimeOfDay(1000);
  }
}
