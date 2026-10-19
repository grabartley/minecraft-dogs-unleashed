package com.grahambartley.dogsunleashed.render.cosmetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import com.grahambartley.dogsunleashed.render.rig.LivePose;
import com.grahambartley.dogsunleashed.render.rig.PoseClearance;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import com.grahambartley.dogsunleashed.render.rig.SlotBounds;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;

class MobHeadFitTest {

  private static final float EPSILON = 1.0e-4f;
  private static final int SAMPLES = 8;
  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);

  static Stream<UnleashedDogBreed> rigs() {
    return Arrays.stream(UnleashedDogBreed.values())
        .filter(rig -> rig != UnleashedDogBreed.CROSS_BREED);
  }

  private static MobHeadFit fitOf(final BakedGeoModel model, final UnleashedDogBreed rig) {
    return MobHeadFit.of(model, BUNDLED.profileOf(rig)).orElseThrow();
  }

  private static CubeGeometry.Bounds headOf(
      final BakedGeoModel model, final UnleashedDogBreed rig) {
    final Set<String> bones = new HashSet<>(BUNDLED.profileOf(rig).bonesIn(Slot.HEAD));
    bones.addAll(BUNDLED.profileOf(rig).bonesIn(Slot.MOUTH));
    return SlotBounds.of(model, bones);
  }

  private static Set<String> earsOf(final RigProfile profile) {
    final Set<String> ears = new HashSet<>(profile.bonesIn(Slot.EAR_LEFT));
    ears.addAll(profile.bonesIn(Slot.EAR_RIGHT));
    return ears;
  }

  private static CubeGeometry.Bounds box(
      final float minX,
      final float minY,
      final float minZ,
      final float maxX,
      final float maxY,
      final float maxZ) {
    final CubeGeometry.Bounds bounds = new CubeGeometry.Bounds();
    bounds.include(new Vector3f(minX, minY, minZ));
    bounds.include(new Vector3f(maxX, maxY, maxZ));
    return bounds;
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("the mob head's face spans the head's width or height, plus vanilla's margin")
  void edgeComesFromTheHeadBones(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final Vector3f size = sizeOf(headOf(model, rig));

    assertEquals(1.1875f * Math.max(size.x, size.y), fitOf(model, rig).edge(), EPSILON);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("the mob head reaches back over the muzzle and is never shallower than a cube")
  void depthCoversTheMuzzle(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final MobHeadFit fit = fitOf(model, rig);

    assertEquals(
        Math.max(fit.edge(), 1.1875f * sizeOf(headOf(model, rig)).z), fit.depth(), EPSILON);
    assertTrue(fit.depthStretch() >= 1.0f);
  }

  private static Vector3f sizeOf(final CubeGeometry.Bounds bounds) {
    return new Vector3f(bounds.max()).sub(bounds.min());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("at rest the mob head is centred on the dog's head and muzzle")
  void centredOnTheHead(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final MobHeadFit fit = fitOf(model, rig);
    final Vector3f centre = headOf(model, rig).center();
    final Vector3f placed =
        LivePose.of(model.getBone(fit.boneName()).orElseThrow())
            .translate(fit.base())
            .rotate(fit.restRotation().conjugate())
            .transformPosition(new Vector3f(0.0f, fit.edge() * 0.5f, 0.0f));

    assertEquals(centre.x, placed.x, EPSILON);
    assertEquals(centre.y, placed.y, EPSILON);
    assertEquals(centre.z, placed.z, EPSILON);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("the mob head is placed from a bone in the head role")
  void placedFromAHeadBone(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    assertTrue(BUNDLED.profileOf(rig).bonesIn(Slot.HEAD).contains(fitOf(model, rig).boneName()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("a mob head only hides ears")
  void hidesOnlyEars(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    assertTrue(earsOf(BUNDLED.profileOf(rig)).containsAll(fitOf(model, rig).hiddenEars()));
  }

  @Test
  @DisplayName("boxes that share space overlap and boxes that only touch do not")
  void overlapRule() {
    final CubeGeometry.Bounds cube = box(0, 0, 0, 1, 1, 1);

    assertTrue(MobHeadFit.overlaps(cube, box(0.5f, 0.5f, 0.5f, 2, 2, 2)));
    assertFalse(MobHeadFit.overlaps(cube, box(1, 0, 0, 2, 1, 1)));
    assertFalse(MobHeadFit.overlaps(cube, box(0, 2, 0, 1, 3, 1)));
    assertFalse(MobHeadFit.overlaps(cube, new CubeGeometry.Bounds()));
  }

  @Test
  @DisplayName("the box stands on its base, centred over it and stretched front to back")
  void boxSitsOnItsBase() {
    final CubeGeometry.Bounds placed = MobHeadFit.boxOf(new Vector3f(1, 2, 3), 2.0f, 4.0f);

    assertEquals(new Vector3f(0, 2, 1), placed.min());
    assertEquals(new Vector3f(2, 4, 5), placed.max());
  }

  @Test
  @DisplayName("the base sits half an edge below the head's centre")
  void baseUnderTheCentre() {
    assertEquals(new Vector3f(1, 0, 1), MobHeadFit.baseOf(box(0, 1, 0, 2, 3, 2), 4.0f));
  }

  static Stream<Arguments> rigClips() {
    final List<Arguments> cases = new ArrayList<>();
    rigs()
        .forEach(
            rig -> clipsOf(rig.serializedId()).forEach(clip -> cases.add(Arguments.of(rig, clip))));
    return cases.stream();
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("rigClips")
  @DisplayName("a mob head stays clear of every ear it leaves showing, in every clip")
  void clearsShownEars(final UnleashedDogBreed rig, final String clip) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final RigProfile profile = BUNDLED.profileOf(rig);
    final MobHeadFit fit = fitOf(model, rig);
    ShippedRigs.pose(model, rig.serializedId(), clip, name -> model.getBone(name).isPresent());
    final Matrix4f headToModel =
        LivePose.of(model.getBone(fit.boneName()).orElseThrow())
            .translate(fit.base())
            .rotate(fit.restRotation().conjugate());
    final Set<String> shownEars = earsOf(profile);
    shownEars.removeAll(fit.hiddenEars());
    final List<PoseClearance.Box> earBoxes = PoseClearance.liveBoxesOf(model, shownEars);

    for (final Vector3f sample : surfaceOf(fit.edge(), fit.depth())) {
      final Vector3f point = headToModel.transformPosition(sample);
      for (final PoseClearance.Box ear : earBoxes) {
        assertFalse(ear.contains(point), rig + " " + clip + " mob head touches an ear");
      }
    }
  }

  private static List<Vector3f> surfaceOf(final float edge, final float depth) {
    final List<Vector3f> samples = new ArrayList<>();
    final float half = edge * 0.5f;
    final float halfDepth = depth * 0.5f;
    for (int i = 0; i <= SAMPLES; i++) {
      for (int j = 0; j <= SAMPLES; j++) {
        final float across = -half + edge * i / SAMPLES;
        final float along = -halfDepth + depth * j / SAMPLES;
        final float upJ = edge * j / SAMPLES;
        final float upI = edge * i / SAMPLES;
        samples.add(new Vector3f(across, 0.0f, along));
        samples.add(new Vector3f(across, edge, along));
        samples.add(new Vector3f(-half, upI, along));
        samples.add(new Vector3f(half, upI, along));
        samples.add(new Vector3f(across, upJ, -halfDepth));
        samples.add(new Vector3f(across, upJ, halfDepth));
      }
    }
    return samples;
  }

  private static List<String> clipsOf(final String rig) {
    try (InputStreamReader reader =
        new InputStreamReader(
            Objects.requireNonNull(
                MobHeadFitTest.class.getResourceAsStream(
                    "/assets/dogs-unleashed/animations/" + rig + ".animation.json")),
            StandardCharsets.UTF_8)) {
      final JsonObject animations =
          JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("animations");
      return List.copyOf(animations.keySet());
    } catch (final IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }
}
