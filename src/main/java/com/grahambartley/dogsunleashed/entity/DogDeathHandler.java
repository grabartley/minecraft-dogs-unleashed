package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.block.entity.DogBedBlockEntity;
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
      petData.syncAppearanceFrom(dog);
      petManager.updatePet(petData);
    }
    petManager.markPetDeceased(dog.getUuid(), dog.isUndead());

    final BlockPos bedPosToAvoid = dog.getAssignedBedPos().orElse(null);

    dog.getAssignedBedPos()
        .ifPresent(
            bedPos -> {
              if (world.getBlockEntity(bedPos) instanceof DogBedBlockEntity bedEntity) {
                bedEntity.clearAssignedDog(world);
              }
            });

    if (DogsUnleashed.SERVER_CONFIG.gravesEnabled()) {
      DogGraveSpawner.spawnGrave(world, dog, bedPosToAvoid);
    }
  }
}
