package com.grahambartley.dogsunleashed.screen;

final class DogPreviewFraming {

  private static final float BOX_FILL = 0.84f;
  private static final float MODEL_DEPTH_BELOW_ORIGIN = 0.12f;

  private DogPreviewFraming() {}

  static int renderSize(final int boxSize, final float portraitHeight) {
    return Math.max(1, Math.round(boxSize * BOX_FILL / portraitHeight));
  }

  static float pivotOffset(final float portraitHeight, final float hitboxHeight) {
    return (portraitHeight - hitboxHeight) / 2.0f - MODEL_DEPTH_BELOW_ORIGIN;
  }
}
