package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

final class DogPreviewFraming {

  private static final float BOX_FILL = 0.84f;
  private static final float MODEL_DEPTH_BELOW_ORIGIN = 0.12f;

  private DogPreviewFraming() {}

  static void draw(
      final DrawContext context,
      final int left,
      final int top,
      final int boxSize,
      final float mouseX,
      final float mouseY,
      final UnleashedDogEntity dog) {
    final UnleashedDogBreed.RenderTransforms transforms =
        dog.getRigSourceBreed().renderTransforms();
    final float relativeSize =
        sizeRelativeToAdult(
            dog.getGrowth().applies(),
            dog.getGrowth().stage().renderScaleMultiplier(),
            dog.isBaby(),
            transforms.adultScale(),
            transforms.babyScale());
    final float framedHeight = transforms.portraitHeight() * relativeSize;
    InventoryScreen.drawEntity(
        context,
        left,
        top,
        left + boxSize,
        top + boxSize,
        renderSize(boxSize, framedHeight),
        pivotOffset(framedHeight, dog.getHeight(), relativeSize),
        mouseX,
        mouseY,
        dog);
  }

  static int renderSize(final int boxSize, final float portraitHeight) {
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

  static float pivotOffset(
      final float portraitHeight, final float hitboxHeight, final float sizeRelativeToAdult) {
    return (portraitHeight - hitboxHeight) / 2.0f - MODEL_DEPTH_BELOW_ORIGIN * sizeRelativeToAdult;
  }
}
