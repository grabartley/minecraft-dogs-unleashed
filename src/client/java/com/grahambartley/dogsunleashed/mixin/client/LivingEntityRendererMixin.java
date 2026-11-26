package com.grahambartley.dogsunleashed.mixin.client;

import com.grahambartley.dogsunleashed.gesture.PatArmPose;
import com.grahambartley.dogsunleashed.riding.SeatedRiders;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

  @Shadow protected M model;

  @Inject(
      method =
          "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/render/entity/model/EntityModel;setAngles(Lnet/minecraft/entity/Entity;FFFFF)V",
              shift = At.Shift.AFTER))
  private void dogsUnleashed$kneelToPat(
      final T entity,
      final float yaw,
      final float tickDelta,
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light,
      final CallbackInfo info) {
    if (entity instanceof PlayerEntity && this.model instanceof PlayerEntityModel<?> player) {
      PatArmPose.applyTo(player, entity, tickDelta);
    }
  }

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
