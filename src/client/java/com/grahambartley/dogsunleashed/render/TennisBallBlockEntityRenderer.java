package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.block.entity.TennisBallBlockEntity;
import com.grahambartley.dogsunleashed.model.TennisBallModel;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class TennisBallBlockEntityRenderer extends GeoBlockRenderer<TennisBallBlockEntity> {

  public static final float PLACED_SCALE = 5f / 12f;

  public TennisBallBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    super(new TennisBallModel());
    withScale(PLACED_SCALE);
  }
}
