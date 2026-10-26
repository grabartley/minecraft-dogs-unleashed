package com.grahambartley.dogsunleashed.portrait;

import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.entity.DogAging;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;

public record DogLook(
    DogStatueLook statue, DogPortraitOutfit outfit, boolean undead, boolean senior) {

  public static DogLook of(final UnleashedDogEntity dog) {
    return new DogLook(
        DogStatueLook.of(dog),
        DogPortraitOutfits.of(dog),
        dog.isUndead(),
        dog.getComponents().get(DogAging.class).isSenior());
  }

  public static DogLook plain(final DogStatueLook statue) {
    return new DogLook(statue, DogPortraitOutfit.NONE, false, false);
  }
}
