package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.block.entity.AssignedDogHolder;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class DogDeathHandler {

  private DogDeathHandler() {}

  static void onTamedDogDeath(final UnleashedDogEntity dog, final ServerWorld world) {
    final PetManager petManager = PetManager.get(world.getServer());
    final PetData petData = petManager.getPetByEntityId(dog.getUuid());
    if (petData != null) {
      petData.syncFrom(dog);
      petManager.updatePet(petData);
    }
    petManager.markPetDeceased(dog.getUuid(), dog.isUndead());

    final BlockPos bedPosToAvoid = dog.getSleepController().getAssignedBedPos().orElse(null);

    dog.getSleepController()
        .getAssignedBedPos()
        .ifPresent(
            bedPos -> {
              if (world.getBlockEntity(bedPos) instanceof AssignedDogHolder holder
                  && dog.getUuid().equals(holder.getAssignedDogUuid())) {
                holder.clearAssignedDog(world);
              }
            });

    if (DogsUnleashed.SERVER_CONFIG.gameplay().gravesEnabled()) {
      DogGraveSpawner.spawnGrave(world, dog, bedPosToAvoid);
    }
  }
}
