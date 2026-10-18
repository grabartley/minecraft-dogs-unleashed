package com.grahambartley.dogsunleashed.render.cosmetic;

import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import com.grahambartley.dogsunleashed.render.rig.HeadTopLocator;
import com.grahambartley.dogsunleashed.render.rig.HeadTopLocator.HeadTop;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import java.util.Optional;
import java.util.Set;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public record HatFit(HeadTop top, float width, Set<String> hiddenEars) {

  public static Optional<HatFit> of(
      final BakedGeoModel dog, final RigProfile profile, final DogCosmetic hat) {
    return HeadTopLocator.locate(dog, profile)
        .map(
            top -> {
              final float width = CosmeticFit.hatWidth(hat, top.headWidth());
              return new HatFit(top, width, top.earsUnder(width));
            });
  }
}
