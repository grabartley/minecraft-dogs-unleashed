package com.grahambartley.dogsunleashed.pet;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.util.DogNames;
import java.util.UUID;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

/**
 * Single entry point for creating the {@link PetData} record behind a tamed dog. Every path that
 * makes a dog tamed and owned (player taming, inherited-owner breeding, load-time backfill of dogs
 * tamed before this existed) goes through here so pet records stay consistent, and so a dog can
 * never end up tamed without a record.
 */
public final class PetRegistrar {

  private PetRegistrar() {}

  /**
   * Registers a pet record for {@code dog} owned by {@code ownerUuid}, returning the existing
   * record untouched when the dog already has one.
   */
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
            true);
    petData.syncAppearanceFrom(dog);
    petManager.registerPet(petData);
    return petData;
  }
}
