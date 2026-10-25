package com.grahambartley.dogsunleashed.mixin.client;

import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.whistle.WhistleBlowPoses;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

  @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
  private static void dogsUnleashed$raiseTheWhistle(
      final AbstractClientPlayerEntity player,
      final Hand hand,
      final CallbackInfoReturnable<BipedEntityModel.ArmPose> info) {
    if (player.getStackInHand(hand).isOf(ModItems.DOG_WHISTLE)
        && WhistleBlowPoses.SHARED.isBlowing(player.getId(), hand, player.getWorld().getTime())) {
      info.setReturnValue(BipedEntityModel.ArmPose.TOOT_HORN);
    }
  }
}
