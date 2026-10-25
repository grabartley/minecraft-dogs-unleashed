package com.grahambartley.dogsunleashed.mixin.client;

import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.whistle.WhistleBlowHold;
import com.grahambartley.dogsunleashed.whistle.WhistleBlowPoses;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

  @Inject(
      method = "renderFirstPersonItem",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
  private void dogsUnleashed$bringTheWhistleToTheMouth(
      final AbstractClientPlayerEntity player,
      final float tickDelta,
      final float pitch,
      final Hand hand,
      final float swingProgress,
      final ItemStack item,
      final float equipProgress,
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light,
      final CallbackInfo info) {
    if (!item.isOf(ModItems.DOG_WHISTLE)) {
      return;
    }
    final float lift =
        WhistleBlowPoses.SHARED.lift(player.getId(), hand, player.getWorld().getTime(), tickDelta);
    if (lift > 0.0f) {
      final Arm arm =
          hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
      WhistleBlowHold.apply(matrices, arm, lift);
    }
  }
}
