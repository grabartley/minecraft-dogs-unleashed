package com.grahambartley.dogsunleashed.mixin.client;

import com.grahambartley.dogsunleashed.render.LeashCollarTint;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class LeashCollarTintMixin {

  @Inject(method = "renderLeash", at = @At("HEAD"))
  private void dogsUnleashed$captureCollarColor(
      final Entity entity,
      final float tickDelta,
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final Entity holdingEntity,
      final CallbackInfo ci) {
    LeashCollarTint.capture(entity);
  }

  @Inject(method = "renderLeash", at = @At("RETURN"))
  private void dogsUnleashed$clearCollarColor(
      final Entity entity,
      final float tickDelta,
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final Entity holdingEntity,
      final CallbackInfo ci) {
    LeashCollarTint.clear();
  }

  @ModifyConstant(method = "renderLeashSegment", constant = @Constant(floatValue = 0.5f))
  private static float dogsUnleashed$tintRed(final float original) {
    return LeashCollarTint.channel(original, LeashCollarTint.RED_SHIFT);
  }

  @ModifyConstant(method = "renderLeashSegment", constant = @Constant(floatValue = 0.4f))
  private static float dogsUnleashed$tintGreen(final float original) {
    return LeashCollarTint.channel(original, LeashCollarTint.GREEN_SHIFT);
  }

  @ModifyConstant(method = "renderLeashSegment", constant = @Constant(floatValue = 0.3f))
  private static float dogsUnleashed$tintBlue(final float original) {
    return LeashCollarTint.channel(original, LeashCollarTint.BLUE_SHIFT);
  }
}
