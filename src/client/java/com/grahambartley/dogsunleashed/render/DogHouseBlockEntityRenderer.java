package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.block.entity.DogHouseBlockEntity;
import com.grahambartley.dogsunleashed.model.DogHouseModel;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * The model spans both of the block's cells, so it is drawn once from the lower half. The upper
 * half carries no block entity and therefore no second copy.
 */
public class DogHouseBlockEntityRenderer extends GeoBlockRenderer<DogHouseBlockEntity> {

  public DogHouseBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    super(new DogHouseModel());
  }

  /**
   * {@code GeoBlockRenderer} leaves the model's own origin on the cell's corner, so without this
   * the house draws half a block off the cells it actually occupies. Centring here also puts the
   * facing rotation on the cell's middle, which is what carries the model into the right cells for
   * every facing.
   */
  @Override
  public void preRender(
      MatrixStack poseStack,
      DogHouseBlockEntity animatable,
      BakedGeoModel model,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      int colour) {
    if (!isReRender) {
      poseStack.translate(0.5, 0, 0.5);
    }
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

  @Override
  public int getRenderDistance() {
    return 128;
  }
}
