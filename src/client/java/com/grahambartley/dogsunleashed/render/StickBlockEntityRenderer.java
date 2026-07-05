package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.block.entity.StickBlockEntity;
import com.grahambartley.dogsunleashed.model.StickModel;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class StickBlockEntityRenderer extends GeoBlockRenderer<StickBlockEntity> {

  public StickBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    super(new StickModel());
  }
}
