package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.render.overlay.OverlayEffects;
import com.grahambartley.dogsunleashed.render.overlay.ProjectedOverlays;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoRenderer;

public class DogSheetOverlayLayer extends TranslucentOverlayLayer {

  public DogSheetOverlayLayer(final GeoRenderer<UnleashedDogEntity> renderer) {
    super(renderer);
  }

  @Override
  protected List<Identifier> overlaysOf(final UnleashedDogEntity dog) {
    return OverlayEffects.dogs().activeOn(dog).stream()
        .map(sheet -> ProjectedOverlays.shared().textureOf(dog.getRigSourceBreed(), sheet))
        .flatMap(Optional::stream)
        .toList();
  }
}
