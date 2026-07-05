package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
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

/**
 * Real-behavior coverage of the play-partner sync added for #238. On a dedicated server the
 * JVM-global {@code ACTIVE_PLAY_SESSIONS} map is invisible to the client, so {@code startPlayMode}
 * / {@code endPlayMode} now mirror the partner into synced {@code PLAY_PARTNER_UUID} tracked data
 * and the client-side stick-throw gate reads it via {@code isAnyNearbyDogInPlayModeFor}.
 *
 * <p>These tests assert the tracked-data contract through the same getters the client gate uses.
 * They need a live {@code ServerWorld}: entity construction and the JVM-global play-session map
 * both fail class init on the JUnit classpath, so this lives here rather than under {@code
 * src/test/java}.
 *
 * <p>The play-session map is JVM-global, so the batch resets it before and after to avoid leaking a
 * session into a sibling test (gametest skill rule 5). Assertions run in the same tick as the
 * mutation, so the void floor of {@code EMPTY_STRUCTURE} never comes into play.
 */
public final class PlayModeSyncGameTest implements FabricGameTest {

  private static final String BATCH = "play-partner-sync";
  private static final BlockPos DOG_POS = new BlockPos(0, 1, 0);

  @BeforeBatch(batchId = BATCH)
  public void clearSessionsBefore(final ServerWorld world) {
    UnleashedDogEntity.clearActivePlaySessions();
  }

  @AfterBatch(batchId = BATCH)
  public void clearSessionsAfter(final ServerWorld world) {
    UnleashedDogEntity.clearActivePlaySessions();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void startPlayModeSetsSyncedPlayPartner(final TestContext context) {
    final PlayerEntity player = spawnPlayer(context);
    final UnleashedDogEntity dog = spawnDog(context);

    dog.startPlayMode(player, FetchTypes.STICK);

    context.assertTrue(
        player.getUuid().equals(dog.getPlayPartnerPlayerUuid()),
        "startPlayMode should expose the partner UUID through synced tracked data");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void endPlayModeClearsSyncedPlayPartner(final TestContext context) {
    final PlayerEntity player = spawnPlayer(context);
    final UnleashedDogEntity dog = spawnDog(context);
    dog.startPlayMode(player, FetchTypes.STICK);

    dog.endPlayMode();

    context.assertTrue(
        dog.getPlayPartnerPlayerUuid() == null,
        "endPlayMode should clear the synced partner UUID so no stale prediction survives");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void nearbyGateMatchesPlayPartner(final TestContext context) {
    final PlayerEntity player = spawnPlayer(context);
    final UnleashedDogEntity dog = spawnDog(context);

    dog.startPlayMode(player, FetchTypes.STICK);

    context.assertTrue(
        UnleashedDogEntity.isAnyNearbyDogInPlayModeFor(player),
        "The nearby-dog gate should pass for the play-mode partner");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void nearbyGateRejectsNonPartnerPlayer(final TestContext context) {
    final PlayerEntity partner = spawnPlayer(context);
    final PlayerEntity bystander = spawnPlayer(context);
    final UnleashedDogEntity dog = spawnDog(context);

    dog.startPlayMode(partner, FetchTypes.STICK);

    context.assertFalse(
        UnleashedDogEntity.isAnyNearbyDogInPlayModeFor(bystander),
        "The nearby-dog gate should reject a player who is not the play-mode partner");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void nearbyGateFalseAfterPlayModeEnds(final TestContext context) {
    final PlayerEntity player = spawnPlayer(context);
    final UnleashedDogEntity dog = spawnDog(context);
    dog.startPlayMode(player, FetchTypes.STICK);

    dog.endPlayMode();

    context.assertFalse(
        UnleashedDogEntity.isAnyNearbyDogInPlayModeFor(player),
        "The nearby-dog gate should stop passing once play mode ends");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void playModeTakeoverClearsPriorDogSyncedPartner(final TestContext context) {
    final PlayerEntity player = spawnPlayer(context);
    final UnleashedDogEntity firstDog = spawnDog(context);
    final UnleashedDogEntity secondDog = spawnDog(context);
    firstDog.startPlayMode(player, FetchTypes.STICK);

    secondDog.startPlayMode(player, FetchTypes.STICK);

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
    dog.startPlayMode(player, FetchTypes.STICK);

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

  /**
   * {@code createMockPlayer} is fine here (gametest skill rule 4): the gate only reads the player's
   * UUID and bounding box, never server-connection state.
   */
  private PlayerEntity spawnPlayer(final TestContext context) {
    final PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
    final BlockPos abs = context.getAbsolutePos(DOG_POS);
    player.refreshPositionAndAngles(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5, 0f, 0f);
    return player;
  }
}
