package com.grahambartley.render;

import com.grahambartley.block.entity.FrisbeeBlockEntity;
import com.grahambartley.model.FrisbeeModel;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class FrisbeeBlockEntityRenderer extends GeoBlockRenderer<FrisbeeBlockEntity> {

  public FrisbeeBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    super(new FrisbeeModel());
  }

  @Override
  public void actuallyRender(
      MatrixStack poseStack,
      FrisbeeBlockEntity animatable,
      BakedGeoModel model,
      RenderLayer renderType,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      int colour) {

    DyeColor frisbeeColor = animatable.getColor();
    int tintColor = frisbeeColor.getEntityColor() | 0xFF000000;

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
