package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.HuskyEntity;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.listener.PetLocationSyncListener;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetManager;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * Enforces that pet records resync their dimension and position from the live entity on
 * load/unload, so stale records (which make dogs unfindable by summons and follows) self-heal on
 * contact with the entity.
 */
public final class PetLocationSyncListenerGameTest implements FabricGameTest {

  private static final BlockPos STALE_POSITION = new BlockPos(100_000, 64, 100_000);

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void recordLocationHealsStaleRecordFromLiveEntity(TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final HuskyEntity husky = spawnTamedDog(context, owner);
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
    final HuskyEntity husky = context.spawnEntity(ModEntities.HUSKY, new BlockPos(1, 2, 1));
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
    final HuskyEntity husky = spawnTamedDog(context, owner);
    final PetData petData = registerPet(context, owner, husky, false);

    PetLocationSyncListener.recordLocation(husky, context.getWorld());

    context.assertTrue(
        STALE_POSITION.equals(petData.getLastKnownPosition()),
        "Deceased records must keep their resting position");
    context.complete();
  }

  private static HuskyEntity spawnTamedDog(TestContext context, ServerPlayerEntity owner) {
    final HuskyEntity husky = context.spawnEntity(ModEntities.HUSKY, new BlockPos(1, 2, 1));
    husky.setAiDisabled(true);
    husky.setTamed(true, true);
    husky.setOwnerUuid(owner.getUuid());
    return husky;
  }

  private static PetData registerPet(
      TestContext context, ServerPlayerEntity owner, HuskyEntity husky, boolean alive) {
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
