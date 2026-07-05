package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.entity.BeagleEntity;
import com.grahambartley.dogsunleashed.render.BeagleRenderer;
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
