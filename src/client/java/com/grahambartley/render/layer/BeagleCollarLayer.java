package com.grahambartley.render.layer;

import com.grahambartley.entity.BeagleEntity;
import com.grahambartley.render.BeagleRenderer;
import net.minecraft.util.Identifier;

public class BeagleCollarLayer extends CollarLayer<BeagleEntity> {

  private final Identifier collarTexture =
      Identifier.of("dogs-unleashed", "textures/entity/beagle_collar.png");

  public BeagleCollarLayer(final BeagleRenderer beagleRenderer) {
    super(beagleRenderer);
  }

  public Identifier getCollarTexture() {
    return collarTexture;
  }
}
