package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed.RenderTransforms;
import org.jetbrains.annotations.Nullable;

public final class DogFrameScale {

  private DogFrameScale() {}

  public static float of(
      final RenderTransforms transforms,
      final boolean baby,
      final @Nullable Float growthStageMultiplier,
      final float slotHeightScale) {
    if (growthStageMultiplier != null) {
      return transforms.adultScale() * growthStageMultiplier * slotHeightScale;
    }
    final float ageScale = baby ? transforms.babyScale() : transforms.adultScale();
    return ageScale * slotHeightScale;
  }
}
