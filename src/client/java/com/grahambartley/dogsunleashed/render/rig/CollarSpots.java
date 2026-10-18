package com.grahambartley.dogsunleashed.render.rig;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.CollarBandLocator.CollarSpot;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public final class CollarSpots {

  private CollarSpots() {}

  public static Optional<CollarSpot> locate(
      final BakedGeoModel model, final UnleashedDogBreed rig, final String mouthBone) {
    final Identifier collarTexture =
        Identifier.of(
            DogsUnleashed.MOD_ID, "textures/entity/" + rig.serializedId() + "_collar.png");
    return CollarTextureMask.load(MinecraftClient.getInstance().getResourceManager(), collarTexture)
        .flatMap(mask -> CollarBandLocator.locate(model, mouthBone, mask));
  }
}
