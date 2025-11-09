package com.grahambartley.render.layer;

import com.grahambartley.entity.HuskyEntity;
import com.grahambartley.render.HuskyRenderer;
import net.minecraft.util.Identifier;

public class HuskyCollarLayer extends CollarLayer<HuskyEntity> {

  private final Identifier collarTexture =
      Identifier.of("dogs-unleashed", "textures/entity/husky_collar.png");

  public HuskyCollarLayer(final HuskyRenderer huskyRenderer) {
    super(huskyRenderer);
  }

  public Identifier getCollarTexture() {
    return collarTexture;
  }
}
