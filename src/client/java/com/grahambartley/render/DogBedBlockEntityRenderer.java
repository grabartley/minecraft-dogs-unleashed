package com.grahambartley.render;

import com.grahambartley.block.entity.DogBedBlockEntity;
import com.grahambartley.model.DogBedModel;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class DogBedBlockEntityRenderer extends GeoBlockRenderer<DogBedBlockEntity> {

  public DogBedBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    super(new DogBedModel());
  }

  @Override
  public void actuallyRender(
      MatrixStack poseStack,
      DogBedBlockEntity animatable,
      BakedGeoModel model,
      RenderLayer renderType,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      int colour) {

    final DyeColor bedColor = animatable.getColor();
    final int tintColor = bedColor.getEntityColor() | 0xFF000000;

    super.actuallyRender(
        poseStack,
        animatable,
        model,
        renderType,
        bufferSource,
        buffer,
        isReRender,
        partialTick,
        packedLight,
        packedOverlay,
        tintColor);
  }
}
