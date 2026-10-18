package com.grahambartley.dogsunleashed.mixin.client;

import com.grahambartley.dogsunleashed.riding.SeatedRiders;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public abstract class CameraMixin {

  @WrapOperation(
      method = "update",
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V",
              ordinal = 0))
  private void dogsUnleashed$seeFromTheSeat(
      final Camera camera,
      final double x,
      final double y,
      final double z,
      final Operation<Void> original,
      final BlockView area,
      final Entity focusedEntity,
      final boolean thirdPerson,
      final boolean inverseView,
      final float tickDelta) {
    final Vec3d eye = SeatedRiders.eyeOf(focusedEntity, new Vec3d(x, y, z), tickDelta);
    original.call(camera, eye.x, eye.y, eye.z);
  }
}
