package com.grahambartley.render;

import com.grahambartley.entity.DalmatianEntity;
import com.grahambartley.model.DalmatianModel;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * GeckoLib renderer for Dalmatian entity Note: GeckoLib 4.7 (for MC 1.21.1) may have compatibility
 * issues with MC 1.21.10's render state changes. This will work once you update to a compatible MC
 * version or when GeckoLib releases a 1.21.10 version.
 */
public class DalmatianRenderer extends GeoEntityRenderer<DalmatianEntity> {

  public DalmatianRenderer(EntityRendererFactory.Context context) {
    super(context, new DalmatianModel());
  }
}
