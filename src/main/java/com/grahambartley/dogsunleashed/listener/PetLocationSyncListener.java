package com.grahambartley.dogsunleashed.listener;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetManager;
import com.grahambartley.dogsunleashed.pet.PetRegistrar;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

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
    PetData petData = petManager.getPetByEntityId(dog.getUuid());
    if (petData == null) {
      petData = PetRegistrar.registerPetFor(dog, dog.getOwnerUuid());
    }
    if (petData == null) {
      return;
    }

    final boolean parentsBackfilled =
        petData.recordParents(
            dog.getLineage().getParentDogUuid(), dog.getLineage().getSecondParentDogUuid());
    if (!petData.isAlive()) {
      if (parentsBackfilled) {
        petManager.updatePet(petData);
      }
      return;
    }

    petData.setDimension(world.getRegistryKey().getValue().toString());
    petData.setLastKnownPosition(dog.getBlockPos());
    petManager.updatePet(petData);
  }
}
