package com.grahambartley.dogsunleashed.mixin.client;

import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.whistle.WhistleBlowHold;
import com.grahambartley.dogsunleashed.whistle.WhistleBlowPoses;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.PlayerHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerHeldItemFeatureRenderer.class)
public abstract class PlayerHeldItemFeatureRendererMixin {

  @Shadow @Final private HeldItemRenderer playerHeldItemRenderer;

  @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
  private void dogsUnleashed$holdTheWhistleToTheMouth(
      final LivingEntity entity,
      final ItemStack stack,
      final ModelTransformationMode mode,
      final Arm arm,
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light,
      final CallbackInfo info) {
    final Hand hand = arm == entity.getMainArm() ? Hand.MAIN_HAND : Hand.OFF_HAND;
    if (!stack.isOf(ModItems.DOG_WHISTLE)
        || !WhistleBlowPoses.SHARED.isBlowing(entity.getId(), hand, entity.getWorld().getTime())
        || !(((FeatureRenderer<?, ?>) (Object) this).getContextModel()
            instanceof ModelWithHead model)) {
      return;
    }
    matrices.push();
    model.getHead().rotate(matrices);
    WhistleBlowHold.atMouth(matrices);
    this.playerHeldItemRenderer.renderItem(
        entity, stack, ModelTransformationMode.NONE, false, matrices, vertexConsumers, light);
    matrices.pop();
    info.cancel();
  }
}
