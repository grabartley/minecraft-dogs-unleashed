package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.BackTopLocator.BackTop;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.Arrays;
import java.util.stream.Stream;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;

class BackTopLocatorTest {

  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);

  static Stream<UnleashedDogBreed> rigs() {
    return Arrays.stream(UnleashedDogBreed.values())
        .filter(rig -> rig != UnleashedDogBreed.CROSS_BREED);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("every rig has a back top on a torso bone, as wide as its torso")
  void everyRigHasABackTop(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final RigProfile profile = BUNDLED.profileOf(rig);
    final BackTop back = BackTopLocator.locate(model, profile).orElseThrow();
    final CubeGeometry.Bounds torso = SlotBounds.of(model, profile.bonesIn(Slot.TORSO));

    assertTrue(profile.bonesIn(Slot.TORSO).contains(back.boneName()));
    assertEquals(torso.max().x - torso.min().x, back.width(), 1.0e-5f);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("the back top sits over the middle of the torso, in its upper half")
  void backTopSitsOnTheMiddleOfTheBack(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final RigProfile profile = BUNDLED.profileOf(rig);
    final BackTop back = BackTopLocator.locate(model, profile).orElseThrow();
    final CubeGeometry.Bounds torso = SlotBounds.of(model, profile.bonesIn(Slot.TORSO));
    final Vector3f posed =
        RestPose.of(model.getBone(back.boneName()).orElseThrow()).point(back.point());

    assertEquals(torso.center().x, posed.x, 1.0e-4f);
    assertEquals(torso.center().z, posed.z, 1.0e-4f);
    assertTrue(posed.y > torso.center().y, "the anchor should be on top, at y " + posed.y);
    assertTrue(posed.y <= torso.max().y + 1.0e-4f);
  }
}
