package com.grahambartley.dogsunleashed.entity;

import java.util.Map;

public record DogHeadReach(float neck, float snout, float halfWidth, float top) {

  static final float UNITS_PER_BLOCK = 16.0f;

  private static final Map<UnleashedDogBreed, DogHeadReach> RIG_UNITS =
      Map.of(
          UnleashedDogBreed.BEAGLE, new DogHeadReach(3.0f, 6.3f, 1.1f, 7.0f),
          UnleashedDogBreed.DACHSHUND, new DogHeadReach(2.8f, 5.9f, 1.0f, 5.9f),
          UnleashedDogBreed.GOLDEN_RETRIEVER, new DogHeadReach(2.8f, 6.5f, 1.3f, 8.9f),
          UnleashedDogBreed.HUSKY, new DogHeadReach(4.55f, 10.85f, 2.7f, 15.39f),
          UnleashedDogBreed.SHIBA_INU, new DogHeadReach(3.0f, 5.9f, 2.2f, 6.8f),
          UnleashedDogBreed.DIRE_WOLF, new DogHeadReach(5.6f, 11.92f, 2.0f, 10.05f));

  public static DogHeadReach rigUnits(final UnleashedDogBreed rig) {
    final DogHeadReach reach = RIG_UNITS.get(rig);
    if (reach == null) {
      throw new IllegalArgumentException("No head reach for rig " + rig);
    }
    return reach;
  }

  public static DogHeadReach of(final UnleashedDogEntity dog) {
    final UnleashedDogBreed rig = dog.getRigSourceBreed();
    final float scale =
        DogFrameScale.of(
            rig.renderTransforms(),
            dog.isBaby(),
            dog.getGrowth().applies() ? dog.getGrowth().stage().renderScaleMultiplier() : null,
            1.0f);
    return rigUnits(rig).scaled(scale / UNITS_PER_BLOCK);
  }

  public DogHeadReach scaled(final float factor) {
    return new DogHeadReach(
        this.neck * factor, this.snout * factor, this.halfWidth * factor, this.top * factor);
  }
}
