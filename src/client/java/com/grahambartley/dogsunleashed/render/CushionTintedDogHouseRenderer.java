package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.block.entity.DogHouseBlockEntity;
import com.grahambartley.dogsunleashed.model.DogHouseModel;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class CushionTintedDogHouseRenderer extends GeoBlockRenderer<DogHouseBlockEntity> {

  public CushionTintedDogHouseRenderer() {
    super(new DogHouseModel());
  }

  @Override
  public void renderRecursively(
      final MatrixStack poseStack,
      final DogHouseBlockEntity animatable,
      final GeoBone bone,
      final RenderLayer renderType,
      final VertexConsumerProvider bufferSource,
      final VertexConsumer buffer,
      final boolean isReRender,
      final float partialTick,
      final int packedLight,
      final int packedOverlay,
      final int colour) {
    super.renderRecursively(
        poseStack,
        animatable,
        bone,
        renderType,
        bufferSource,
        buffer,
        isReRender,
        partialTick,
        packedLight,
        packedOverlay,
        DogHouseCushionTint.forBone(bone.getName(), animatable.getColor(), colour));
  }
}
