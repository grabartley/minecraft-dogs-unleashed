package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.render.fur.FurLook;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import java.util.List;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoRenderer;

public class DogFurOverlayLayer extends TranslucentOverlayLayer {

  public DogFurOverlayLayer(final GeoRenderer<UnleashedDogEntity> renderer) {
    super(renderer);
  }

  @Override
  protected List<Identifier> overlaysOf(final UnleashedDogEntity dog) {
    return FurLook.overlays(RigProfiles.of(dog.getRigSourceBreed()), dog.getFur().stage());
  }
}
