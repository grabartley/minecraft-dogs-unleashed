package com.grahambartley.dogsunleashed.pet;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfits;

public record PortraitLook(
    boolean baby,
    int coatVariant,
    int huskyEyeVariant,
    int furStage,
    int growthStage,
    DogPortraitOutfit outfit) {

  public static PortraitLook of(final UnleashedDogEntity dog) {
    return new PortraitLook(
        dog.isBaby(),
        PetData.coatVariantOf(dog),
        PetData.huskyEyeVariantOf(dog),
        dog.getFur().stage().ordinal(),
        dog.getGrowth().stage().number(),
        DogPortraitOutfits.of(dog));
  }
}
