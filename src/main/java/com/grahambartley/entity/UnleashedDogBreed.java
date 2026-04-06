package com.grahambartley.entity;

import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public enum UnleashedDogBreed {
  HUSKY("husky", 0.08, -0.52, 0.55f),
  DACHSHUND("dachshund", -0.02, -0.34, 0.42f),
  BEAGLE("beagle", 0.03, -0.42, 0.48f),
  GOLDEN_RETRIEVER("goldenretriever", 0.08, -0.52, 0.55f),
  SHIBA_INU("shibainu", 0.03, -0.42, 0.48f);

  private final String serializedId;
  private final double carryBallVerticalOffset;
  private final double carryBallForwardOffset;
  private final float carryBallScale;

  UnleashedDogBreed(
      final String serializedId,
      final double carryBallVerticalOffset,
      final double carryBallForwardOffset,
      final float carryBallScale) {
    this.serializedId = serializedId;
    this.carryBallVerticalOffset = carryBallVerticalOffset;
    this.carryBallForwardOffset = carryBallForwardOffset;
    this.carryBallScale = carryBallScale;
  }

  public String serializedId() {
    return this.serializedId;
  }

  public String translationKey() {
    return "entity.dogs-unleashed." + this.serializedId;
  }

  public String animationPath() {
    return "animations/" + this.serializedId + ".animation.json";
  }

  public String defaultTexturePath() {
    return "textures/entity/" + this.serializedId + ".png";
  }

  public double carryBallVerticalOffset() {
    return this.carryBallVerticalOffset;
  }

  public double carryBallForwardOffset() {
    return this.carryBallForwardOffset;
  }

  public float carryBallScale() {
    return this.carryBallScale;
  }

  public static @Nullable UnleashedDogBreed fromSerializedIdOrNull(final @Nullable String id) {
    if (id == null || id.isEmpty()) {
      return null;
    }

    return switch (id.toLowerCase(Locale.ROOT)) {
      case "husky" -> HUSKY;
      case "dachshund" -> DACHSHUND;
      case "beagle" -> BEAGLE;
      case "goldenretriever", "golden_retriever" -> GOLDEN_RETRIEVER;
      case "shibainu", "shiba_inu" -> SHIBA_INU;
      default -> null;
    };
  }

  public static UnleashedDogBreed fromSerializedId(final String id) {
    final UnleashedDogBreed breed = fromSerializedIdOrNull(id);
    return breed == null ? HUSKY : breed;
  }
}
