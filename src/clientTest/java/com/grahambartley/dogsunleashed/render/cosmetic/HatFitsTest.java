package com.grahambartley.dogsunleashed.render.cosmetic;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.bernie.geckolib.cache.object.BakedGeoModel;

class HatFitsTest {

  private static final RigProfile HUSKY =
      new RigProfiles(RigProfiles::openBundled).profileOf(UnleashedDogBreed.HUSKY);

  @Test
  @DisplayName("the same dog model and hat reuse one fit")
  void reusesTheFit() {
    final HatFits fits = new HatFits();
    final BakedGeoModel dog = ShippedRigs.model("husky");

    assertSame(
        fits.of(dog, HUSKY, DogCosmetic.STRAW_HAT).orElseThrow(),
        fits.of(dog, HUSKY, DogCosmetic.STRAW_HAT).orElseThrow());
  }

  @Test
  @DisplayName("each hat gets its own fit")
  void eachHatHasItsOwnFit() {
    final HatFits fits = new HatFits();
    final BakedGeoModel dog = ShippedRigs.model("husky");

    assertNotSame(
        fits.of(dog, HUSKY, DogCosmetic.STRAW_HAT).orElseThrow(),
        fits.of(dog, HUSKY, DogCosmetic.PARTY_HAT).orElseThrow());
  }
}
