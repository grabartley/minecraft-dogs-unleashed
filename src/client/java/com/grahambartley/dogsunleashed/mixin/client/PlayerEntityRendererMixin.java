package com.grahambartley.dogsunleashed.mixin.client;

import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.gesture.PatBodyPose;
import com.grahambartley.dogsunleashed.gesture.PlayerGesture;
import com.grahambartley.dogsunleashed.gesture.PlayerGesturePoses;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

  @Inject(
      method =
          "getPositionOffset(Lnet/minecraft/client/network/AbstractClientPlayerEntity;F)Lnet/minecraft/util/math/Vec3d;",
      at = @At("RETURN"),
      cancellable = true)
  private void dogsUnleashed$kneelFromTheGround(
      final AbstractClientPlayerEntity player,
      final float tickDelta,
      final CallbackInfoReturnable<Vec3d> info) {
    final float pat = PatBodyPose.bodyWeight(player, tickDelta);
    if (pat > 0.0f) {
      info.setReturnValue(info.getReturnValue().multiply(1.0f - pat));
    }
  }

  @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
  private static void dogsUnleashed$raiseTheWhistle(
      final AbstractClientPlayerEntity player,
      final Hand hand,
      final CallbackInfoReturnable<BipedEntityModel.ArmPose> info) {
    if (player.getStackInHand(hand).isOf(ModItems.DOG_WHISTLE)
        && PlayerGesturePoses.SHARED.isPlaying(
            player.getId(), hand, PlayerGesture.WHISTLE, player.getWorld().getTime())) {
      info.setReturnValue(BipedEntityModel.ArmPose.TOOT_HORN);
    }
  }
}
