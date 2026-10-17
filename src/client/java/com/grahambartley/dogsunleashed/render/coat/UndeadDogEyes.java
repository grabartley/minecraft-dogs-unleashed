package com.grahambartley.dogsunleashed.render.coat;

import com.grahambartley.dogsunleashed.render.rig.SlotRegion;
import com.grahambartley.dogsunleashed.render.rig.Texel;
import java.util.List;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public record UndeadDogEyes(int atlasWidth, int atlasHeight, List<Texel> pupils) {

  public static final UndeadDogEyes NONE = new UndeadDogEyes(0, 0, List.of());

  public UndeadDogEyes {
    pupils = List.copyOf(pupils);
  }

  public static UndeadDogEyes of(
      final BakedGeoModel model, final List<Texel> pupils, final List<SlotRegion> headRegions) {
    return new UndeadDogEyes(
        (int) model.properties().textureWidth(),
        (int) model.properties().textureHeight(),
        pupils.stream()
            .filter(pupil -> headRegions.stream().anyMatch(region -> region.contains(pupil)))
            .toList());
  }

  public boolean fits(final int width, final int height) {
    return width == this.atlasWidth && height == this.atlasHeight;
  }
}
