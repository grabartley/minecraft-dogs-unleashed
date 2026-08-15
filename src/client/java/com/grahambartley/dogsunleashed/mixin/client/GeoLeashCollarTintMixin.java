package com.grahambartley.dogsunleashed.mixin.client;

import com.grahambartley.dogsunleashed.render.LeashCollarTint;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Mixin(value = GeoEntityRenderer.class, remap = false)
public abstract class GeoLeashCollarTintMixin {

  @Inject(method = "renderLeash", at = @At("HEAD"))
  private void dogsUnleashed$captureCollarColor(
      final MobEntity mob,
      final float partialTick,
      final MatrixStack matrices,
      final VertexConsumerProvider bufferSource,
      final Entity leashHolder,
      final CallbackInfo ci) {
    LeashCollarTint.capture(mob);
  }

  @Inject(method = "renderLeash", at = @At("RETURN"))
  private void dogsUnleashed$clearCollarColor(
      final MobEntity mob,
      final float partialTick,
      final MatrixStack matrices,
      final VertexConsumerProvider bufferSource,
      final Entity leashHolder,
      final CallbackInfo ci) {
    LeashCollarTint.clear();
  }

  @ModifyConstant(method = "renderLeashPiece", constant = @Constant(floatValue = 0.5f))
  private static float dogsUnleashed$tintRed(final float original) {
    return LeashCollarTint.channel(original, LeashCollarTint.RED_SHIFT);
  }

  @ModifyConstant(method = "renderLeashPiece", constant = @Constant(floatValue = 0.4f))
  private static float dogsUnleashed$tintGreen(final float original) {
    return LeashCollarTint.channel(original, LeashCollarTint.GREEN_SHIFT);
  }

  @ModifyConstant(method = "renderLeashPiece", constant = @Constant(floatValue = 0.3f))
  private static float dogsUnleashed$tintBlue(final float original) {
    return LeashCollarTint.channel(original, LeashCollarTint.BLUE_SHIFT);
  }
}
