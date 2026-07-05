package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.block.entity.TennisBallBlockEntity;
import com.grahambartley.dogsunleashed.model.TennisBallModel;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class TennisBallBlockEntityRenderer extends GeoBlockRenderer<TennisBallBlockEntity> {

  public TennisBallBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    super(new TennisBallModel());
  }

  @Override
  public void preRender(
      MatrixStack poseStack,
      TennisBallBlockEntity animatable,
      BakedGeoModel model,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      int colour) {
    super.preRender(
        poseStack,
        animatable,
        model,
        bufferSource,
        buffer,
        isReRender,
        partialTick,
        packedLight,
        packedOverlay,
        colour);
  }
}
