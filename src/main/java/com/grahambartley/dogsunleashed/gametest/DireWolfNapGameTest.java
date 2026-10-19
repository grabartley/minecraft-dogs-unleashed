package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.SleepBatchFixture;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class DireWolfNapGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String WAKE_BATCH = "ground-nap-hostile";
  private static final String BREED_BATCH = "ground-nap-breed";
  private static final String COMMAND_BATCH = "ground-nap-command";
  private static final long LATE_AFTERNOON = 9000;
  private static final BlockPos REL_DOG = new BlockPos(3, 2, 3);
  private static final int SETTLE_TICKS = 40;

  @BeforeBatch(batchId = WAKE_BATCH)
  public void beforeHostileBatch(final ServerWorld world) {
    pinLateAfternoon(world);
  }

  @AfterBatch(batchId = WAKE_BATCH)
  public void afterHostileBatch(final ServerWorld world) {
    SleepBatchFixture.teardown(world);
  }

  @BeforeBatch(batchId = BREED_BATCH)
  public void beforeBreedBatch(final ServerWorld world) {
    pinLateAfternoon(world);
  }

  @AfterBatch(batchId = BREED_BATCH)
  public void afterBreedBatch(final ServerWorld world) {
    SleepBatchFixture.teardown(world);
  }

  @BeforeBatch(batchId = COMMAND_BATCH)
  public void beforeCommandBatch(final ServerWorld world) {
    pinLateAfternoon(world);
  }

  @AfterBatch(batchId = COMMAND_BATCH)
  public void afterCommandBatch(final ServerWorld world) {
    SleepBatchFixture.teardown(world);
  }

  @GameTest(templateName = ARENA, batchId = WAKE_BATCH, tickLimit = 100)
  public void anIdleTamedDireWolfNapsAndANearbyZombieWakesIt(final TestContext context) {
    final UnleashedDogEntity wolf = spawnStayingDog(context, ModEntities.DIRE_WOLF);

    context.runAtTick(
        SETTLE_TICKS,
        () -> {
          context.assertTrue(
              wolf.getSleepController().isNapping(), "An idle tamed dire wolf should nap");
          final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, REL_DOG.west(3));
          zombie.setAiDisabled(true);
        });
    context.runAtTick(
        SETTLE_TICKS + 20,
        () -> {
          context.assertFalse(
              wolf.getSleepController().isNapping(), "A zombie nearby should wake the dire wolf");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BREED_BATCH, tickLimit = 100)
  public void aDogThatSleepsInBedsNeverNapsOnTheGround(final TestContext context) {
    final UnleashedDogEntity husky = spawnStayingDog(context, ModEntities.HUSKY);

    context.runAtTick(
        SETTLE_TICKS,
        () -> {
          context.assertFalse(
              husky.getSleepController().isAsleep(), "A husky sleeps in its bed, not the ground");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = COMMAND_BATCH, tickLimit = 100)
  public void aCommandWakesANappingDireWolf(final TestContext context) {
    final UnleashedDogEntity wolf = spawnStayingDog(context, ModEntities.DIRE_WOLF);

    context.runAtTick(
        SETTLE_TICKS,
        () -> {
          context.assertTrue(
              wolf.getSleepController().isNapping(), "An idle tamed dire wolf should nap");
          wolf.getCommandController().apply(DogCommand.FOLLOW);
          context.assertFalse(
              wolf.getSleepController().isNapping(), "A command should wake the dire wolf");
        });
    context.runAtTick(
        SETTLE_TICKS + 20,
        () -> {
          context.assertFalse(
              wolf.getSleepController().isNapping(),
              "A dire wolf woken by a command should not nap again in the same part of the day");
          context.complete();
        });
  }

  private static void pinLateAfternoon(final ServerWorld world) {
    SleepBatchFixture.prepare(world);
    world.setTimeOfDay(LATE_AFTERNOON);
  }

  private static UnleashedDogEntity spawnStayingDog(
      final TestContext context, final EntityType<UnleashedDogEntity> type) {
    final UnleashedDogEntity dog = context.spawnEntity(type, REL_DOG);
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    dog.setTamed(true, true);
    dog.setOwnerUuid(owner.getUuid());
    dog.getCommandController().apply(DogCommand.STAY);
    return dog;
  }
}
