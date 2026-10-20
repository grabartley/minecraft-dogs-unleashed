package com.grahambartley.dogsunleashed.screen;

public final class DogPreviewFraming {

  private static final float BOX_FILL = 0.84f;
  private static final float MODEL_DEPTH_BELOW_ORIGIN = 0.12f;

  private DogPreviewFraming() {}

  public static int renderSize(final int boxSize, final float portraitHeight) {
    return Math.max(1, Math.round(boxSize * BOX_FILL / portraitHeight));
  }

  static float sizeRelativeToAdult(
      final boolean growsThroughStages,
      final float stageScale,
      final boolean baby,
      final float adultScale,
      final float babyScale) {
    if (growsThroughStages) {
      return stageScale;
    }
    return baby ? babyScale / adultScale : 1.0f;
  }

  public static float pivotOffset(
      final float portraitHeight, final float hitboxHeight, final float sizeRelativeToAdult) {
    return (portraitHeight - hitboxHeight) / 2.0f - MODEL_DEPTH_BELOW_ORIGIN * sizeRelativeToAdult;
  }
}
