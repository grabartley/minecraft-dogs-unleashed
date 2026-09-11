package com.grahambartley.dogsunleashed.pet;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.util.DogNames;
import java.util.UUID;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public final class PetRegistrar {

  private PetRegistrar() {}

  public static @Nullable PetData registerPetFor(
      final UnleashedDogEntity dog, final @Nullable UUID ownerUuid) {
    if (ownerUuid == null || !(dog.getWorld() instanceof ServerWorld serverWorld)) {
      return null;
    }

    final PetManager petManager = PetManager.get(serverWorld.getServer());
    final PetData existing = petManager.getPetByEntityId(dog.getUuid());
    if (existing != null) {
      return existing;
    }

    final PetData petData =
        new PetData(
            dog.getUuid(),
            ownerUuid,
            dog.getBreed(),
            DogNames.getRandomName(),
            dog.getHealth(),
            dog.getMaxHealth(),
            dog.getBlockPos(),
            serverWorld.getRegistryKey().getValue().toString(),
            PetLifeState.LIVING);
    petData.syncAppearanceFrom(dog);
    petData.recordParents(
        dog.getLineage().getParentDogUuid(), dog.getLineage().getSecondParentDogUuid());
    petManager.registerPet(petData);
    return petData;
  }
}
