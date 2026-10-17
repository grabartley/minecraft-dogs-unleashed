package com.grahambartley.dogsunleashed.pet;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;

public record PortraitLook(
    boolean baby, int collarColor, int coatVariant, int huskyEyeVariant, int furStage) {

  public static PortraitLook of(final UnleashedDogEntity dog) {
    return new PortraitLook(
        dog.isBaby(),
        dog.getCollarColor().getId(),
        PetData.coatVariantOf(dog),
        PetData.huskyEyeVariantOf(dog),
        dog.getFur().stage().ordinal());
  }
}
