package com.grahambartley.dogsunleashed.mixin.client;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gesture.PatArmPose;
import com.grahambartley.dogsunleashed.riding.SeatedRiders;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> {

  @Shadow
  protected abstract ModelPart getArm(Arm arm);

  @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
  private void dogsUnleashed$leanIntoTheRide(
      final T entity,
      final float limbAngle,
      final float limbDistance,
      final float animationProgress,
      final float headYaw,
      final float headPitch,
      final CallbackInfo info) {
    if (SeatedRiders.isDrawnBySeat(entity)
        && entity.getVehicle() instanceof UnleashedDogEntity mount) {
      SeatedRiders.poseOf(
              mount, MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true))
          .applyTo(
              (BipedEntityModel<?>) (Object) this,
              ((BipedEntityModel<?>) (Object) this).handSwingProgress <= 0.0f
                  && !entity.isUsingItem());
    }
  }

  @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
  private void dogsUnleashed$reachOutToPat(
      final T entity,
      final float limbAngle,
      final float limbDistance,
      final float animationProgress,
      final float headYaw,
      final float headPitch,
      final CallbackInfo info) {
    if (entity instanceof PlayerEntity) {
      PatArmPose.applyTo(
          this.getArm(entity.getMainArm()),
          ((BipedEntityModel<?>) (Object) this).head,
          entity,
          MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true));
    }
  }
}
