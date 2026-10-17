package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Overlay;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import software.bernie.geckolib.cache.object.GeoBone;

class RigProfilesTest {

  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = UnleashedDogBreed.class,
      names = {"HUSKY", "BEAGLE", "DACHSHUND", "SHIBA_INU", "GOLDEN_RETRIEVER"})
  @DisplayName("every rig ships a profile with a torso and a mouth")
  void everyRigHasAProfile(final UnleashedDogBreed rig) {
    assertFalse(BUNDLED.profileOf(rig).bonesIn(Slot.TORSO).isEmpty());
    assertTrue(BUNDLED.profileOf(rig).mouthBone().isPresent());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = UnleashedDogBreed.class,
      names = {"HUSKY", "BEAGLE", "DACHSHUND", "SHIBA_INU", "GOLDEN_RETRIEVER"})
  @DisplayName("every bone a profile names exists in that rig")
  void profileBonesExistInTheRig(final UnleashedDogBreed rig) {
    final Set<String> bones = new HashSet<>();
    collect(ShippedRigs.model(rig.serializedId()).topLevelBones(), bones);
    final RigProfile profile = BUNDLED.profileOf(rig);
    for (final Slot slot : Slot.values()) {
      assertTrue(
          bones.containsAll(profile.bonesIn(slot)),
          rig + " " + slot + " names " + profile.bonesIn(slot) + " but the rig has " + bones);
    }
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = UnleashedDogBreed.class,
      names = {"HUSKY", "BEAGLE", "DACHSHUND", "SHIBA_INU", "GOLDEN_RETRIEVER"})
  @DisplayName("every overlay a profile names is a texture that ships")
  void profileOverlaysExist(final UnleashedDogBreed rig) {
    for (final Overlay overlay : Overlay.values()) {
      BUNDLED
          .profileOf(rig)
          .overlay(overlay)
          .ifPresent(
              id ->
                  assertNotNull(
                      RigProfilesTest.class.getResource(
                          "/assets/" + id.getNamespace() + "/" + id.getPath()),
                      rig + " names a missing overlay " + id));
    }
  }

  @Test
  @DisplayName("a rig without a profile file gets the empty profile")
  void missingProfileIsEmpty() {
    final RigProfiles none = new RigProfiles(id -> Optional.empty());
    assertEquals(RigProfile.EMPTY, none.profileOf(UnleashedDogBreed.HUSKY));
  }

  @Test
  @DisplayName("a broken profile file is treated as empty rather than crashing the client")
  void brokenProfileIsEmpty() {
    final RigProfiles broken =
        new RigProfiles(id -> Optional.<Reader>of(new StringReader("{ not json")));
    assertEquals(RigProfile.EMPTY, broken.profileOf(UnleashedDogBreed.HUSKY));
  }

  @Test
  @DisplayName("profiles are read once and read again after a clear")
  void cachesUntilCleared() {
    final int[] reads = {0};
    final RigProfiles counting =
        new RigProfiles(
            id -> {
              reads[0]++;
              return Optional.<Reader>of(new StringReader("{\"torso\": [\"main\"]}"));
            });

    counting.profileOf(UnleashedDogBreed.HUSKY);
    counting.profileOf(UnleashedDogBreed.HUSKY);
    counting.clear();
    counting.profileOf(UnleashedDogBreed.HUSKY);

    assertEquals(2, reads[0]);
  }

  private static void collect(final Iterable<GeoBone> bones, final Set<String> into) {
    for (final GeoBone bone : bones) {
      into.add(bone.getName());
      collect(bone.getChildBones(), into);
    }
  }
}
