package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.network.handler.PetSyncRefresher;
import com.grahambartley.dogsunleashed.network.payload.PetSyncData;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public final class PetSyncRefresherGameTest implements FabricGameTest {

  private static final BlockPos FAR_AWAY = new BlockPos(1_200_000, 70, -1_200_000);
  private static final float SAVED_HEALTH = 7.0F;

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void refreshNeverLoadsTheChunkOfAnUnloadedDog(TestContext context) {
    final ServerWorld world = context.getWorld();
    final ChunkPos chunk = new ChunkPos(FAR_AWAY);
    context.assertFalse(
        world.isChunkLoaded(chunk.x, chunk.z), "Precondition: the far chunk starts unloaded");
    final PetData pet = registerUnloadedPet(context);

    PetSyncRefresher.refreshAndProject(world.getServer(), petManager(context), List.of(pet));

    context.assertFalse(
        world.isChunkLoaded(chunk.x, chunk.z),
        "Refreshing the Pet Manager must not load an unloaded dog's chunk");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void unloadedDogIsListedFromItsSavedData(TestContext context) {
    final PetData pet = registerUnloadedPet(context);

    final List<PetSyncData> synced =
        PetSyncRefresher.refreshAndProject(
            context.getWorld().getServer(), petManager(context), List.of(pet));

    final PetSyncData row = synced.get(0);
    context.assertTrue(
        new BlockPos(row.posX(), row.posY(), row.posZ()).equals(FAR_AWAY),
        "An unloaded dog should be listed at its last known place");
    context.assertTrue(
        row.health() == SAVED_HEALTH, "An unloaded dog should be listed with its saved health");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void loadedDogRefreshesItsRecordFromTheLiveEntity(TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, new BlockPos(1, 2, 1));
    husky.setAiDisabled(true);
    husky.setTamed(true, true);
    husky.setOwnerUuid(owner.getUuid());
    final PetData pet =
        register(context, husky.getUuid(), owner.getUuid(), FAR_AWAY, "minecraft:the_end");

    PetSyncRefresher.refreshAndProject(
        context.getWorld().getServer(), petManager(context), List.of(pet));

    context.assertTrue(
        husky.getBlockPos().equals(pet.getLastKnownPosition()),
        "A loaded dog's record should follow the live entity, but was "
            + pet.getLastKnownPosition());
    context.assertTrue(
        context.getWorld().getRegistryKey().getValue().toString().equals(pet.getDimension()),
        "A loaded dog's record should hold its live dimension, but was " + pet.getDimension());
    context.complete();
  }

  private static PetData registerUnloadedPet(TestContext context) {
    return register(
        context,
        UUID.randomUUID(),
        UUID.randomUUID(),
        FAR_AWAY,
        context.getWorld().getRegistryKey().getValue().toString());
  }

  private static PetData register(
      TestContext context, UUID petId, UUID ownerId, BlockPos lastPos, String dimension) {
    final PetData pet =
        new PetData(
            petId,
            ownerId,
            UnleashedDogBreed.HUSKY,
            "Scout",
            SAVED_HEALTH,
            20.0F,
            lastPos,
            dimension,
            PetLifeState.LIVING);
    petManager(context).registerPet(pet);
    return pet;
  }

  private static PetManager petManager(TestContext context) {
    return PetManager.get(context.getWorld().getServer());
  }
}
