package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.entity.DachshundEntity;
import com.grahambartley.dogsunleashed.render.DachshundRenderer;
import net.minecraft.util.Identifier;

public class DachshundCollarLayer extends CollarLayer<DachshundEntity> {

  private final Identifier collarTexture =
      Identifier.of("dogs-unleashed", "textures/entity/dachshund_collar.png");

  public DachshundCollarLayer(final DachshundRenderer dachshundRenderer) {
    super(dachshundRenderer);
  }

  public Identifier getCollarTexture() {
    return collarTexture;
  }
}
