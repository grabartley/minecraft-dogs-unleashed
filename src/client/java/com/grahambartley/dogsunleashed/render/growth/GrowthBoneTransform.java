package com.grahambartley.dogsunleashed.render.growth;

import java.util.Optional;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;

public final class GrowthBoneTransform {

  private static final float MODEL_UNITS_PER_BLOCK = 16.0f;

  private GrowthBoneTransform() {}

  public static boolean push(
      final MatrixStack poseStack, final @Nullable GrowthRigScale growth, final GeoBone bone) {
    if (growth == null) {
      return false;
    }
    final Optional<AnchoredPlacement.Shift> shift = growth.shiftOf(bone.getName());
    if (shift.isPresent()) {
      poseStack.push();
      scaleBetween(poseStack, shift.get().to(), shift.get().scale(), shift.get().from());
      return true;
    }
    final float scale = growth.localScale(bone.getName());
    if (scale == 1.0f) {
      return false;
    }
    poseStack.push();
    final Vector3f pivot = new Vector3f(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
    scaleBetween(poseStack, pivot, scale, pivot);
    return true;
  }

  private static void scaleBetween(
      final MatrixStack poseStack, final Vector3f to, final float scale, final Vector3f from) {
    poseStack.translate(
        to.x / MODEL_UNITS_PER_BLOCK, to.y / MODEL_UNITS_PER_BLOCK, to.z / MODEL_UNITS_PER_BLOCK);
    poseStack.scale(scale, scale, scale);
    poseStack.translate(
        -from.x / MODEL_UNITS_PER_BLOCK,
        -from.y / MODEL_UNITS_PER_BLOCK,
        -from.z / MODEL_UNITS_PER_BLOCK);
  }
}
