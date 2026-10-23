package com.grahambartley.dogsunleashed.render.layer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import com.grahambartley.dogsunleashed.render.rig.LivePose;
import com.grahambartley.dogsunleashed.render.rig.RenderedBounds;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.util.RenderUtil;

class HeldFrisbeeTest {

  private static final float TOLERANCE = 1.0e-4f;
  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);

  private static MouthGrip grip(final float forwardZ, final float headWidth) {
    return new MouthGrip("snout", new Vector3f(), new Vector3f(0, 0, forwardZ), headWidth);
  }

  @ParameterizedTest(name = "forward z {0}, head {1}, model {2}")
  @CsvSource({"-1, 0.34, 1", "1, 0.125, 1", "-1, 0.285, 2", "1, 0.2, 0.5"})
  @DisplayName("the disc is one and a quarter head widths across")
  void isSizedAgainstTheHead(final float forwardZ, final float headWidth, final float model) {
    final Matrix4f disc = HeldFrisbee.gripToDisc(grip(forwardZ, headWidth), model);
    final Vector3f left = disc.transformPosition(new Vector3f(-model / 2.0f, 0, 0));
    final Vector3f right = disc.transformPosition(new Vector3f(model / 2.0f, 0, 0));
    assertEquals(1.25f * headWidth, left.distance(right), TOLERANCE);
  }

  @ParameterizedTest(name = "forward z {0}, head {1}, model {2}")
  @CsvSource({"-1, 0.34, 1", "1, 0.125, 1", "-1, 0.285, 2", "1, 0.2, 0.5"})
  @DisplayName("the near rim is in the dog's teeth")
  void nearRimIsAtTheGrip(final float forwardZ, final float headWidth, final float model) {
    final Matrix4f disc = HeldFrisbee.gripToDisc(grip(forwardZ, headWidth), model);
    final Vector3f nearRim =
        disc.transformPosition(new Vector3f(0, 0, -Math.signum(forwardZ) * model / 2.0f));
    assertEquals(0.0f, nearRim.length(), TOLERANCE);
  }

  @ParameterizedTest(name = "forward z {0}, head {1}, model {2}")
  @CsvSource({"-1, 0.34, 1", "1, 0.125, 1", "-1, 0.285, 2", "1, 0.2, 0.5"})
  @DisplayName("the far rim sticks out in front of the muzzle, tipped a little nose-down")
  void farRimSticksOutTippedDown(final float forwardZ, final float headWidth, final float model) {
    final Matrix4f disc = HeldFrisbee.gripToDisc(grip(forwardZ, headWidth), model);
    final Vector3f farRim =
        disc.transformPosition(new Vector3f(0, 0, Math.signum(forwardZ) * model / 2.0f));
    final float diameter = 1.25f * headWidth;
    final double tip = Math.toRadians(15.0);
    assertEquals(-diameter * Math.sin(tip), farRim.y, TOLERANCE);
    assertEquals(Math.signum(forwardZ) * diameter * Math.cos(tip), farRim.z, TOLERANCE);
    assertEquals(0.0f, farRim.x, TOLERANCE);
  }

  static Stream<UnleashedDogBreed> rigs() {
    return Arrays.stream(UnleashedDogBreed.values())
        .filter(rig -> rig != UnleashedDogBreed.CROSS_BREED);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("at rest the held disc clears the head, neck, body and front legs")
  void clearsTheBodyAtRest(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final RigProfile profile = BUNDLED.profileOf(rig);
    final MouthGrip grip =
        MouthGrip.of(model, profile, rig.renderTransforms().bodyYawOffsetDegrees()).orElseThrow();
    final Matrix4f disc = placedAsTheLayerDrawsIt(model, grip);

    final Set<String> body = new HashSet<>();
    for (final Slot slot :
        List.of(
            Slot.HEAD,
            Slot.EYES,
            Slot.NECK,
            Slot.TORSO,
            Slot.LEG_FRONT_LEFT,
            Slot.LEG_FRONT_RIGHT)) {
      body.addAll(profile.bonesIn(slot));
    }
    body.removeAll(profile.bonesIn(Slot.MOUTH));
    final List<CubeGeometry.Bounds> cubes = RenderedBounds.cubesOf(model, body);

    for (int ring = 1; ring <= 4; ring++) {
      for (int step = 0; step < 16; step++) {
        final double angle = Math.PI * 2.0 * step / 16.0;
        final float radius = 0.5f * ring / 4.0f;
        final Vector3f point =
            disc.transformPosition(
                new Vector3f(
                    (float) (Math.cos(angle) * radius), 0.0f, (float) (Math.sin(angle) * radius)));
        for (final CubeGeometry.Bounds cube : cubes) {
          assertFalse(inside(point, cube), rig + " disc point " + point + " inside a body cube");
        }
      }
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("as the layer draws it, the near rim sits on the front of the muzzle")
  void nearRimSitsOnTheMuzzleFront(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final RigProfile profile = BUNDLED.profileOf(rig);
    final MouthGrip grip =
        MouthGrip.of(model, profile, rig.renderTransforms().bodyYawOffsetDegrees()).orElseThrow();
    final CubeGeometry.Bounds mouth = RenderedBounds.of(model, profile.bonesIn(Slot.MOUTH));
    final boolean facesSouth = Math.abs(rig.renderTransforms().bodyYawOffsetDegrees()) == 180.0f;

    final Vector3f nearRim =
        placedAsTheLayerDrawsIt(model, grip)
            .transformPosition(new Vector3f(0, 0, facesSouth ? -0.5f : 0.5f));

    assertEquals((mouth.min().x + mouth.max().x) / 2.0f, nearRim.x, 1.0e-3f);
    assertEquals(mouth.min().y + (mouth.max().y - mouth.min().y) * 0.25f, nearRim.y, 1.0e-3f);
    assertEquals(facesSouth ? mouth.max().z : mouth.min().z, nearRim.z, 1.0e-3f);
  }

  private static Matrix4f placedAsTheLayerDrawsIt(final BakedGeoModel model, final MouthGrip grip) {
    final GeoBone mouth = model.getBone(grip.boneName()).orElseThrow();
    final MatrixStack stack = new MatrixStack();
    stack.peek().getPositionMatrix().set(LivePose.of(mouth));
    RenderUtil.translateAndRotateMatrixForBone(stack, mouth);
    HeldFrisbee.place(stack, mouth, grip, 1.0f);
    return stack.peek().getPositionMatrix();
  }

  private static boolean inside(final Vector3f point, final CubeGeometry.Bounds cube) {
    final float margin = 1.0e-3f;
    return point.x > cube.min().x + margin
        && point.x < cube.max().x - margin
        && point.y > cube.min().y + margin
        && point.y < cube.max().y - margin
        && point.z > cube.min().z + margin
        && point.z < cube.max().z - margin;
  }
}
