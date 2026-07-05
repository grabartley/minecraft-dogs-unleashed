package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.entity.HuskyEntity;
import com.grahambartley.dogsunleashed.render.HuskyRenderer;
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
