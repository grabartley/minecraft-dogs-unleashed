package com.grahambartley.dogsunleashed.mixin.client;

import com.grahambartley.dogsunleashed.entity.WolfDogArmour;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.WolfArmorFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.WolfEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WolfArmorFeatureRenderer.class)
public abstract class WolfArmorFeatureRendererMixin {

  @Inject(
      method =
          "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/passive/WolfEntity;FFFFFF)V",
      at = @At("HEAD"),
      cancellable = true)
  private void dogsUnleashed$leaveDogArmourToItsOwnLayer(
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light,
      final WolfEntity wolf,
      final float limbAngle,
      final float limbDistance,
      final float tickDelta,
      final float animationProgress,
      final float headYaw,
      final float headPitch,
      final CallbackInfo ci) {
    if (WolfDogArmour.wears(wolf)) {
      ci.cancel();
    }
  }
}
