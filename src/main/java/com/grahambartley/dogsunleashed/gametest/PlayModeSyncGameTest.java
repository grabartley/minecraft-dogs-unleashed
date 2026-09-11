package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.DogPlaySession;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.fetch.FetchTypes;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public final class PlayModeSyncGameTest implements FabricGameTest {

  private static final String BATCH = "play-partner-sync";
  private static final BlockPos DOG_POS = new BlockPos(0, 1, 0);

  @BeforeBatch(batchId = BATCH)
  public void clearSessionsBefore(final ServerWorld world) {
    DogPlaySession.clearActivePlaySessions();
  }

  @AfterBatch(batchId = BATCH)
  public void clearSessionsAfter(final ServerWorld world) {
    DogPlaySession.clearActivePlaySessions();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void startPlayModeSetsSyncedPlayPartner(final TestContext context) {
    final PlayerEntity player = spawnPlayer(context);
    final UnleashedDogEntity dog = spawnDog(context);

    dog.getPlaySession().startPlayMode(player, FetchTypes.STICK);

    context.assertTrue(
        player.getUuid().equals(dog.getPlayPartnerPlayerUuid()),
        "startPlayMode should expose the partner UUID through synced tracked data");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void endPlayModeClearsSyncedPlayPartner(final TestContext context) {
    final PlayerEntity player = spawnPlayer(context);
    final UnleashedDogEntity dog = spawnDog(context);
    dog.getPlaySession().startPlayMode(player, FetchTypes.STICK);

    dog.getPlaySession().endPlayMode();

    context.assertTrue(
        dog.getPlayPartnerPlayerUuid() == null,
        "endPlayMode should clear the synced partner UUID so no stale prediction survives");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void nearbyGateMatchesPlayPartner(final TestContext context) {
    final PlayerEntity player = spawnPlayer(context);
    final UnleashedDogEntity dog = spawnDog(context);

    dog.getPlaySession().startPlayMode(player, FetchTypes.STICK);

    context.assertTrue(
        DogPlaySession.isAnyNearbyDogInPlayModeFor(player),
        "The nearby-dog gate should pass for the play-mode partner");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void nearbyGateRejectsNonPartnerPlayer(final TestContext context) {
    final PlayerEntity partner = spawnPlayer(context);
    final PlayerEntity bystander = spawnPlayer(context);
    final UnleashedDogEntity dog = spawnDog(context);

    dog.getPlaySession().startPlayMode(partner, FetchTypes.STICK);

    context.assertFalse(
        DogPlaySession.isAnyNearbyDogInPlayModeFor(bystander),
        "The nearby-dog gate should reject a player who is not the play-mode partner");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void nearbyGateFalseAfterPlayModeEnds(final TestContext context) {
    final PlayerEntity player = spawnPlayer(context);
    final UnleashedDogEntity dog = spawnDog(context);
    dog.getPlaySession().startPlayMode(player, FetchTypes.STICK);

    dog.getPlaySession().endPlayMode();

    context.assertFalse(
        DogPlaySession.isAnyNearbyDogInPlayModeFor(player),
        "The nearby-dog gate should stop passing once play mode ends");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void playModeTakeoverClearsPriorDogSyncedPartner(final TestContext context) {
    final PlayerEntity player = spawnPlayer(context);
    final UnleashedDogEntity firstDog = spawnDog(context);
    final UnleashedDogEntity secondDog = spawnDog(context);
    firstDog.getPlaySession().startPlayMode(player, FetchTypes.STICK);

    secondDog.getPlaySession().startPlayMode(player, FetchTypes.STICK);

    context.assertTrue(
        firstDog.getPlayPartnerPlayerUuid() == null,
        "Starting play mode with a second dog should clear the first dog's synced partner");
    context.assertTrue(
        player.getUuid().equals(secondDog.getPlayPartnerPlayerUuid()),
        "The second dog should hold the synced partner after takeover");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void dogRemovalClearsSyncedPlayPartner(final TestContext context) {
    final PlayerEntity player = spawnPlayer(context);
    final UnleashedDogEntity dog = spawnDog(context);
    dog.getPlaySession().startPlayMode(player, FetchTypes.STICK);

    dog.discard();

    context.assertTrue(
        dog.getPlayPartnerPlayerUuid() == null,
        "Discarding a play-mode dog should clear its synced partner via endPlayMode");
    context.complete();
  }

  private UnleashedDogEntity spawnDog(final TestContext context) {
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.HUSKY, DOG_POS);
    dog.setAiDisabled(true);
    return dog;
  }

  private PlayerEntity spawnPlayer(final TestContext context) {
    final PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
    final BlockPos abs = context.getAbsolutePos(DOG_POS);
    player.refreshPositionAndAngles(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5, 0f, 0f);
    return player;
  }
}
