package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogDeathWatch;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.gametest.util.SurvivalTestPlayer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class OwnerDeathWatchGameTest implements FabricGameTest {

  private static final String BATCH = "owner-death-watch";
  private static final String ARENA = "dogs-unleashed:teleport_arena";
  private static final int FLOOR_Y = 2;
  private static final BlockPos REL_DOG = new BlockPos(2, FLOOR_Y, 2);
  private static final BlockPos REL_SPOT = new BlockPos(4, FLOOR_Y, 2);
  private static final BlockPos REL_NEAR_THREAT = new BlockPos(10, FLOOR_Y, 2);
  private static final BlockPos REL_FAR_THREAT = new BlockPos(15, FLOOR_Y, 2);
  private static final BlockPos REL_OWNER_AWAY = new BlockPos(22, FLOOR_Y, 2);
  private static final BlockPos REL_OWNER_CLOSE = new BlockPos(7, FLOOR_Y, 2);
  private static final int OBSERVATION_TICK = 100;
  private static final int TICK_LIMIT = 120;

  private static DogDeathWatch watchOf(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogDeathWatch.class);
  }

  private static ServerPlayerEntity ownerAt(final TestContext context, final BlockPos rel) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final Vec3d pos = Vec3d.ofBottomCenter(context.getAbsolutePos(rel));
    owner.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0f, 0f);
    return owner;
  }

  private static UnleashedDogEntity dogWatchingSpot(
      final TestContext context, final ServerPlayerEntity owner, final DogCommand command) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, REL_DOG, owner.getUuid());
    dog.getCommandController().apply(command);
    watchOf(dog).keepWatchAt(context.getAbsolutePos(REL_SPOT), context.getWorld().getRegistryKey());
    return dog;
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void ownersDeathSendsFollowingDogsToKeepWatchAtTheSpot(final TestContext context) {
    final ServerPlayerEntity owner =
        SurvivalTestPlayer.joinAt(context, Vec3d.ofBottomCenter(REL_SPOT));
    try {
      final UnleashedDogEntity follower =
          DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, REL_DOG, owner.getUuid());
      follower.setAiDisabled(true);
      final BlockPos deathSpot = owner.getBlockPos();

      owner.kill();

      context.assertTrue(
          watchOf(follower).isKeepingWatch(),
          "A following dog should keep watch where its owner died");
      context.assertTrue(
          follower.getCommand() == DogCommand.GUARD,
          "A dog keeping watch should guard, was " + follower.getCommand());
      context.assertTrue(
          deathSpot.equals(follower.getCommandController().getAnchorPos()),
          "The dog should guard the death spot "
              + deathSpot
              + ", was "
              + follower.getCommandController().getAnchorPos());
    } finally {
      SurvivalTestPlayer.leave(context, owner);
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void ownersDeathLeavesStayingDogsAndOtherOwnersDogsAlone(final TestContext context) {
    final ServerPlayerEntity owner =
        SurvivalTestPlayer.joinAt(context, Vec3d.ofBottomCenter(REL_SPOT));
    try {
      final UnleashedDogEntity staying =
          DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, REL_DOG, owner.getUuid());
      staying.setAiDisabled(true);
      staying.getCommandController().apply(DogCommand.STAY);
      final ServerPlayerEntity stranger = ownerAt(context, REL_OWNER_AWAY);
      final UnleashedDogEntity strangersDog =
          DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, REL_DOG, stranger.getUuid());
      strangersDog.setAiDisabled(true);

      owner.kill();

      context.assertFalse(watchOf(staying).isKeepingWatch(), "A staying dog should stay put");
      context.assertTrue(staying.getCommand() == DogCommand.STAY, "A staying dog should keep STAY");
      context.assertFalse(
          watchOf(strangersDog).isKeepingWatch(), "Another owner's dog should not keep watch");
    } finally {
      SurvivalTestPlayer.leave(context, owner);
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void watchingDogTargetsHostileNearTheSpot(final TestContext context) {
    final UnleashedDogEntity dog =
        dogWatchingSpot(context, ownerAt(context, REL_OWNER_AWAY), DogCommand.FOLLOW);
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, REL_NEAR_THREAT);
    zombie.setAiDisabled(true);

    final LivingEntity[] firstTarget = new LivingEntity[1];
    context.runAtEveryTick(
        () -> {
          if (firstTarget[0] == null && dog.getTarget() != null) {
            firstTarget[0] = dog.getTarget();
          }
        });
    context.runAtTick(
        OBSERVATION_TICK,
        () -> {
          context.assertTrue(
              firstTarget[0] == zombie,
              "A dog keeping watch should target a zombie near the spot, target=" + firstTarget[0]);
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void watchingDogIgnoresHostileBeyondEightBlocksOfTheSpot(final TestContext context) {
    final UnleashedDogEntity dog =
        dogWatchingSpot(context, ownerAt(context, REL_OWNER_AWAY), DogCommand.FOLLOW);
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, REL_FAR_THREAT);
    zombie.setAiDisabled(true);

    final LivingEntity[] firstTarget = new LivingEntity[1];
    context.runAtEveryTick(
        () -> {
          if (firstTarget[0] == null && dog.getTarget() != null) {
            firstTarget[0] = dog.getTarget();
          }
        });
    context.runAtTick(
        OBSERVATION_TICK,
        () -> {
          context.assertTrue(
              firstTarget[0] == null,
              "A dog keeping watch should leave a zombie 11 blocks from the spot, target="
                  + firstTarget[0]);
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void watchingDogLiesDownAtTheSpot(final TestContext context) {
    final UnleashedDogEntity dog =
        dogWatchingSpot(context, ownerAt(context, REL_OWNER_AWAY), DogCommand.FOLLOW);
    final BlockPos spot = context.getAbsolutePos(REL_SPOT);

    context.runAtTick(
        OBSERVATION_TICK,
        () -> {
          context.assertTrue(
              dog.getBlockPos().getSquaredDistance(spot) <= 2.0,
              "The dog should go to the spot " + spot + ", was at " + dog.getBlockPos());
          context.assertTrue(dog.isInSittingPose(), "The dog should lie down at the spot");
          context.assertTrue(
              dog.getCommand() == DogCommand.GUARD, "The dog should still be keeping watch");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void ownerComingCloseEndsTheWatchAndRestoresHeel(final TestContext context) {
    final UnleashedDogEntity dog =
        dogWatchingSpot(context, ownerAt(context, REL_OWNER_CLOSE), DogCommand.HEEL);

    context.runAtTick(
        OBSERVATION_TICK,
        () -> {
          context.assertFalse(
              watchOf(dog).isKeepingWatch(), "The watch should end when the owner comes close");
          context.assertTrue(
              dog.getCommand() == DogCommand.HEEL,
              "The dog should go back to HEEL, was " + dog.getCommand());
          context.assertTrue(
              dog.getCommandController().getAnchorPos() == null,
              "The dog should no longer be anchored to the spot");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void anyCommandEndsTheWatch(final TestContext context) {
    final UnleashedDogEntity dog =
        dogWatchingSpot(context, ownerAt(context, REL_OWNER_AWAY), DogCommand.FOLLOW);
    dog.setAiDisabled(true);

    dog.getCommandController().apply(DogCommand.SIT);

    context.assertFalse(watchOf(dog).isKeepingWatch(), "A command should end the watch");
    context.assertTrue(dog.getCommand() == DogCommand.SIT, "The new command should apply");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void watchSurvivesAnNbtRoundTrip(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER_AWAY);
    final UnleashedDogEntity source = dogWatchingSpot(context, owner, DogCommand.HEEL);
    source.setAiDisabled(true);
    final NbtCompound nbt = new NbtCompound();
    source.writeCustomDataToNbt(nbt);

    final UnleashedDogEntity restored =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, REL_DOG, owner.getUuid());
    restored.setAiDisabled(true);
    restored.readCustomDataFromNbt(nbt);

    context.assertTrue(watchOf(restored).isKeepingWatch(), "The watch should survive a reload");
    context.assertTrue(
        context.getAbsolutePos(REL_SPOT).equals(restored.getCommandController().getAnchorPos()),
        "The watched spot should survive a reload");
    context.complete();
  }
}
