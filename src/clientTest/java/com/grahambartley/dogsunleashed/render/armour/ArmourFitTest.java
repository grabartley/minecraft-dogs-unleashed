package com.grahambartley.dogsunleashed.render.armour;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ArmourFitTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = UnleashedDogBreed.class,
      names = {"HUSKY", "BEAGLE", "DACHSHUND", "SHIBA_INU", "GOLDEN_RETRIEVER"})
  @DisplayName("every breed with its own rig has torso bones for armour")
  void everyRigHasATorso(final UnleashedDogBreed breed) {
    assertFalse(ArmourFit.torsoBones(breed).isEmpty());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = UnleashedDogBreed.class,
      names = {"HUSKY", "BEAGLE", "DACHSHUND", "SHIBA_INU", "GOLDEN_RETRIEVER"})
  @DisplayName("every torso bone named for armour exists in that breed's rig")
  void torsoBonesExistInTheRig(final UnleashedDogBreed breed) {
    final Set<String> bones = new HashSet<>();
    JsonParser.parseReader(
            new InputStreamReader(
                ArmourFitTest.class
                    .getClassLoader()
                    .getResourceAsStream(
                        "assets/dogs-unleashed/geo/" + breed.serializedId() + ".geo.json"),
                StandardCharsets.UTF_8))
        .getAsJsonObject()
        .getAsJsonArray("minecraft:geometry")
        .get(0)
        .getAsJsonObject()
        .getAsJsonArray("bones")
        .forEach(bone -> bones.add(bone.getAsJsonObject().get("name").getAsString()));

    assertTrue(bones.containsAll(ArmourFit.torsoBones(breed)), breed + " has " + bones);
  }

  @Test
  @DisplayName("the golden's armour covers its chest core, not the fluff in front of it")
  void goldenSkipsChestFluff() {
    assertTrue(ArmourFit.covers(UnleashedDogBreed.GOLDEN_RETRIEVER, "bone", 0));
    assertFalse(ArmourFit.covers(UnleashedDogBreed.GOLDEN_RETRIEVER, "bone", 1));
  }

  @Test
  @DisplayName("bones outside the torso are never armoured")
  void legsAreNeverCovered() {
    assertFalse(ArmourFit.covers(UnleashedDogBreed.BEAGLE, "frontleg1", 0));
  }
}
