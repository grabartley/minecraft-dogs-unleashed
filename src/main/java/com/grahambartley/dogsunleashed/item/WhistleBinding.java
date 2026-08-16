package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.pet.PetData;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves the dog a whistle is bound to, kept free of world and server types so the ownership and
 * liveness rules can be tested directly.
 */
public final class WhistleBinding {

  private WhistleBinding() {}

  /**
   * The pet the whistle answers to, or null when it is unbound, bound to a dog that has since died,
   * or bound to a dog that is no longer in this owner's pack. Binding to another player's dog is
   * refused at the point of binding; this is the second gate, for a whistle that changed hands.
   */
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
