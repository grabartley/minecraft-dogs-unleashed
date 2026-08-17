package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.listener.PetLocationSyncListener;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * Enforces that pet records resync their dimension and position from the live entity on
 * load/unload, so stale records (which make dogs unfindable by summons and follows) self-heal on
 * contact with the entity, and that a tamed, owned dog with no record at all gets one backfilled so
 * dogs ghosted by the old inherited-owner breeding path heal in existing worlds.
 */
public final class PetLocationSyncListenerGameTest implements FabricGameTest {

  private static final BlockPos STALE_POSITION = new BlockPos(100_000, 64, 100_000);

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void recordLocationHealsStaleRecordFromLiveEntity(TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final UnleashedDogEntity husky = spawnTamedDog(context, owner);
    final PetData petData = registerPet(context, owner, husky, true);

    PetLocationSyncListener.recordLocation(husky, context.getWorld());

    context.assertTrue(
        husky.getBlockPos().equals(petData.getLastKnownPosition()),
        "Record position should match the live entity, but was " + petData.getLastKnownPosition());
    context.assertTrue(
        context.getWorld().getRegistryKey().getValue().toString().equals(petData.getDimension()),
        "Record dimension should match the live entity, but was " + petData.getDimension());
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void recordLocationIgnoresUntamedDogs(TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, new BlockPos(1, 2, 1));
    husky.setAiDisabled(true);
    final PetData petData = registerPet(context, owner, husky, true);

    PetLocationSyncListener.recordLocation(husky, context.getWorld());

    context.assertTrue(
        STALE_POSITION.equals(petData.getLastKnownPosition()),
        "Untamed dogs must not touch pet records");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void recordLocationIgnoresDeceasedRecords(TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final UnleashedDogEntity husky = spawnTamedDog(context, owner);
    final PetData petData = registerPet(context, owner, husky, false);

    PetLocationSyncListener.recordLocation(husky, context.getWorld());

    context.assertTrue(
        STALE_POSITION.equals(petData.getLastKnownPosition()),
        "Deceased records must keep their resting position");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void recordLocationBackfillsMissingRecordForTamedDog(TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final UnleashedDogEntity husky = spawnTamedDog(context, owner);

    PetLocationSyncListener.recordLocation(husky, context.getWorld());

    final PetData petData =
        PetManager.get(context.getWorld().getServer()).getPetByEntityId(husky.getUuid());
    context.assertTrue(
        petData != null, "A tamed, owned dog with no record should be backfilled on load");
    context.assertTrue(
        owner.getUuid().equals(petData.getOwnerId()),
        "Backfilled record should belong to the dog's owner, but was " + petData.getOwnerId());
    context.assertTrue(
        husky.getBlockPos().equals(petData.getLastKnownPosition()),
        "Backfilled record should hold the live position, but was "
            + petData.getLastKnownPosition());
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void recordLocationDoesNotBackfillOwnerlessDogs(TestContext context) {
    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, new BlockPos(1, 2, 1));
    husky.setAiDisabled(true);
    husky.setTamed(true, true);

    PetLocationSyncListener.recordLocation(husky, context.getWorld());

    context.assertTrue(
        PetManager.get(context.getWorld().getServer()).getPetByEntityId(husky.getUuid()) == null,
        "An ownerless dog must never be registered as a pet");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void recordLocationDoesNotBackfillUntamedDogs(TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, new BlockPos(1, 2, 1));
    husky.setAiDisabled(true);
    husky.setOwnerUuid(owner.getUuid());

    PetLocationSyncListener.recordLocation(husky, context.getWorld());

    context.assertTrue(
        PetManager.get(context.getWorld().getServer()).getPetByEntityId(husky.getUuid()) == null,
        "An untamed dog must never be registered as a pet");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void recordLocationKeepsTheExistingRecordName(TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final UnleashedDogEntity husky = spawnTamedDog(context, owner);
    registerPet(context, owner, husky, true);

    PetLocationSyncListener.recordLocation(husky, context.getWorld());

    final PetData petData =
        PetManager.get(context.getWorld().getServer()).getPetByEntityId(husky.getUuid());
    context.assertTrue(
        "Scout".equals(petData.getName()),
        "Backfill must not overwrite an existing record, name was " + petData.getName());
    context.assertTrue(
        PetManager.get(context.getWorld().getServer()).getPetsByOwner(owner.getUuid()).size() == 1,
        "Backfill must not add a second record for an already-registered dog");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void recordLocationBackfillsParentsIntoExistingRecord(TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final UnleashedDogEntity husky = spawnTamedDog(context, owner);
    final UUID parentUuid = UUID.randomUUID();
    husky.getLineage().setParentDogUuid(parentUuid);
    final PetData petData = registerPet(context, owner, husky, true);
    context.assertTrue(
        petData.getParentAId() == null, "Precondition: legacy record starts without parents");

    PetLocationSyncListener.recordLocation(husky, context.getWorld());

    context.assertTrue(
        parentUuid.equals(petData.getParentAId()),
        "Legacy record should recover the entity's remembered parent, but was "
            + petData.getParentAId());
    context.complete();
  }

  private static UnleashedDogEntity spawnTamedDog(TestContext context, ServerPlayerEntity owner) {
    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, new BlockPos(1, 2, 1));
    husky.setAiDisabled(true);
    husky.setTamed(true, true);
    husky.setOwnerUuid(owner.getUuid());
    return husky;
  }

  private static PetData registerPet(
      TestContext context, ServerPlayerEntity owner, UnleashedDogEntity husky, boolean alive) {
    final ServerWorld world = context.getWorld();
    final PetData petData =
        new PetData(
            husky.getUuid(),
            owner.getUuid(),
            UnleashedDogBreed.HUSKY,
            "Scout",
            husky.getHealth(),
            husky.getMaxHealth(),
            STALE_POSITION,
            "minecraft:the_end",
            alive);
    PetManager.get(world.getServer()).registerPet(petData);
    return petData;
  }
}
