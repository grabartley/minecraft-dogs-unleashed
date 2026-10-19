package com.grahambartley.dogsunleashed.render.cosmetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.item.cosmetic.MobHead;
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
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;

class MobHeadFitTest {

  private static final float EPSILON = 1.0e-4f;
  private static final int SAMPLES = 6;
  private static final float MUZZLE_TOLERANCE = 0.5f / 16.0f;
  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);

  static Stream<UnleashedDogBreed> rigs() {
    return Arrays.stream(UnleashedDogBreed.values())
        .filter(rig -> rig != UnleashedDogBreed.CROSS_BREED);
  }

  static Stream<Arguments> rigsAndHeads() {
    return rigs()
        .flatMap(rig -> Arrays.stream(MobHead.values()).map(head -> Arguments.of(rig, head)));
  }

  private static MobHeadFit fitOf(
      final BakedGeoModel model, final UnleashedDogBreed rig, final MobHead head) {
    return MobHeadFit.of(model, BUNDLED.profileOf(rig), head).orElseThrow();
  }

  private static Set<String> muzzleBones(final RigProfile profile) {
    final Set<String> bones = new HashSet<>(profile.bonesIn(Slot.HEAD));
    bones.addAll(profile.bonesIn(Slot.MOUTH));
    return bones;
  }

  private static Set<String> earsOf(final RigProfile profile) {
    final Set<String> ears = new HashSet<>(profile.bonesIn(Slot.EAR_LEFT));
    ears.addAll(profile.bonesIn(Slot.EAR_RIGHT));
    return ears;
  }

  private static Vector3f sizeOf(final CubeGeometry.Bounds bounds) {
    return new Vector3f(bounds.max()).sub(bounds.min());
  }

  private static Matrix4f headToModel(final BakedGeoModel model, final MobHeadFit fit) {
    return LivePose.of(model.getBone(fit.boneName()).orElseThrow())
        .translate(fit.base())
        .rotate(fit.restRotation().conjugate())
        .rotateY(fit.facingYaw());
  }

  private static CubeGeometry.Bounds drawnInHead(final MobHead head, final MobHeadFit fit) {
    return MobHeadShape.placed(head, new Vector3f(), fit.edge(), fit.depthStretch());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("at rest a stretched mob head covers the dog's whole head and muzzle")
  void coversTheMuzzleAtRest(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final MobHeadFit fit = fitOf(model, rig, MobHead.CREEPER);
    final CubeGeometry.Bounds muzzle = SlotBounds.of(model, muzzleBones(BUNDLED.profileOf(rig)));
    final CubeGeometry.Bounds cube = drawnInHead(MobHead.CREEPER, fit);
    final Matrix4f modelToHead = headToModel(model, fit).invert();

    for (final Vector3f corner : cornersOf(muzzle)) {
      final Vector3f local = modelToHead.transformPosition(corner);
      assertTrue(
          insideWithin(cube, local, EPSILON),
          rig + " leaves " + local + " outside " + cube.min() + " " + cube.max());
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName(
      "the mob head's face is as big as the dog's head, with no more than vanilla's margin")
  void faceMatchesTheHead(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final Vector3f size = sizeOf(SlotBounds.of(model, muzzleBones(BUNDLED.profileOf(rig))));
    final float larger = Math.max(size.x, size.y);
    final float edge = fitOf(model, rig, MobHead.ZOMBIE).edge();

    assertTrue(edge >= larger, rig + " face " + edge + " is smaller than the head " + larger);
    assertTrue(edge <= larger * 1.1875f + EPSILON, rig + " face " + edge + " is oversized");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("the dragon head keeps its own length and every other head stretches the same way")
  void onlyTheDragonKeepsItsLength(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());

    assertEquals(1.0f, fitOf(model, rig, MobHead.DRAGON).depthStretch(), EPSILON);
    assertTrue(fitOf(model, rig, MobHead.CREEPER).depthStretch() >= 1.0f);
    assertEquals(
        fitOf(model, rig, MobHead.CREEPER).depthStretch(),
        fitOf(model, rig, MobHead.CARVED_PUMPKIN).depthStretch(),
        EPSILON);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("at rest the mob head is centred on the dog's head and muzzle")
  void centredOnTheHead(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final MobHeadFit fit = fitOf(model, rig, MobHead.SKELETON);
    final Vector3f centre = SlotBounds.of(model, muzzleBones(BUNDLED.profileOf(rig))).center();
    final Vector3f placed =
        headToModel(model, fit).transformPosition(new Vector3f(0.0f, fit.edge() * 0.5f, 0.0f));

    assertEquals(centre.x, placed.x, EPSILON);
    assertEquals(centre.y, placed.y, EPSILON);
    assertEquals(centre.z, placed.z, EPSILON);
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("rigsAndHeads")
  @DisplayName("a mob head is placed from a head bone and only hides ears")
  void placedFromTheHeadAndHidesOnlyEars(final UnleashedDogBreed rig, final MobHead head) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final MobHeadFit fit = fitOf(model, rig, head);

    assertTrue(BUNDLED.profileOf(rig).bonesIn(Slot.HEAD).contains(fit.boneName()));
    assertTrue(earsOf(BUNDLED.profileOf(rig)).containsAll(fit.hiddenEars()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("every mob head faces the way the rig's own model faces")
  void facesTheRigsFront(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final float offset = rig.renderTransforms().bodyYawOffsetDegrees();

    for (final MobHead head : MobHead.values()) {
      assertEquals(
          Math.round(offset / 90.0f) & 3, fitOf(model, rig, head).quarterTurns(), rig + " " + head);
    }
  }

  @Test
  @DisplayName("a head faces whichever side its muzzle sticks out of")
  void facesTheMuzzle() {
    final Vector3f skull = new Vector3f();

    assertEquals(0, MobHeadFit.quarterTurnsToward(skull, new Vector3f(0.1f, 0, -1)));
    assertEquals(1, MobHeadFit.quarterTurnsToward(skull, new Vector3f(-1, 0, 0.1f)));
    assertEquals(2, MobHeadFit.quarterTurnsToward(skull, new Vector3f(-0.1f, 0, 1)));
    assertEquals(3, MobHeadFit.quarterTurnsToward(skull, new Vector3f(1, 0, 0)));
    assertEquals(0, MobHeadFit.quarterTurnsToward(skull, new Vector3f()));
  }

  @Test
  @DisplayName("turning a box half way round puts its front at the back")
  void turnedBoxSwapsFrontAndBack() {
    final CubeGeometry.Bounds box = new CubeGeometry.Bounds();
    box.include(new Vector3f(-1, 0, -3));
    box.include(new Vector3f(1, 2, 1));

    final CubeGeometry.Bounds turned = MobHeadFit.turned(box, 2, new Vector3f(0, 1, 0));

    assertEquals(-1.0f, turned.min().x, EPSILON);
    assertEquals(1.0f, turned.min().y, EPSILON);
    assertEquals(-1.0f, turned.min().z, EPSILON);
    assertEquals(3.0f, turned.max().z, EPSILON);
  }

  @Test
  @DisplayName("the base sits half an edge below the head's centre")
  void baseUnderTheCentre() {
    final CubeGeometry.Bounds head = new CubeGeometry.Bounds();
    head.include(new Vector3f(0, 1, 0));
    head.include(new Vector3f(2, 3, 2));

    assertEquals(new Vector3f(1, 0, 1), MobHeadFit.baseOf(head, 4.0f));
  }

  static Stream<Arguments> rigClipsAndHeads() {
    final List<Arguments> cases = new ArrayList<>();
    rigs()
        .forEach(
            rig ->
                clipsOf(rig.serializedId())
                    .forEach(
                        clip -> {
                          for (final MobHead head : MobHead.values()) {
                            cases.add(Arguments.of(rig, clip, head));
                          }
                        }));
    return cases.stream();
  }

  @ParameterizedTest(name = "{0} {1} {2}")
  @MethodSource("rigClipsAndHeads")
  @DisplayName("a mob head stays clear of every ear it leaves showing, in every clip")
  void clearsShownEars(final UnleashedDogBreed rig, final String clip, final MobHead head) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final RigProfile profile = BUNDLED.profileOf(rig);
    final MobHeadFit fit = fitOf(model, rig, head);
    ShippedRigs.pose(model, rig.serializedId(), clip, name -> model.getBone(name).isPresent());
    final Matrix4f modelToHead = headToModel(model, fit).invert();
    final CubeGeometry.Bounds drawn = drawnInHead(head, fit);
    final Set<String> shownEars = earsOf(profile);
    shownEars.removeAll(fit.hiddenEars());

    for (final PoseClearance.Box ear : PoseClearance.liveBoxesOf(model, shownEars)) {
      final Matrix4f localToModel = new Matrix4f(ear.modelToLocal()).invert();
      for (final Vector3f sample : volumeOf(ear.min(), ear.max())) {
        final Vector3f local =
            modelToHead.transformPosition(localToModel.transformPosition(sample));
        assertFalse(drawn.contains(local), rig + " " + clip + " " + head + " touches an ear");
      }
    }
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
  @DisplayName("a stretched mob head keeps the head and muzzle inside it, in every clip")
  void keepsTheMuzzleInsideInEveryClip(final UnleashedDogBreed rig, final String clip) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final MobHeadFit fit = fitOf(model, rig, MobHead.CREEPER);
    ShippedRigs.pose(model, rig.serializedId(), clip, name -> model.getBone(name).isPresent());
    final Matrix4f modelToHead = headToModel(model, fit).invert();
    final CubeGeometry.Bounds drawn = drawnInHead(MobHead.CREEPER, fit);

    for (final String name : muzzleBones(BUNDLED.profileOf(rig))) {
      final GeoBone bone = model.getBone(name).orElseThrow();
      for (final GeoCube cube : bone.getCubes()) {
        for (final Vector3f corner : ShippedRigs.liveCorners(bone, cube)) {
          final Vector3f local = modelToHead.transformPosition(new Vector3f(corner));
          assertTrue(
              insideWithin(drawn, local, MUZZLE_TOLERANCE),
              rig + " " + clip + " " + name + " pokes out at " + local);
        }
      }
    }
  }

  private static boolean insideWithin(
      final CubeGeometry.Bounds box, final Vector3f point, final float tolerance) {
    final Vector3f min = box.min().sub(tolerance, tolerance, tolerance);
    final Vector3f max = box.max().add(tolerance, tolerance, tolerance);
    return point.x >= min.x
        && point.x <= max.x
        && point.y >= min.y
        && point.y <= max.y
        && point.z >= min.z
        && point.z <= max.z;
  }

  private static List<Vector3f> cornersOf(final CubeGeometry.Bounds bounds) {
    final Vector3f min = bounds.min();
    final Vector3f max = bounds.max();
    final List<Vector3f> corners = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      corners.add(
          new Vector3f(
              (i & 1) == 0 ? min.x : max.x,
              (i & 2) == 0 ? min.y : max.y,
              (i & 4) == 0 ? min.z : max.z));
    }
    return corners;
  }

  private static List<Vector3f> volumeOf(final Vector3f min, final Vector3f max) {
    final List<Vector3f> samples = new ArrayList<>();
    final Vector3f size = new Vector3f(max).sub(min);
    for (int i = 0; i <= SAMPLES; i++) {
      for (int j = 0; j <= SAMPLES; j++) {
        for (int k = 0; k <= SAMPLES; k++) {
          samples.add(
              new Vector3f(
                  min.x + size.x * i / SAMPLES,
                  min.y + size.y * j / SAMPLES,
                  min.z + size.z * k / SAMPLES));
        }
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
