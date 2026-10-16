package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.SleepBatchFixture;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class DogSleepCommandWakeGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String ADULT_BATCH = "sleep-command-wake-adult";
  private static final String UNDEAD_BATCH = "sleep-command-wake-undead";
  private static final String PUPPY_BATCH = "sleep-command-wake-puppy";
  private static final int TICK_LIMIT = 200;
  private static final long ADULT_NIGHT = 15000;
  private static final long UNDEAD_MIDDAY = 6000;
  private static final long PUPPY_EVENING = 12000;

  @BeforeBatch(batchId = ADULT_BATCH)
  public void beforeAdultBatch(final ServerWorld world) {
    SleepBatchFixture.prepare(world);
  }

  @AfterBatch(batchId = ADULT_BATCH)
  public void afterAdultBatch(final ServerWorld world) {
    SleepBatchFixture.teardown(world);
  }

  @BeforeBatch(batchId = UNDEAD_BATCH)
  public void beforeUndeadBatch(final ServerWorld world) {
    SleepBatchFixture.prepare(world);
  }

  @AfterBatch(batchId = UNDEAD_BATCH)
  public void afterUndeadBatch(final ServerWorld world) {
    SleepBatchFixture.teardown(world);
  }

  @BeforeBatch(batchId = PUPPY_BATCH)
  public void beforePuppyBatch(final ServerWorld world) {
    SleepBatchFixture.prepare(world);
  }

  @AfterBatch(batchId = PUPPY_BATCH)
  public void afterPuppyBatch(final ServerWorld world) {
    SleepBatchFixture.teardown(world);
  }

  @GameTest(templateName = ARENA, batchId = ADULT_BATCH, tickLimit = TICK_LIMIT)
  public void commandWakeAtNightLetsAutoSleptDogLeaveItsBed(final TestContext context) {
    assertCommandWakeLetsDogLeaveItsBed(context, ModEntities.HUSKY, false, ADULT_NIGHT);
  }

  @GameTest(templateName = ARENA, batchId = UNDEAD_BATCH, tickLimit = TICK_LIMIT)
  public void commandWakeByDayLetsAutoSleptUndeadDogLeaveItsBed(final TestContext context) {
    assertCommandWakeLetsDogLeaveItsBed(context, ModEntities.ZOMBIE_HUSKY, false, UNDEAD_MIDDAY);
  }

  @GameTest(templateName = ARENA, batchId = PUPPY_BATCH, tickLimit = TICK_LIMIT)
  public void commandWakeInTheEveningLetsAutoSleptPuppyLeaveItsBed(final TestContext context) {
    assertCommandWakeLetsDogLeaveItsBed(context, ModEntities.HUSKY, true, PUPPY_EVENING);
  }

  private static void assertCommandWakeLetsDogLeaveItsBed(
      final TestContext context,
      final EntityType<? extends UnleashedDogEntity> type,
      final boolean baby,
      final long sleepTime) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final UnleashedDogEntity dog = context.spawnEntity(type, relBedPos);
    dog.setBaby(baby);
    dog.setTamed(true, true);
    dog.setInvulnerable(true);
    @SuppressWarnings("removal")
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    dog.setOwnerUuid(owner.getUuid());
    final BlockPos absOwnerPos = context.getAbsolutePos(new BlockPos(5, 2, 5));
    owner.refreshPositionAndAngles(
        absOwnerPos.getX() + 0.5, absOwnerPos.getY(), absOwnerPos.getZ() + 0.5, 0.0f, 0.0f);

    context.runAtTick(
        5,
        () -> {
          world.setTimeOfDay(sleepTime);
          dog.setAssignedBedPos(absBedPos);
        });

    context.runAtTick(
        60,
        () -> {
          context.assertTrue(dog.isSleepingInBed(), "Dog should auto-sleep in its bed");
          dog.getCommandController().apply(DogCommand.HEEL);
          context.assertFalse(dog.isSleepingInBed(), "The command should wake the dog");
          context.assertTrue(
              dog.getSleepController().isAutoSleepSuppressed(),
              "A command inside the dog's own sleep window should hold off auto-sleep");
        });

    context.runAtTick(
        160,
        () -> {
          final double distanceFromBed = Math.sqrt(dog.getBlockPos().getSquaredDistance(absBedPos));
          context.assertFalse(dog.isSleepingInBed(), "The woken dog must not go back to bed");
          context.assertTrue(
              distanceFromBed > 2.0,
              "A woken dog should follow its new command instead of staying on the bed, but it is "
                  + String.format("%.1f", distanceFromBed)
                  + " blocks from the bed");
          context.complete();
        });
  }
}
