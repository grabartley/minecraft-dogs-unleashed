package com.grahambartley.gametest;

import com.grahambartley.entity.HuskyEntity;
import com.grahambartley.gametest.util.DogTestData;
import com.grahambartley.gametest.util.DogTestHelper;
import com.grahambartley.listener.PlayerJoinReunionListener;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * Covers the owner-reunion celebration: {@link
 * com.grahambartley.entity.UnleashedDogEntity#celebrateOwnerArrival()} and the {@link
 * PlayerJoinReunionListener} query that drives it on player join.
 */
public final class DogReunionGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";

  private void positionOwnerAt(
      final TestContext context, final ServerPlayerEntity owner, final BlockPos relativePos) {
    final BlockPos abs = context.getAbsolutePos(relativePos);
    owner.refreshPositionAndAngles(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5, 0f, 0f);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void celebrateOwnerArrivalStartsTailWag(TestContext context) {
    final HuskyEntity dog =
        DogTestHelper.spawnTamedDog(
            context, DogTestData.HUSKY, new BlockPos(1, 1, 0), UUID.randomUUID());
    dog.setAiDisabled(true);

    context.runAtTick(
        3,
        () -> {
          dog.celebrateOwnerArrival();
          context.assertTrue(
              dog.getTailWagTimerTicks() > 0,
              "celebrateOwnerArrival should start a tail wag, timer=" + dog.getTailWagTimerTicks());
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void celebrateOwnerArrivalRespectsCooldown(TestContext context) {
    final HuskyEntity dog =
        DogTestHelper.spawnTamedDog(
            context, DogTestData.HUSKY, new BlockPos(1, 1, 0), UUID.randomUUID());
    dog.setAiDisabled(true);

    context.runAtTick(3, dog::celebrateOwnerArrival);

    context.runAtTick(
        25,
        () -> {
          final int before = dog.getTailWagTimerTicks();
          dog.celebrateOwnerArrival();
          final int after = dog.getTailWagTimerTicks();

          context.assertTrue(
              before > 0, "First celebration should still be winding down, before=" + before);
          context.assertTrue(
              after == before,
              "Second celebration inside cooldown must not restart the tail wag, before="
                  + before
                  + " after="
                  + after);
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void reunionListenerWagsOwnedDogNearby(TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    positionOwnerAt(context, owner, new BlockPos(1, 1, 0));

    final HuskyEntity dog =
        DogTestHelper.spawnTamedDog(
            context, DogTestData.HUSKY, new BlockPos(2, 1, 0), owner.getUuid());
    dog.setAiDisabled(true);

    context.runAtTick(
        3,
        () -> {
          PlayerJoinReunionListener.celebrateDogsNear(owner);
          context.assertTrue(
              dog.getTailWagTimerTicks() > 0,
              "Listener should celebrate the joining player's own nearby dog");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void reunionListenerIgnoresUntamedDog(TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    positionOwnerAt(context, owner, new BlockPos(1, 1, 0));

    final HuskyEntity strayDog =
        DogTestHelper.spawnDog(context, DogTestData.HUSKY, new BlockPos(2, 1, 0));
    strayDog.setAiDisabled(true);

    context.runAtTick(
        3,
        () -> {
          PlayerJoinReunionListener.celebrateDogsNear(owner);
          context.assertTrue(
              strayDog.getTailWagTimerTicks() == 0,
              "Listener must not celebrate an untamed dog the player does not own");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20, maxAttempts = 3, requiredSuccesses = 1)
  public void reunionListenerIgnoresDogOwnedByAnotherPlayer(TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    positionOwnerAt(context, owner, new BlockPos(1, 1, 0));

    final HuskyEntity othersDog =
        DogTestHelper.spawnTamedDog(
            context, DogTestData.HUSKY, new BlockPos(2, 1, 0), UUID.randomUUID());
    othersDog.setAiDisabled(true);

    // maxAttempts guards the ~1/200-per-tick random idle wag a tamed dog can self-start (Rule 8);
    // the contract under test is that the listener itself never wags a non-owned dog.
    context.runAtTick(
        3,
        () -> {
          PlayerJoinReunionListener.celebrateDogsNear(owner);
          context.assertTrue(
              othersDog.getTailWagTimerTicks() == 0,
              "Listener must not celebrate a dog owned by a different player");
          context.complete();
        });
  }
}
