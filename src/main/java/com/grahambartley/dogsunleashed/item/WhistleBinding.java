package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.pet.PetData;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public final class WhistleBinding {

  private WhistleBinding() {}

  public static @Nullable PetData boundPet(
      final List<PetData> ownerPets, final @Nullable UUID boundPetId) {
    if (boundPetId == null) {
      return null;
    }
    return ownerPets.stream()
        .filter(PetData::isAlive)
        .filter(pet -> boundPetId.equals(pet.getPetId()))
        .findFirst()
        .orElse(null);
  }
}
