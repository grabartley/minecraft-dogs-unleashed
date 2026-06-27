package com.grahambartley.gametest;

import static com.grahambartley.ModConstants.BARK_PITCH;
import static com.grahambartley.ModConstants.PUPPY_BARK_PITCH_MULTIPLIER;

import com.grahambartley.ModBlocks;
import com.grahambartley.ModEntities;
import com.grahambartley.block.DogBedBlock;
import com.grahambartley.entity.HuskyEntity;
import com.grahambartley.entity.UnleashedDogEntity;
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

/**
 * Behavioral coverage for the puppy differentiators added for issue #189: babies refuse combat
 * targets until they grow up, bark at a higher pitch, and auto-sleep on an assigned bed during the
 * day (where adults only sleep at night).
 *
 * <p>The day-sleep tests pin world time and therefore each batch freezes the daylight cycle so
 * {@code setTimeOfDay} actually holds (gametest skill rule 3). The flag-style tests disable AI so
 * the goal selector can't race the DataTracker assertions (rule 6). Both day-sleep tests pin the
 * SAME time value (day), so they can safely share a batch.
 */
public final class PuppyBehaviorGameTest implements FabricGameTest {

  private static final float PITCH_EPSILON = 0.001f;

  private static void prepareBatch(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, world.getServer());
    UnleashedDogEntity.clearActivePlaySessions();
    DogBedBlock.clearPendingAssignments();
  }

  private static void teardownBatch(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, world.getServer());
    UnleashedDogEntity.clearActivePlaySessions();
    DogBedBlock.clearPendingAssignments();
  }

  @BeforeBatch(batchId = "puppy-flags")
  public void beforeFlagsBatch(final ServerWorld world) {
    prepareBatch(world);
  }

  @AfterBatch(batchId = "puppy-flags")
  public void afterFlagsBatch(final ServerWorld world) {
    teardownBatch(world);
  }

  @BeforeBatch(batchId = "puppy-day-sleep")
  public void beforeDaySleepBatch(final ServerWorld world) {
    prepareBatch(world);
  }

  @AfterBatch(batchId = "puppy-day-sleep")
  public void afterDaySleepBatch(final ServerWorld world) {
    teardownBatch(world);
  }

  /**
   * A baby refuses any combat target, which keeps its attack goals inert. Once it grows up, the
   * same {@code setTarget} call sticks, proving adult combat AI is restored without any goal
   * re-registration.
   */
  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "puppy-flags", tickLimit = 40)
  public void babyRefusesTargetUntilGrownUp(final TestContext context) {
    final HuskyEntity dog =
        (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, new BlockPos(0, 1, 0));
    final HuskyEntity victim =
        (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, new BlockPos(2, 1, 0));
    dog.setAiDisabled(true);
    victim.setAiDisabled(true);

    context.runAtTick(
        10,
        () -> {
          dog.setBaby(true);
          context.assertTrue(dog.isBaby(), "Dog should be a baby after setBaby(true)");

          dog.setTarget(victim);
          context.assertTrue(dog.getTarget() == null, "Puppy must refuse a combat target");

          dog.setBreedingAge(0);
          context.assertTrue(!dog.isBaby(), "Dog should be an adult after growing up");

          dog.setTarget(victim);
          context.assertTrue(
              dog.getTarget() == victim, "Grown-up dog must accept a combat target again");
          context.complete();
        });
  }

  /** A baby barks at the boosted puppy pitch; an adult barks at the base pitch. */
  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "puppy-flags", tickLimit = 20)
  public void puppyBarkPitchIsHigherThanAdult(final TestContext context) {
    final HuskyEntity dog =
        (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, new BlockPos(0, 1, 0));
    dog.setAiDisabled(true);

    context.assertTrue(
        Math.abs(dog.getBarkPitch() - BARK_PITCH) < PITCH_EPSILON,
        "Adult bark pitch should be the base pitch, got " + dog.getBarkPitch());

    dog.setBaby(true);
    context.assertTrue(
        Math.abs(dog.getBarkPitch() - BARK_PITCH * PUPPY_BARK_PITCH_MULTIPLIER) < PITCH_EPSILON,
        "Puppy bark pitch should be the boosted pitch, got " + dog.getBarkPitch());
    context.complete();
  }

  /**
   * Being born arms the one-shot birth-wake heart burst, and the first {@code wakeUp} consumes it.
   * A second wake is a no-op, proving the flag is genuinely one-shot.
   */
  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "puppy-flags", tickLimit = 20)
  public void birthArmsHeartsAndFirstWakeConsumesThem(final TestContext context) {
    final HuskyEntity dog =
        (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, new BlockPos(0, 1, 0));
    dog.setAiDisabled(true);

    context.assertTrue(
        !dog.hasPendingBirthWakeHearts(), "A freshly spawned adult should not have hearts armed");

    dog.setBaby(true);
    context.assertTrue(dog.hasPendingBirthWakeHearts(), "Birth should arm the heart burst");

    dog.wakeUp();
    context.assertTrue(
        !dog.hasPendingBirthWakeHearts(), "First wake-up after birth should consume the hearts");

    dog.wakeUp();
    context.assertTrue(
        !dog.hasPendingBirthWakeHearts(), "Birth-wake hearts should be a one-shot, not re-armed");
    context.complete();
  }

  /**
   * The headline behavior: a puppy with an assigned bed auto-sleeps during the DAY. AutoSleepGoal
   * needs an indeterminate number of ticks to navigate and settle, so this polls for the sleep
   * transition rather than asserting at a fixed tick, and retries for goal-selector race safety
   * (gametest skill rules 8/9).
   */
  @GameTest(
      templateName = "dogs-unleashed:dog_arena",
      batchId = "puppy-day-sleep",
      tickLimit = 400,
      maxAttempts = 3,
      requiredSuccesses = 1)
  public void puppyAutoSleepsDuringDay(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity puppy = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relBedPos);
    puppy.setTamed(true, true);
    puppy.setBaby(true);
    puppy.setInvulnerable(true);
    // Ownerless tamed dogs get stuck in SitGoal (priority 2), which preempts AutoSleepGoal. Give an
    // owner so the goal hierarchy matches production. Gametest skill rule 6.
    @SuppressWarnings("removal")
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    puppy.setOwnerUuid(owner.getUuid());
    final AtomicBoolean armed = new AtomicBoolean(false);
    final AtomicBoolean hasSlept = new AtomicBoolean(false);

    context.runAtTick(
        10,
        () -> {
          world.setTimeOfDay(1000); // daytime, pinned by the frozen daylight cycle
          puppy.setAssignedBedPos(absBedPos);
          context.assertTrue(puppy.isBaby(), "Subject must be a puppy for this test");
          // Anchor the puppy on its bed with a clean transient state so AutoSleepGoal is the only
          // goal whose canStart can return true. This mirrors the deterministic setup the
          // night-sleep suite uses; without it the goal selector rarely converges in-test.
          puppy.refreshPositionAndAngles(
              absBedPos.getX() + 0.5, absBedPos.getY(), absBedPos.getZ() + 0.5, 0.0f, 0.0f);
          puppy.setVelocity(0, 0, 0);
          puppy.setSitting(false);
          puppy.setTarget(null);
          puppy.setAngerTime(0);
          armed.set(true);
        });

    context.runAtEveryTick(
        () -> {
          if (armed.get() && puppy.isSleepingInBed()) {
            hasSlept.set(true);
          }
        });

    context.runAtTick(
        399,
        () -> {
          context.assertTrue(
              hasSlept.get(),
              "Puppy with an assigned bed should auto-sleep during the day within the window");
          context.complete();
        });
  }

  /**
   * Control for {@link #puppyAutoSleepsDuringDay}: an adult with the same assigned bed during the
   * day must NOT auto-sleep, confirming day-sleeping is puppy-specific.
   */
  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "puppy-day-sleep", tickLimit = 200)
  public void adultDoesNotAutoSleepDuringDay(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity adult = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relBedPos);
    adult.setTamed(true, true);
    adult.setInvulnerable(true);
    @SuppressWarnings("removal")
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    adult.setOwnerUuid(owner.getUuid());
    final AtomicBoolean armed = new AtomicBoolean(false);

    context.runAtTick(
        10,
        () -> {
          world.setTimeOfDay(1000); // daytime
          adult.setAssignedBedPos(absBedPos);
          context.assertTrue(!adult.isBaby(), "Subject must be an adult for this test");
          armed.set(true);
        });

    context.runAtEveryTick(
        () -> {
          if (armed.get()) {
            context.assertTrue(
                !adult.isSleepingInBed(), "Adult dog must not auto-sleep during the day");
          }
        });

    context.runAtTick(199, context::complete);
  }
}
