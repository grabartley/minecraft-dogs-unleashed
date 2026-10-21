package com.grahambartley.dogsunleashed.render.sled;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

class HarnessRingTest {

  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);

  static Stream<UnleashedDogBreed> rigs() {
    return Arrays.stream(UnleashedDogBreed.values())
        .filter(rig -> rig != UnleashedDogBreed.CROSS_BREED);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("the line clips on at the root of each rig's tail")
  void ringIsTheTailRoot(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final RigProfile profile = BUNDLED.profileOf(rig);
    final String ring = HarnessRing.boneOf(model, profile).orElseThrow();
    assertTrue(profile.bonesIn(RigProfile.Slot.TAIL).contains(ring));
    final GeoBone parent = model.getBone(ring).orElseThrow().getParent();
    assertTrue(
        parent == null || !profile.bonesIn(RigProfile.Slot.TAIL).contains(parent.getName()),
        ring + " sits inside another tail bone");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("a rig with no tail in its profile has no ring")
  void noTailNoRing(final UnleashedDogBreed rig) {
    assertEquals(
        Optional.empty(),
        HarnessRing.boneOf(ShippedRigs.model(rig.serializedId()), RigProfile.EMPTY));
  }
}
