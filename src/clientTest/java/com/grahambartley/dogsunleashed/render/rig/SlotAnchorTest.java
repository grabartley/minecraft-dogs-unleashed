package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.costume.CostumePartFit;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.util.RenderUtil;

class SlotAnchorTest {

  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);
  private static final float TOLERANCE = 1.0e-3f;

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
  @DisplayName("a slot is anchored on its shallowest bone")
  void anchorIsTheShallowestSlotBone(final UnleashedDogBreed rig, final Slot slot) {
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
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("rigSlots")
  @DisplayName("the part reference box lands on the slot's bounds as GeckoLib draws them")
  void referenceBoxMatchesTheRenderedSlot(final UnleashedDogBreed rig, final Slot slot) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final Set<String> bones = BUNDLED.profileOf(rig).bonesIn(slot);
    final SlotAnchor anchor = SlotAnchor.locate(model, bones).orElseThrow();
    final Matrix4f boxToModel =
        LivePose.of(model.getBone(anchor.boneName()).orElseThrow())
            .mul(CostumePartFit.partToBone(anchor, 0.0f));
    final CubeGeometry.Bounds rendered = renderedBounds(model, bones);

    assertPoint(rendered.min(), boxToModel.transformPosition(new Vector3f(-0.5f)));
    assertPoint(rendered.max(), boxToModel.transformPosition(new Vector3f(0.5f)));
  }

  private static CubeGeometry.Bounds renderedBounds(
      final BakedGeoModel model, final Set<String> bones) {
    final CubeGeometry.Bounds bounds = new CubeGeometry.Bounds();
    for (final String name : bones) {
      model
          .getBone(name)
          .ifPresent(
              bone -> {
                final Matrix4f boneMatrix = LivePose.of(bone);
                for (final GeoCube cube : bone.getCubes()) {
                  final MatrixStack stack = new MatrixStack();
                  stack.peek().getPositionMatrix().set(boneMatrix);
                  RenderUtil.translateToPivotPoint(stack, cube);
                  RenderUtil.rotateMatrixAroundCube(stack, cube);
                  RenderUtil.translateAwayFromPivotPoint(stack, cube);
                  for (final GeoQuad quad : cube.quads()) {
                    if (quad == null) {
                      continue;
                    }
                    for (final GeoVertex vertex : quad.vertices()) {
                      bounds.include(
                          stack
                              .peek()
                              .getPositionMatrix()
                              .transformPosition(new Vector3f(vertex.position())));
                    }
                  }
                }
              });
    }
    return bounds;
  }

  private static void assertPoint(final Vector3f expected, final Vector3f actual) {
    assertEquals(expected.x, actual.x, TOLERANCE);
    assertEquals(expected.y, actual.y, TOLERANCE);
    assertEquals(expected.z, actual.z, TOLERANCE);
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
