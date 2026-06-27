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
 * Behavioral coverage for the puppy differentiators: babies refuse combat targets until they grow
 * up, bark higher, follow the parent they were bred from, and sleep on a wider schedule than adults
 * (to bed two hours before night, awake one hour past sunrise) while staying up through the day.
 *
 * <p>Time-pinning tests freeze the daylight cycle so {@code setTimeOfDay} actually holds (gametest
 * skill rule 3), and each distinct pinned time gets its own batch so siblings don't race on the
 * shared world clock. Flag-style tests disable AI so the goal selector can't race the assertions
 * (rule 6). The follow test uses an untamed baby so neither {@code SitGoal} nor {@code
 * FollowOwnerGoal} can preempt the parent-follow goal under test.
 */
public final class PuppyBehaviorGameTest implements FabricGameTest {

  private static final float PITCH_EPSILON = 0.001f;

  private static final long TWO_HOURS_BEFORE_NIGHT_TICK = 11500;
  private static final long MIDDAY_TICK = 6000;
  private static final long JUST_PAST_SUNRISE_TICK = 23500;

  private static final double FOLLOW_REACHED_DISTANCE = 3.5;

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

  @BeforeBatch(batchId = "puppy-sleep-predusk")
  public void beforePreduskBatch(final ServerWorld world) {
    prepareBatch(world);
  }

  @AfterBatch(batchId = "puppy-sleep-predusk")
  public void afterPreduskBatch(final ServerWorld world) {
    teardownBatch(world);
  }

  @BeforeBatch(batchId = "puppy-awake-midday")
  public void beforeMiddayBatch(final ServerWorld world) {
    prepareBatch(world);
  }

  @AfterBatch(batchId = "puppy-awake-midday")
  public void afterMiddayBatch(final ServerWorld world) {
    teardownBatch(world);
  }

  @BeforeBatch(batchId = "puppy-sleep-postsunrise")
  public void beforePostSunriseBatch(final ServerWorld world) {
    prepareBatch(world);
  }

  @AfterBatch(batchId = "puppy-sleep-postsunrise")
  public void afterPostSunriseBatch(final ServerWorld world) {
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
   * The recorded parent resolves to the live entity while it is alive, and to {@code null} the
   * moment that parent is gone, which is exactly the "follow for as long as they are alive"
   * contract.
   */
  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "puppy-flags", tickLimit = 40)
  public void parentDogResolvesWhileAliveAndNullWhenGone(final TestContext context) {
    final HuskyEntity parent =
        (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, new BlockPos(2, 1, 0));
    final HuskyEntity puppy =
        (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, new BlockPos(0, 1, 0));
    parent.setAiDisabled(true);
    puppy.setAiDisabled(true);
    puppy.setBaby(true);

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              puppy.getParentDog() == null, "No recorded parent should resolve to null");

          puppy.setParentDogUuid(parent.getUuid());
          context.assertTrue(
              puppy.getParentDog() == parent, "Recorded parent should resolve while alive");

