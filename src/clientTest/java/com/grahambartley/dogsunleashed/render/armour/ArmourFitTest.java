package com.grahambartley.dogsunleashed.render.armour;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

class ArmourFitTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = UnleashedDogBreed.class,
      names = {
        "HUSKY",
        "BEAGLE",
        "DACHSHUND",
        "SHIBA_INU",
        "GOLDEN_RETRIEVER",
        "GERMAN_SHEPHERD",
        "BORDER_COLLIE"
      })
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

  static Stream<Arguments> armouredCubes() {
    return Stream.of(
        Arguments.of(
            UnleashedDogBreed.HUSKY,
            List.of("main#0", "belly#0", "belly#1", "belly#2", "frontchest#0")),
        Arguments.of(UnleashedDogBreed.DACHSHUND, List.of("torso#0", "torso#1", "bunda#0")),
        Arguments.of(UnleashedDogBreed.BEAGLE, List.of("bone#0", "Torso#0")),
        Arguments.of(UnleashedDogBreed.GOLDEN_RETRIEVER, List.of("bone#0", "Torso#0", "Torso#1")),
        Arguments.of(UnleashedDogBreed.SHIBA_INU, List.of("bone#0", "Torso#0")),
        Arguments.of(UnleashedDogBreed.GERMAN_SHEPHERD, List.of("bone#0", "Torso#0")),
        Arguments.of(UnleashedDogBreed.BORDER_COLLIE, List.of("bone#0", "Torso#0")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("armouredCubes")
  @DisplayName("each rig armours exactly the torso cubes it always has")
  void armoursTheSameCubes(final UnleashedDogBreed breed, final List<String> expected) {
    final List<String> armoured = new ArrayList<>();
    collect(ShippedRigs.model(breed.serializedId()).topLevelBones(), breed, armoured);
    assertEquals(expected, armoured);
  }

  private static void collect(
      final List<GeoBone> bones, final UnleashedDogBreed breed, final List<String> out) {
    for (final GeoBone bone : bones) {
      for (int i = 0; i < bone.getCubes().size(); i++) {
        if (ArmourFit.covers(breed, bone.getName(), bone.getCubes().get(i))) {
          out.add(bone.getName() + "#" + i);
        }
      }
      collect(bone.getChildBones(), breed, out);
    }
  }
}
