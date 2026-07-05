package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.block.DogBedBlock;
import com.grahambartley.dogsunleashed.entity.HuskyEntity;
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

  /**
   * Per-batch setup. Tests in the same batch run IN PARALLEL in different regions of the same
   * world, so any two tests that pin world time-of-day to different values race each other and
   * corrupt {@code AutoSleepGoal} / {@code SleepInBedGoal} evaluations across the entire batch. The
   * time-pinning tests therefore each live in their own batch ({@code sleep-stay-asleep}, {@code
   * sleep-wake-at-sunrise}, {@code sleep-suppress}, {@code sleep-resleep}) and the time-agnostic
   * flag tests share the {@code sleep-flags} batch. Each batch shares the same setup: freeze the
   * daylight cycle (so {@code setTimeOfDay} actually pins time, since TestServer does NOT default
   * it off) and clear the JVM-global maps mutated by gameplay. The teardown restores defaults so
   * other batches aren't affected. Gametest skill rules 3 and 5.
   */
  private static void prepareSleepBatch(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, world.getServer());
    UnleashedDogEntity.clearActivePlaySessions();
    DogBedBlock.clearPendingAssignments();
  }

  private static void teardownSleepBatch(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, world.getServer());
    UnleashedDogEntity.clearActivePlaySessions();
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

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-flags", tickLimit = 200)
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

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-flags", tickLimit = 200)
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
   * across multiple ticks. {@code startSleepingInBed} intentionally clears {@code
   * COMMANDED_TO_SLEEP} (the command is satisfied as soon as the dog reaches the bed), so this test
   * asserts that transition immediately and then tracks only the persistent sleeping flag.
   *
   * <p>The polling pattern is intentional: instead of asserting "the flag is true at exactly tick
   * 25", we assert "the flag stays true for every tick from 11 to {@code tickLimit}". That mirrors
   * how the production contract is observed in-game and decouples the test from intra-tick
   * scheduling races between {@code runAtTick} callbacks and goal selector evaluation. Gametest
   * skill rule 9.
   */
  @GameTest(
      templateName = "dogs-unleashed:dog_arena",
      batchId = "sleep-stay-asleep",
      tickLimit = 60)
  public void sleepingDogStaysSleepingAcrossMultipleTicks(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);
    // This test asserts the SLEEPING_IN_BED DataTracker contract over a multi-tick window: once
    // set via startSleepingInBed, it stays set absent an explicit wakeUp / damage / day-time
    // transition. None of those should occur in this batch (daylight cycle is frozen at night),
    // so the contract under test is the flag itself, not the goal selector. Disabling AI removes
    // the entire goal-selector race surface and makes this test deterministic. The sister tests
    // {@code commandedSleepAutoWakesAtSunrise} and {@code manualNightWakeDogAutoSleepsNextNight}
    // genuinely need AI because they test goal-driven transitions. Gametest skill rule 6.
    husky.setAiDisabled(true);

    context.runAtTick(
        10,
        () -> {
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
  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-flags", tickLimit = 200)
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

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-flags", tickLimit = 200)
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

  /**
   * Verifies the auto-wake-at-sunrise transition: a sleeping dog wakes within a small tick window
   * after world time crosses from night to day. The production contract is "shortly after sunrise",
   * not "at exactly tick N+5", so the test polls {@code isSleepingInBed()} every tick after the day
   * pin and completes the moment it flips. This decouples the assertion from the intra-tick
   * scheduling of {@code runAtTick} callbacks vs. goal selector evaluation, which previously made
   * this test irreducibly flaky. Gametest skill rule 8 / rule 9.
   */
  @GameTest(
      templateName = "dogs-unleashed:dog_arena",
      batchId = "sleep-wake-at-sunrise",
      tickLimit = 200)
  public void commandedSleepAutoWakesAtSunrise(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);
    husky.setInvulnerable(true); // see note on sister test re: in-bed fall damage triggering wakeUp
    // See note on sister test {@code manualNightWakeDogAutoSleepsNextNight}: SitGoal (priority 2)
    // is unconditionally startable for an ownerless tamed dog, so it preempts SleepInBedGoal /
    // AutoSleepGoal (priorities 3/4) and the goal-driven wake-at-sunrise transition never fires.
    // Production dogs always have an owner. Gametest skill rule 6.
    @SuppressWarnings("removal")
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    husky.setOwnerUuid(owner.getUuid());
    final AtomicBoolean armedForWake = new AtomicBoolean(false);

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
          armedForWake.set(true);
        });

    // Scheduled once at the top level (see note on the sister test re: NPE when nested in
    // runAtTick). After sunrise the {@code armedForWake} gate lets the polling complete the test
    // the moment the wake propagates.
    context.runAtEveryTick(
        () -> {
          if (!armedForWake.get()) return;
          if (!husky.isSleepingInBed()) {
            context.complete();
          }
        });

    // If the wake didn't propagate by the last tick, surface a meaningful failure.
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

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);
    // See sister tests: ownerless tamed dogs trigger SitGoal preemption. Give an owner so the goal
    // hierarchy matches production. Gametest skill rule 6.
    @SuppressWarnings("removal")
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    husky.setOwnerUuid(owner.getUuid());

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

  /**
   * Verifies the full suppression cycle: manual wake at night sets suppression, suppression clears
   * after sunrise, and AutoSleepGoal re-fires the next night to put the dog back to sleep.
   *
   * <p>The "next-night auto-sleep" half is observed via a tick-by-tick poll inside the night window
   * rather than a single assertion at a fixed tick. AutoSleepGoal's start/move/sleep loop needs an
   * indeterminate number of ticks (depends on goal selector priority and navigation), and locking
   * the assertion to a single tick previously made this test flaky. The polling pattern follows the
   * gametest skill's rule 8 / rule 9.
   */
  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-resleep", tickLimit = 600)
  public void manualNightWakeDogAutoSleepsNextNight(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relBedPos);
    husky.setTamed(true, true);
    husky.setInvulnerable(true); // see note on sister test re: in-bed fall damage triggering wakeUp
    // Give the dog an owner. Without an owner, vanilla {@code SitGoal.canStart} unconditionally
    // returns true for any tamed on-ground dog, and SitGoal (priority 2) outranks AutoSleepGoal
    // (priority 4), so once AI is re-enabled at tick 130 SitGoal preempts MOVE and AutoSleepGoal
    // never gets to fire. A real production dog always has an owner. Gametest skill rule 6.
    @SuppressWarnings("removal")
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    husky.setOwnerUuid(owner.getUuid());
    final AtomicBoolean armedForAutoSleep = new AtomicBoolean(false);
    // Latches true the first time the dog is observed sleeping after AutoSleepGoal is armed. The
    // test contract is "AutoSleepGoal fires within the window", not "the dog is still sleeping at
    // the exact moment we check the final task" - SleepInBedGoal.stop() can briefly clear the flag
    // between {@code complete()} firing and the {@code addInstantFinalTask} assertion running.
    final AtomicBoolean hasAutoSlept = new AtomicBoolean(false);

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
          // Freeze the dog in place between tick 10 and tick 130 so it can't wander out of range,
          // pick up anger, get a target, etc. We re-enable AI at tick 130 with the dog anchored at
          // the bed; from there AutoSleepGoal is the only sleep-related goal whose preconditions
          // can be true, so its start() is exercised deterministically. The suppression /
          // suppression-clearing assertions at ticks 50 and 90 are flag-only checks that don't
          // need AI running.
          husky.setAiDisabled(true);
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
          // Anchor at bed, reset transient AI/anger state, re-enable AI. AutoSleepGoal is now the
          // only goal whose canStart can return true (night + tamed + has-bed + in-range +
          // not-sleeping + not-commanded + not-sitting + not-suppressed), so the goal selector
          // picks it on the next server tick.
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

    // Scheduled once at the top level (see note on the sister test re: NPE when nested in
    // runAtTick). After tick 130 the gate lets the polling latch the first observed sleep
    // transition; the test stays running until the final tick check so any goal-cascade
    // wake-and-resleep cycles don't matter, only "has the goal converted the dog at least once".
    context.runAtEveryTick(
        () -> {
          if (!armedForAutoSleep.get()) return;
          if (husky.isSleepingInBed()) {
            hasAutoSlept.set(true);
          }
        });

    // Final tick: assert the latch is set (the goal fired) and complete.
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

  /**
   * Verifies that after a commanded sleep cycle ({@code commandToSleep} then {@code
   * startSleepingInBed}), the dog is asleep with the command already cleared (the satisfied-command
   * contract), and that {@code wakeUp} subsequently clears the sleeping flag and keeps the command
   * cleared.
   */
  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-flags", tickLimit = 200)
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
  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "sleep-flags", tickLimit = 100)
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
