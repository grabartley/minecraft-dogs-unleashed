package com.grahambartley.dogsunleashed.item.cosmetic;

import java.util.OptionalInt;

public enum DogCosmetic {
  BANDANA("bandana", Mount.COLLAR, OptionalInt.of(0xB02E26), true),
  COLLAR_BOW("collar_bow", Mount.COLLAR, OptionalInt.of(0xF38BAA), false),
  PARTY_HAT("party_hat", Mount.HEAD, OptionalInt.empty(), false),
  STRAW_HAT("straw_hat", Mount.HEAD, OptionalInt.empty(), false);

  public enum Mount {
    HEAD,
    COLLAR
  }

  private final String itemId;
  private final Mount mount;
  private final OptionalInt undyedColor;
  private final boolean takesBannerPatterns;

  DogCosmetic(
      final String itemId,
      final Mount mount,
      final OptionalInt undyedColor,
      final boolean takesBannerPatterns) {
    this.itemId = itemId;
    this.mount = mount;
    this.undyedColor = undyedColor;
    this.takesBannerPatterns = takesBannerPatterns;
  }

  public String itemId() {
    return this.itemId;
  }

  public Mount mount() {
    return this.mount;
  }

  public boolean isDyeable() {
    return this.undyedColor.isPresent();
  }

  public int undyedColor() {
    return this.undyedColor.orElse(0xFFFFFF);
  }

  public boolean takesBannerPatterns() {
    return this.takesBannerPatterns;
  }
}
