package com.grahambartley.dogsunleashed.mixin.client;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.render.bend.BendablePart;
import com.grahambartley.dogsunleashed.riding.SeatedRiders;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> {

  @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
  private void dogsUnleashed$straightenLimbs(
      final T entity,
      final float limbAngle,
      final float limbDistance,
      final float animationProgress,
      final float headYaw,
      final float headPitch,
      final CallbackInfo info) {
    final BipedEntityModel<?> model = (BipedEntityModel<?>) (Object) this;
    BendablePart.of(model.rightArm).dogsUnleashed$setBend(0.0f, 0.0f);
    BendablePart.of(model.leftArm).dogsUnleashed$setBend(0.0f, 0.0f);
    BendablePart.of(model.rightLeg).dogsUnleashed$setBend(0.0f, 0.0f);
    BendablePart.of(model.leftLeg).dogsUnleashed$setBend(0.0f, 0.0f);
  }

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
}
