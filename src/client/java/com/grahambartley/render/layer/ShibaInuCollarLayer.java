package com.grahambartley.render.layer;

import com.grahambartley.entity.ShibaInuEntity;
import com.grahambartley.render.ShibaInuRenderer;
import net.minecraft.util.Identifier;

public class ShibaInuCollarLayer extends CollarLayer<ShibaInuEntity> {

  private final Identifier collarTexture =
      Identifier.of("dogs-unleashed", "textures/entity/shibainu_collar.png");

  public ShibaInuCollarLayer(final ShibaInuRenderer shibaInuRenderer) {
    super(shibaInuRenderer);
  }

  public Identifier getCollarTexture() {
    return collarTexture;
  }
}
