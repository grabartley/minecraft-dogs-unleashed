package com.grahambartley.dogsunleashed.mixin.client;

import com.grahambartley.dogsunleashed.riding.SeatedRiders;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity> {

  @Inject(method = "setupTransforms", at = @At("HEAD"))
  private void dogsUnleashed$faceAlongTheSeat(
      final T entity,
      final MatrixStack matrices,
      final float animationProgress,
      final float bodyYaw,
      final float tickDelta,
      final float scale,
      final CallbackInfo info) {
    if (SeatedRiders.isDrawnBySeat(entity)) {
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(bodyYaw - 180.0f));
    }
  }
}
