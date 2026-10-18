package com.grahambartley.dogsunleashed.mixin.client;

import com.grahambartley.dogsunleashed.riding.SeatedRiders;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

  @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
  private void dogsUnleashed$leaveSeatedRidersToTheirMount(
      final Entity entity,
      final double cameraX,
      final double cameraY,
      final double cameraZ,
      final float tickDelta,
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final CallbackInfo info) {
    if (SeatedRiders.isSeated(entity)) {
      info.cancel();
      return;
    }
    SeatedRiders.beginWorldEntity(entity);
  }

  @Inject(method = "renderEntity", at = @At("RETURN"))
  private void dogsUnleashed$endWorldEntity(
      final Entity entity,
      final double cameraX,
      final double cameraY,
      final double cameraZ,
      final float tickDelta,
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final CallbackInfo info) {
    SeatedRiders.endWorldEntity();
  }
}
