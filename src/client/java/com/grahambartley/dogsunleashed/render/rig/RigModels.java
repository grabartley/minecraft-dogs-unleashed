package com.grahambartley.dogsunleashed.render.rig;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.Optional;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public final class RigModels {

  private RigModels() {}

  public static Identifier idOf(final UnleashedDogBreed rig) {
    return Identifier.of(MOD_ID, rig.geoPath());
  }

  public static Optional<BakedGeoModel> loaded(final UnleashedDogBreed rig) {
    return Optional.ofNullable(GeckoLibCache.getBakedModels().get(idOf(rig)));
  }
}
