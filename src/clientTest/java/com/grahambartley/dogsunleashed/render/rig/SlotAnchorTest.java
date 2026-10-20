package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

class SlotAnchorTest {

  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);
  private static final float TOLERANCE = 1.0e-4f;

  static Stream<Arguments> rigSlots() {
    return Arrays.stream(UnleashedDogBreed.values())
        .filter(rig -> rig != UnleashedDogBreed.CROSS_BREED)
        .flatMap(
            rig ->
                Arrays.stream(Slot.values())
                    .filter(slot -> !BUNDLED.profileOf(rig).bonesIn(slot).isEmpty())
                    .map(slot -> Arguments.of(rig, slot)));
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("rigSlots")
  @DisplayName("a slot is anchored on its shallowest bone at the centre of its rest bounds")
  void anchorSitsOnTheSlot(final UnleashedDogBreed rig, final Slot slot) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final Set<String> bones = BUNDLED.profileOf(rig).bonesIn(slot);
    final SlotAnchor anchor = SlotAnchor.locate(model, bones).orElseThrow();
    final GeoBone bone = model.getBone(anchor.boneName()).orElseThrow();

    assertTrue(bones.contains(anchor.boneName()));
    for (final String other : bones) {
      model
          .getBone(other)
          .ifPresent(candidate -> assertTrue(depthOf(candidate) >= depthOf(bone), other));
    }
    final Vector3f expected = SlotBounds.of(model, bones).center();
    final Vector3f actual = RestPose.of(bone).point(anchor.centre());
    assertEquals(expected.x, actual.x, TOLERANCE);
    assertEquals(expected.y, actual.y, TOLERANCE);
    assertEquals(expected.z, actual.z, TOLERANCE);
    assertTrue(anchor.size().x > 0 && anchor.size().y > 0 && anchor.size().z > 0);
  }

  @Test
  @DisplayName("a slot whose bones are missing from the model has no anchor")
  void missingBonesHaveNoAnchor() {
    assertTrue(SlotAnchor.locate(ShippedRigs.model("husky"), Set.of("no_such_bone")).isEmpty());
  }

  private static int depthOf(final GeoBone bone) {
    int depth = 0;
    for (GeoBone parent = bone.getParent(); parent != null; parent = parent.getParent()) {
      depth++;
    }
    return depth;
  }
}
