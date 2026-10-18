package com.grahambartley.dogsunleashed.render.cosmetic;

import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import com.grahambartley.dogsunleashed.render.rig.HeadTopLocator;
import com.grahambartley.dogsunleashed.render.rig.HeadTopLocator.HeadTop;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public record HatFit(HeadTop top, float width, Set<String> hiddenEars) {

  public static Optional<HatFit> of(
      final BakedGeoModel dog, final RigProfile profile, final DogCosmetic hat) {
    return HeadTopLocator.locate(dog, profile)
        .map(
            top -> {
              final float width = CosmeticFit.hatWidth(hat, top.headWidth());
              return new HatFit(top, width, earsUnder(top, width));
            });
  }

  static Set<String> earsUnder(final HeadTop top, final float hatWidth) {
    final float half = hatWidth * 0.5f;
    final Vector3f crown = top.crown();
    final Set<String> covered = new HashSet<>();
    for (final HeadTopLocator.RisingEar ear : top.risingEars()) {
      final CubeGeometry.Bounds bounds = ear.bounds();
      if (bounds.min().x < crown.x + half
          && bounds.max().x > crown.x - half
          && bounds.min().z < crown.z + half
          && bounds.max().z > crown.z - half) {
        covered.addAll(ear.bones());
      }
    }
    return Set.copyOf(covered);
  }
}
