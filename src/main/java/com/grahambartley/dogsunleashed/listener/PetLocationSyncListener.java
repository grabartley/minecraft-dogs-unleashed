package com.grahambartley.dogsunleashed.listener;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

/**
 * Keeps each pet's recorded dimension and position in sync with its entity whenever the entity
 * loads or unloads. Pet lookups chunk-load the recorded position, so a record that goes stale (e.g.
 * the dog's chunk unloaded after it was moved) would leave the dog unfindable by summons and
 * follows until something happens to load its real chunk again.
 */
public final class PetLocationSyncListener {

  private PetLocationSyncListener() {}

  public static void initialize() {
    ServerEntityEvents.ENTITY_LOAD.register(PetLocationSyncListener::recordLocation);
    ServerEntityEvents.ENTITY_UNLOAD.register(PetLocationSyncListener::recordLocation);
  }

  public static void recordLocation(Entity entity, ServerWorld world) {
    if (!(entity instanceof UnleashedDogEntity dog) || !dog.isTamed()) {
      return;
    }

    final PetManager petManager = PetManager.get(world.getServer());
    final PetData petData = petManager.getPetByEntityId(dog.getUuid());
    if (petData == null || !petData.isAlive()) {
      return;
    }

    petData.setDimension(world.getRegistryKey().getValue().toString());
    petData.setLastKnownPosition(dog.getBlockPos());
    petManager.updatePet(petData);
  }
}
