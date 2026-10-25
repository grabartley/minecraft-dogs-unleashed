package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.network.payload.PetSyncData;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfits;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

record PortraitDogAppearance(
    boolean undead, UUID entityId, DogStatueLook look, DogPortraitOutfit outfit) {

  static PortraitDogAppearance of(final PetSyncData pet) {
    return new PortraitDogAppearance(
        pet.undead(),
        UUID.fromString(pet.petId()),
        new DogStatueLook(
            pet.breed(),
            pet.baby(),
            pet.coatVariant(),
            pet.displayBreed().hasEyeColorVariants()
                ? pet.huskyEyeVariant()
                : UnleashedDogEntity.UNSET_VARIANT,
            pet.furStage(),
            pet.growthStage(),
            genomeOf(pet),
            null),
        pet.outfit());
  }

  UnleashedDogBreed entityBreed() {
    return this.look.breed();
  }

  void applyTo(final UnleashedDogEntity dog) {
    this.look.applyTo(dog);
    DogPortraitOutfits.dress(dog, this.outfit);
  }

  private static @Nullable DogGenome genomeOf(final PetSyncData pet) {
    if (pet.composition().isEmpty()) {
      return null;
    }
    return new DogGenome(
        pet.composition(),
        pet.maxHealth(),
        pet.movementSpeed(),
        pet.attackDamage(),
        DogGenome.BASELINE_FUR_GROWTH_RATE,
        DogGenome.dominantOf(pet.composition()));
  }
}
