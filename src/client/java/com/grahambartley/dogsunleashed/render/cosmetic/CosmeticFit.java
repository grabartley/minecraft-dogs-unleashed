package com.grahambartley.dogsunleashed.render.cosmetic;

import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;

public final class CosmeticFit {

  static final float CLEAR_MARGIN = 0.9f;

  private CosmeticFit() {}

  public static float hatWidth(
      final DogCosmetic cosmetic, final float headWidth, final float clearWidth) {
    return Math.min(headWidth * headShare(cosmetic), clearWidth * CLEAR_MARGIN);
  }

  public static float collarWidth(final DogCosmetic cosmetic, final float chestWidth) {
    return chestWidth * chestShare(cosmetic);
  }

  static float headShare(final DogCosmetic cosmetic) {
    return switch (cosmetic) {
      case STRAW_HAT -> 0.85f;
      case PARTY_HAT -> 0.5f;
      case BANDANA, COLLAR_BOW -> 0.0f;
    };
  }

  static float chestShare(final DogCosmetic cosmetic) {
    return switch (cosmetic) {
      case BANDANA -> 0.9f;
      case COLLAR_BOW -> 0.6f;
      case PARTY_HAT, STRAW_HAT -> 0.0f;
    };
  }
}
