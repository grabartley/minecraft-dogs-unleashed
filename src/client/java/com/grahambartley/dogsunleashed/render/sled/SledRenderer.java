package com.grahambartley.dogsunleashed.render.sled;

import com.grahambartley.dogsunleashed.entity.sled.SledEntity;
import com.grahambartley.dogsunleashed.model.SledModel;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SledRenderer extends GeoEntityRenderer<SledEntity> {

  static final float WOBBLE_DEGREES = 4.0f;

  public SledRenderer(final EntityRendererFactory.Context context) {
    super(context, new SledModel<>());
    this.shadowRadius = 0.8f;
  }

  @Override
  protected void applyRotations(
      final SledEntity sled,
      final MatrixStack poseStack,
      final float ageInTicks,
      final float rotationYaw,
      final float partialTick,
      final float nativeScale) {
    poseStack.multiply(
        RotationAxis.POSITIVE_Y.rotationDegrees(
            180.0f - MathHelper.lerpAngleDegrees(partialTick, sled.prevYaw, sled.getYaw())));
    final float wobble = sled.wobbleTicks() - partialTick;
    if (wobble > 0.0f) {
      poseStack.multiply(
          RotationAxis.POSITIVE_Z.rotationDegrees(
              MathHelper.sin(wobble) * wobble / SledEntity.HURT_WOBBLE_TICKS * WOBBLE_DEGREES));
    }
  }
}
