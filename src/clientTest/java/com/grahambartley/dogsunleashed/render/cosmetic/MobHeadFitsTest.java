package com.grahambartley.dogsunleashed.render.cosmetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.item.cosmetic.MobHead;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;

class MobHeadFitsTest {

  private static final RigProfile HUSKY =
      new RigProfiles(RigProfiles::openBundled).profileOf(UnleashedDogBreed.HUSKY);

  @Test
  @DisplayName("the same dog model reuses one fit")
  void reusesTheFit() {
    final MobHeadFits fits = new MobHeadFits();
    final BakedGeoModel dog = ShippedRigs.model("husky");

    assertSame(fits.of(dog, HUSKY).orElseThrow(), fits.of(dog, HUSKY).orElseThrow());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(MobHead.class)
  @DisplayName("a drawn mob head hides the ears its fit covers")
  void drawnHeadHidesEars(final MobHead head) {
    final MobHeadFits fits = new MobHeadFits();
    final BakedGeoModel dog = ShippedRigs.model("husky");

    assertEquals(
        fits.of(dog, HUSKY).orElseThrow().hiddenEars(),
        fits.earsHiddenBy(Optional.of(head), dog, HUSKY));
  }

  @Test
  @DisplayName("the husky's standing ears go under a mob head")
  void huskyEarsAreHidden() {
    assertTrue(
        new MobHeadFits()
            .earsHiddenBy(Optional.of(MobHead.CREEPER), ShippedRigs.model("husky"), HUSKY)
            .containsAll(HUSKY.bonesIn(RigProfile.Slot.EAR_LEFT)));
  }

  @Test
  @DisplayName("no mob head hides no ears")
  void nothingDrawnHidesNothing() {
    assertTrue(
        new MobHeadFits()
            .earsHiddenBy(Optional.empty(), ShippedRigs.model("husky"), HUSKY)
            .isEmpty());
  }
}
