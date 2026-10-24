package com.grahambartley.dogsunleashed.portrait;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;

public enum PortraitBackground {
  SNOW("snow"),
  PLAINS("plains"),
  FOREST("forest"),
  BEACH("beach"),
  CHERRY_GROVE("cherry_grove"),
  PEAKS("peaks");

  private final String id;

  PortraitBackground(final String id) {
    this.id = id;
  }

  public String id() {
    return this.id;
  }

  public String texturePath(final PortraitSize size) {
    return "textures/portrait/background/" + this.id + "_" + size.serializedName() + ".png";
  }

  public static PortraitBackground of(final UnleashedDogBreed breed) {
    return switch (breed) {
      case HUSKY -> SNOW;
      case DACHSHUND -> PLAINS;
      case BEAGLE -> FOREST;
      case GOLDEN_RETRIEVER -> BEACH;
      case SHIBA_INU -> CHERRY_GROVE;
      case DIRE_WOLF -> PEAKS;
      case CROSS_BREED -> PLAINS;
    };
  }
}
