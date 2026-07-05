package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.entity.GoldenRetrieverEntity;
import com.grahambartley.dogsunleashed.render.GoldenRetrieverRenderer;
import net.minecraft.util.Identifier;

public class GoldenRetrieverCollarLayer extends CollarLayer<GoldenRetrieverEntity> {

  private final Identifier collarTexture =
      Identifier.of("dogs-unleashed", "textures/entity/goldenretriever_collar.png");

  public GoldenRetrieverCollarLayer(final GoldenRetrieverRenderer goldenRetrieverRenderer) {
    super(goldenRetrieverRenderer);
  }

  public Identifier getCollarTexture() {
    return collarTexture;
  }
}
