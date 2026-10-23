package com.grahambartley.dogsunleashed.render.layer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import com.grahambartley.dogsunleashed.render.rig.LivePose;
import com.grahambartley.dogsunleashed.render.rig.RenderedBounds;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import java.util.Arrays;
import java.util.stream.Stream;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;

class MouthGripTest {

  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);
  private static final float TOLERANCE = 1.0e-3f;

  static Stream<UnleashedDogBreed> rigs() {
    return Arrays.stream(UnleashedDogBreed.values())
        .filter(rig -> rig != UnleashedDogBreed.CROSS_BREED);
  }

  private static MouthGrip gripOf(final UnleashedDogBreed rig) {
    return MouthGrip.of(
            ShippedRigs.model(rig.serializedId()),
            BUNDLED.profileOf(rig),
            rig.renderTransforms().bodyYawOffsetDegrees())
        .orElseThrow();
  }

  private static boolean facesSouth(final UnleashedDogBreed rig) {
    return Math.abs(rig.renderTransforms().bodyYawOffsetDegrees()) == 180.0f;
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("the grip sits on the front of the muzzle, centred, a quarter up from its bottom")
  void gripsTheFrontOfTheMuzzle(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final RigProfile profile = BUNDLED.profileOf(rig);
    final CubeGeometry.Bounds mouth = RenderedBounds.of(model, profile.bonesIn(Slot.MOUTH));
    final MouthGrip grip = gripOf(rig);

    final Vector3f inModel =
        LivePose.of(model.getBone(grip.boneName()).orElseThrow()).transformPosition(grip.point());

    assertEquals((mouth.min().x + mouth.max().x) / 2.0f, inModel.x, TOLERANCE);
    assertEquals(mouth.min().y + (mouth.max().y - mouth.min().y) * 0.25f, inModel.y, TOLERANCE);
    assertEquals(facesSouth(rig) ? mouth.max().z : mouth.min().z, inModel.z, TOLERANCE);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("forward points out of the muzzle, the way the rig faces")
  void forwardPointsOutOfTheMuzzle(final UnleashedDogBreed rig) {
    final Vector3f forward = gripOf(rig).forward();
    assertEquals(0.0f, forward.x, TOLERANCE);
    assertEquals(0.0f, forward.y, TOLERANCE);
    assertEquals(facesSouth(rig) ? 1.0f : -1.0f, forward.z, TOLERANCE);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("the head width is the width of the head slot as GeckoLib draws it")
  void measuresTheHeadWidth(final UnleashedDogBreed rig) {
    final CubeGeometry.Bounds head =
        RenderedBounds.of(
            ShippedRigs.model(rig.serializedId()), BUNDLED.profileOf(rig).bonesIn(Slot.HEAD));
    assertEquals(head.max().x - head.min().x, gripOf(rig).headWidth(), TOLERANCE);
  }
}
