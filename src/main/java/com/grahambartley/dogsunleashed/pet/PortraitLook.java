package com.grahambartley.dogsunleashed.pet;

import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfits;

public record PortraitLook(DogStatueLook statue, DogPortraitOutfit outfit) {

  public static PortraitLook of(final UnleashedDogEntity dog) {
    return new PortraitLook(DogStatueLook.of(dog), DogPortraitOutfits.of(dog));
  }
}
