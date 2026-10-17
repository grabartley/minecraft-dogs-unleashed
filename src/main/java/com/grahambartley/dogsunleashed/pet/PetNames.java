package com.grahambartley.dogsunleashed.pet;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public final class PetNames {

  private PetNames() {}

  public static String tamedNameOf(final UnleashedDogEntity dog) {
    final PetData pet =
        dog.getWorld() instanceof ServerWorld serverWorld
            ? PetManager.get(serverWorld.getServer()).getPetByEntityId(dog.getUuid())
            : null;
    return nameOf(pet, dog.getBreed());
  }

  static String nameOf(final @Nullable PetData pet, final UnleashedDogBreed breed) {
    return pet != null ? pet.getName() : Text.translatable(breed.translationKey()).getString();
  }
}
