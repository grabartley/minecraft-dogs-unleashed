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
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.util.RenderUtil;

class HeldFrisbeeTest {

  private static final float TOLERANCE = 1.0e-4f;
  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);
  private static final float WORLD_DIAMETER = 7.0f / 16.0f;
  private static final double TIP = Math.toRadians(15.0);

  @ParameterizedTest(name = "forward z {0}, disc scale {1}, model {2}")
  @CsvSource({"-1, 0.4375, 1", "1, 0.2, 1", "-1, 0.3, 2", "1, 0.8, 0.5"})
  @DisplayName("the near rim of the disc is at the grip")
  void nearRimIsAtTheGrip(final float forwardZ, final float discScale, final float model) {
    final Matrix4f disc = discOf(forwardZ, discScale, model);
    final Vector3f nearRim =
        disc.transformPosition(new Vector3f(0, 0, -Math.signum(forwardZ) * model / 2.0f));
    assertEquals(0.0f, nearRim.length(), TOLERANCE);
  }

  @ParameterizedTest(name = "forward z {0}, disc scale {1}, model {2}")
  @CsvSource({"-1, 0.4375, 1", "1, 0.2, 1", "-1, 0.3, 2", "1, 0.8, 0.5"})
  @DisplayName("the far rim sticks out in front of the muzzle, tipped a little nose-down")
  void farRimSticksOutTippedDown(final float forwardZ, final float discScale, final float model) {
    final Matrix4f disc = discOf(forwardZ, discScale, model);
    final Vector3f farRim =
        disc.transformPosition(new Vector3f(0, 0, Math.signum(forwardZ) * model / 2.0f));
    final float diameter = discScale * model;
    assertEquals(-diameter * Math.sin(TIP), farRim.y, TOLERANCE);
    assertEquals(Math.signum(forwardZ) * diameter * Math.cos(TIP), farRim.z, TOLERANCE);
    assertEquals(0.0f, farRim.x, TOLERANCE);
  }

  @ParameterizedTest(name = "forward z {0}, disc scale {1}, model {2}")
  @CsvSource({"-1, 0.4375, 1", "1, 0.2, 1", "-1, 0.3, 2", "1, 0.8, 0.5"})
  @DisplayName("placing the disc only turns the normals, so it is lit like the dog")
  void keepsNormalsUnitLength(final float forwardZ, final float discScale, final float model) {
    final MatrixStack stack = new MatrixStack();
    stack.scale(16.0f, -16.0f, 16.0f);
    final Matrix3f before = new Matrix3f(stack.peek().getNormalMatrix());
    HeldFrisbee.gripToDisc(stack, new Vector3f(0, 0, forwardZ), discScale, model);
    final Matrix3f turn = new Matrix3f(before).invert().mul(stack.peek().getNormalMatrix());
    assertEquals(1.0f, turn.determinant(), TOLERANCE);
    assertEquals(1.0f, turn.transform(new Vector3f(1, 0, 0)).length(), TOLERANCE);
    assertEquals(1.0f, turn.transform(new Vector3f(0, 1, 0)).length(), TOLERANCE);
  }

  private static Matrix4f discOf(final float forwardZ, final float discScale, final float model) {
    final MatrixStack stack = new MatrixStack();
    HeldFrisbee.gripToDisc(stack, new Vector3f(0, 0, forwardZ), discScale, model);
    return stack.peek().getPositionMatrix();
  }

  static Stream<UnleashedDogBreed> rigs() {
    return Arrays.stream(UnleashedDogBreed.values())
        .filter(rig -> rig != UnleashedDogBreed.CROSS_BREED);
  }

  static Stream<Arguments> rigsAtEveryAge() {
    return rigs()
        .flatMap(
            rig ->
                Stream.of(
                    Arguments.of(rig, rig.renderTransforms().adultScale()),
                    Arguments.of(rig, rig.renderTransforms().babyScale())));
  }

  @ParameterizedTest(name = "{0} at scale {1}")
  @MethodSource("rigsAtEveryAge")
  @DisplayName("the disc is the same size in the world on every breed and age")
  void keepsOneWorldSize(final UnleashedDogBreed rig, final float dogScale) {
    final Matrix4f disc = placedAsTheLayerDrawsIt(rig, dogScale);
    final Vector3f left = disc.transformPosition(new Vector3f(-0.5f, 0, 0));
    final Vector3f right = disc.transformPosition(new Vector3f(0.5f, 0, 0));
    assertEquals(WORLD_DIAMETER, left.distance(right), 1.0e-3f);
  }

  @ParameterizedTest(name = "{0} at scale {1}")
  @MethodSource("rigsAtEveryAge")
  @DisplayName("as the layer draws it, the near rim sits on the front of the muzzle")
  void nearRimSitsOnTheMuzzleFront(final UnleashedDogBreed rig, final float dogScale) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final CubeGeometry.Bounds mouth =
        RenderedBounds.of(model, BUNDLED.profileOf(rig).bonesIn(Slot.MOUTH));
    final float out = outOf(rig);

    final Vector3f nearRim =
        placedAsTheLayerDrawsIt(rig, dogScale)
            .transformPosition(new Vector3f(0, 0, -out * 0.5f))
            .div(dogScale);

    assertEquals((mouth.min().x + mouth.max().x) / 2.0f, nearRim.x, 1.0e-3f);
    assertEquals(mouth.min().y + (mouth.max().y - mouth.min().y) * 0.25f, nearRim.y, 1.0e-3f);
    assertEquals(out > 0 ? mouth.max().z : mouth.min().z, nearRim.z, 1.0e-3f);
  }

  @ParameterizedTest(name = "{0} at scale {1}")
  @MethodSource("rigsAtEveryAge")
  @DisplayName("at rest the disc runs straight out of the muzzle, tipped 15 degrees down")
  void restsLevelOutOfTheMuzzle(final UnleashedDogBreed rig, final float dogScale) {
    final float out = outOf(rig);
    final Matrix4f disc = placedAsTheLayerDrawsIt(rig, dogScale);
    final Vector3f span =
        disc.transformPosition(new Vector3f(0, 0, out * 0.5f))
            .sub(disc.transformPosition(new Vector3f(0, 0, -out * 0.5f)));

    assertEquals(0.0f, span.x, 1.0e-3f);
    assertEquals(-WORLD_DIAMETER * Math.sin(TIP), span.y, 1.0e-3f);
    assertEquals(out * WORLD_DIAMETER * Math.cos(TIP), span.z, 1.0e-3f);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("with the head and mouth animated, the near rim stays where the mouth bone took it")
  void nearRimFollowsAnAnimatedMouth(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final RigProfile profile = BUNDLED.profileOf(rig);
    final MouthGrip grip = gripOf(rig, model);
    final GeoBone mouth = model.getBone(grip.boneName()).orElseThrow();
    final GeoBone head = model.getBone(profile.bonesIn(Slot.HEAD).iterator().next()).orElseThrow();
    head.setRotY(head.getRotY() + 0.35f);
    head.setRotX(head.getRotX() - 0.2f);
    mouth.setRotX(mouth.getRotX() + 0.3f);
    mouth.setRotZ(mouth.getRotZ() - 0.15f);

    final Vector3f nearRim =
        placedAsTheLayerDrawsIt(model, grip, 1.0f)
            .transformPosition(new Vector3f(0, 0, -outOf(rig) * 0.5f));
    final Vector3f expected = LivePose.of(mouth).transformPosition(grip.point());

    assertEquals(expected.x, nearRim.x, 1.0e-3f);
    assertEquals(expected.y, nearRim.y, 1.0e-3f);
    assertEquals(expected.z, nearRim.z, 1.0e-3f);
  }

  @ParameterizedTest(name = "{0} at scale {1}")
  @MethodSource("rigsAtEveryAge")
  @DisplayName("at rest the held disc clears the head, neck, body and front legs")
  void clearsTheBodyAtRest(final UnleashedDogBreed rig, final float dogScale) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final RigProfile profile = BUNDLED.profileOf(rig);
    final Matrix4f disc = placedAsTheLayerDrawsIt(model, gripOf(rig, model), dogScale);

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
                        (float) (Math.cos(angle) * radius),
                        0.0f,
                        (float) (Math.sin(angle) * radius)))
                .div(dogScale);
        for (final CubeGeometry.Bounds cube : cubes) {
          assertFalse(inside(point, cube), rig + " disc point " + point + " inside a body cube");
        }
      }
    }
  }

  private static float outOf(final UnleashedDogBreed rig) {
    return Math.abs(rig.renderTransforms().bodyYawOffsetDegrees()) == 180.0f ? 1.0f : -1.0f;
  }

  private static MouthGrip gripOf(final UnleashedDogBreed rig, final BakedGeoModel model) {
    return MouthGrip.of(
            model, BUNDLED.profileOf(rig), rig.renderTransforms().bodyYawOffsetDegrees())
        .orElseThrow();
  }

  private static Matrix4f placedAsTheLayerDrawsIt(
      final UnleashedDogBreed rig, final float dogScale) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    return placedAsTheLayerDrawsIt(model, gripOf(rig, model), dogScale);
  }

  private static Matrix4f placedAsTheLayerDrawsIt(
      final BakedGeoModel model, final MouthGrip grip, final float dogScale) {
    final GeoBone mouth = model.getBone(grip.boneName()).orElseThrow();
    final MatrixStack stack = new MatrixStack();
    stack.scale(dogScale, dogScale, dogScale);
    stack.peek().getPositionMatrix().mul(LivePose.of(mouth));
    RenderUtil.translateAndRotateMatrixForBone(stack, mouth);
    HeldFrisbee.place(stack, mouth, grip, new Matrix4f());
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