          parent.discard();
          context.assertTrue(
              puppy.getParentDog() == null, "A parent that is gone should resolve to null");
          context.complete();
        });
  }

  /**
   * A puppy paths toward the parent it was bred from instead of wandering off. The puppy is left
   * untamed so neither {@code SitGoal} nor {@code FollowOwnerGoal} can interfere; the parent is a
   * stationary, AI-disabled beacon. Navigation is inherently multi-tick, so this polls for arrival
   * and retries for race safety (gametest skill rules 8/9).
   */
  @GameTest(
      templateName = "dogs-unleashed:dog_arena",
      batchId = "puppy-flags",
      tickLimit = 300,
      maxAttempts = 3,
      requiredSuccesses = 1)
  public void puppyFollowsParent(final TestContext context) {
    final HuskyEntity parent =
        (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, new BlockPos(2, 1, 2));
    parent.setAiDisabled(true);
    parent.setInvulnerable(true);

    final HuskyEntity puppy =
        (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, new BlockPos(5, 1, 5));
    puppy.setBaby(true);
    puppy.setInvulnerable(true);
    puppy.setParentDogUuid(parent.getUuid());

    final AtomicBoolean reached = new AtomicBoolean(false);
    context.runAtEveryTick(
        () -> {
          if (puppy.squaredDistanceTo(parent)
              <= FOLLOW_REACHED_DISTANCE * FOLLOW_REACHED_DISTANCE) {
            reached.set(true);
          }
        });

    context.runAtTick(
        299,
        () -> {
          context.assertTrue(
              reached.get(), "Puppy should path to within follow distance of its parent");
          context.complete();
        });
  }

  /** Two hours before night a puppy turns in on its bed, while an adult is still up. */
  @GameTest(
      templateName = "dogs-unleashed:dog_arena",
      batchId = "puppy-sleep-predusk",
      tickLimit = 400,
      maxAttempts = 3,
      requiredSuccesses = 1)
  public void puppySleepsTwoHoursBeforeNight(final TestContext context) {
    assertAutoSleeps(context, TWO_HOURS_BEFORE_NIGHT_TICK, true);
  }

  @GameTest(
      templateName = "dogs-unleashed:dog_arena",
      batchId = "puppy-sleep-predusk",
      tickLimit = 200)
  public void adultStaysAwakeTwoHoursBeforeNight(final TestContext context) {
    assertStaysAwake(context, TWO_HOURS_BEFORE_NIGHT_TICK, false);
  }

  /** Through the day a puppy stays awake (so it can follow its parent), even on an assigned bed. */
  @GameTest(
      templateName = "dogs-unleashed:dog_arena",
      batchId = "puppy-awake-midday",
      tickLimit = 200)
  public void puppyStaysAwakeAtMidday(final TestContext context) {
    assertStaysAwake(context, MIDDAY_TICK, true);
  }

  /** A puppy sleeps in past sunrise, an hour after the adults have woken. */
  @GameTest(
      templateName = "dogs-unleashed:dog_arena",
      batchId = "puppy-sleep-postsunrise",
      tickLimit = 400,
      maxAttempts = 3,
      requiredSuccesses = 1)
  public void puppySleepsPastSunrise(final TestContext context) {
    assertAutoSleeps(context, JUST_PAST_SUNRISE_TICK, true);
  }

  /**
   * Drives a tamed dog (baby or adult) onto an assigned bed at a pinned time and asserts
   * AutoSleepGoal eventually puts it to sleep. The dog is anchored on its bed with clean transient
   * state so the goal selector converges deterministically, mirroring the night-sleep suite.
   */
  private void assertAutoSleeps(
      final TestContext context, final long pinnedTime, final boolean baby) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity dog = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relBedPos);
    dog.setTamed(true, true);
    if (baby) {
      dog.setBaby(true);
    }
    dog.setInvulnerable(true);
    // Ownerless tamed dogs get stuck in SitGoal (priority 2), which preempts AutoSleepGoal. Give an
    // owner so the goal hierarchy matches production. Gametest skill rule 6.
    @SuppressWarnings("removal")
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    dog.setOwnerUuid(owner.getUuid());
    final AtomicBoolean armed = new AtomicBoolean(false);
    final AtomicBoolean hasSlept = new AtomicBoolean(false);

    context.runAtTick(
        10,
        () -> {
          world.setTimeOfDay(pinnedTime);
          dog.setAssignedBedPos(absBedPos);
          context.assertTrue(dog.isBaby() == baby, "Subject age must match the test expectation");
          anchorOnBed(dog, absBedPos);
          armed.set(true);
        });

    context.runAtEveryTick(
        () -> {
          if (armed.get() && dog.isSleepingInBed()) {
            hasSlept.set(true);
          }
        });

    context.runAtTick(
        399,
        () -> {
          context.assertTrue(
              hasSlept.get(), "Dog should auto-sleep on its bed at time " + pinnedTime);
          context.complete();
        });
  }

  /**
   * Anchors a tamed dog on its assigned bed at a pinned time and asserts AutoSleepGoal never puts
   * it to sleep across the window (the dog is outside its sleep schedule).
   */
  private void assertStaysAwake(
      final TestContext context, final long pinnedTime, final boolean baby) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity dog = (HuskyEntity) context.spawnEntity(ModEntities.HUSKY, relBedPos);
    dog.setTamed(true, true);
    if (baby) {
      dog.setBaby(true);
    }
    dog.setInvulnerable(true);
    @SuppressWarnings("removal")
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    dog.setOwnerUuid(owner.getUuid());
    final AtomicBoolean armed = new AtomicBoolean(false);

    context.runAtTick(
        10,
        () -> {
          world.setTimeOfDay(pinnedTime);
          dog.setAssignedBedPos(absBedPos);
          anchorOnBed(dog, absBedPos);
          armed.set(true);
        });

    context.runAtEveryTick(
        () -> {
          if (armed.get()) {
            context.assertTrue(
                !dog.isSleepingInBed(), "Dog should stay awake on its bed at time " + pinnedTime);
          }
        });

    context.runAtTick(199, context::complete);
  }

  private static void anchorOnBed(final HuskyEntity dog, final BlockPos absBedPos) {
    dog.refreshPositionAndAngles(
        absBedPos.getX() + 0.5, absBedPos.getY(), absBedPos.getZ() + 0.5, 0.0f, 0.0f);
    dog.setVelocity(0, 0, 0);
    dog.setSitting(false);
    dog.setTarget(null);
    dog.setAngerTime(0);
  }
}
