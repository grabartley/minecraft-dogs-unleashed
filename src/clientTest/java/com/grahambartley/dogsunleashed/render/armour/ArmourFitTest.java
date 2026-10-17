package com.grahambartley.dogsunleashed.render.armour;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

class ArmourFitTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = UnleashedDogBreed.class,
      names = {"HUSKY", "BEAGLE", "DACHSHUND", "SHIBA_INU", "GOLDEN_RETRIEVER"})
  @DisplayName("every breed with its own rig has torso bones for armour")
  void everyRigHasATorso(final UnleashedDogBreed breed) {
    assertFalse(ArmourFit.torsoBones(breed).isEmpty());
  }

  @Test
  @DisplayName("the golden's armour covers its chest core, not the fluff in front of it")
  void goldenSkipsChestFluff() {
    final GeoBone chest = bone("goldenretriever", "bone");

    assertTrue(
        ArmourFit.covers(UnleashedDogBreed.GOLDEN_RETRIEVER, "bone", chest.getCubes().get(0)));
    assertFalse(
        ArmourFit.covers(UnleashedDogBreed.GOLDEN_RETRIEVER, "bone", chest.getCubes().get(1)));
  }

  @Test
  @DisplayName("bones outside the torso are never armoured")
  void legsAreNeverCovered() {
    final GeoBone leg = bone("beagle", "bone11");

    assertFalse(ArmourFit.covers(UnleashedDogBreed.BEAGLE, "bone11", leg.getCubes().get(0)));
  }

  private static GeoBone bone(final String rig, final String name) {
    final BakedGeoModel model = ShippedRigs.model(rig);
    return model.getBone(name).orElseThrow();
  }
}
