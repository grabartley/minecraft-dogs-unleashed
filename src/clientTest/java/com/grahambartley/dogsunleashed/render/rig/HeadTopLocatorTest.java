package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import com.grahambartley.dogsunleashed.render.cosmetic.CosmeticFit;
import com.grahambartley.dogsunleashed.render.rig.HeadTopLocator.HeadTop;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
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

class HeadTopLocatorTest {

  private static final float EPSILON = 1.0e-5f;
  private static final int SAMPLES = 6;
  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);

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

  @Test
  @DisplayName("with no ears above it the whole head top is clear")
  void noEarsLeavesWholeTop() {
    final HeadTopLocator.Patch patch =
        HeadTopLocator.clearPatch(box(-2, 0, -3, 2, 1, 1), List.of());

    assertEquals(0.0f, patch.centreX(), EPSILON);
    assertEquals(-1.0f, patch.centreZ(), EPSILON);
    assertEquals(4.0f, patch.size(), EPSILON);
  }

  @Test
  @DisplayName("ears at the sides leave the gap between them")
  void wideSetEarsLeaveTheMiddle() {
    final HeadTopLocator.Patch patch =
        HeadTopLocator.clearPatch(
            box(-3, 0, -2, 3, 1, 2),
            List.of(box(-3, 0.5f, -2, -1.5f, 3, 2), box(1.5f, 0.5f, -2, 3, 3, 2)));

    assertEquals(0.0f, patch.centreX(), EPSILON);
    assertEquals(3.0f, patch.size(), EPSILON);
  }

  @Test
  @DisplayName("thin ears that meet in the middle leave room in front of them")
  void closeSetThinEarsLeaveTheFront() {
    final HeadTopLocator.Patch patch =
        HeadTopLocator.clearPatch(
            box(-2, 0, -3, 2, 1, 1),
            List.of(box(-2, 0.5f, -2, -0.1f, 3, -1.5f), box(0.1f, 0.5f, -2, 2, 3, -1.5f)));

    final float size = 1.0f / (1.0f - HeadTopLocator.OVERHANG);
    assertEquals(size, patch.size(), EPSILON);
    assertEquals(-2.0f - size * 0.5f, patch.centreZ(), EPSILON);
  }

  @Test
  @DisplayName("a point inside a quad is covered and one outside is not")
  void coversInsideOnly() {
    final Vector3f[] square = {
      new Vector3f(0, 0, 0), new Vector3f(1, 0, 0), new Vector3f(1, 0, 1), new Vector3f(0, 0, 1)
    };

    assertTrue(HeadTopLocator.coversXz(square, 0.5f, 0.5f));
    assertFalse(HeadTopLocator.coversXz(square, 1.5f, 0.5f));
  }

  static Stream<UnleashedDogBreed> rigs() {
    return Arrays.stream(UnleashedDogBreed.values())
        .filter(rig -> rig != UnleashedDogBreed.CROSS_BREED);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("every rig has a head top with room for a hat")
  void everyRigHasRoom(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final HeadTop top = HeadTopLocator.locate(model, BUNDLED.profileOf(rig)).orElseThrow();

    assertTrue(BUNDLED.profileOf(rig).bonesIn(Slot.HEAD).contains(top.boneName()));
    assertTrue(top.clearWidth() > top.headWidth() * 0.25f, rig + " left " + top.clearWidth());
  }

  static Stream<Arguments> rigClipsAndHats() {
    final List<Arguments> cases = new ArrayList<>();
    rigs()
        .forEach(
            rig -> {
              for (final String clip : clipsOf(rig.serializedId())) {
                for (final DogCosmetic hat :
                    List.of(DogCosmetic.PARTY_HAT, DogCosmetic.STRAW_HAT)) {
                  cases.add(Arguments.of(rig, clip, hat));
                }
              }
            });
    return cases.stream();
  }

  @ParameterizedTest(name = "{0} {1} {2}")
  @MethodSource("rigClipsAndHats")
  @DisplayName("a hat stays clear of the ears in every clip")
  void hatClearsEars(final UnleashedDogBreed rig, final String clip, final DogCosmetic hat) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final RigProfile profile = BUNDLED.profileOf(rig);
    final HeadTop top = HeadTopLocator.locate(model, profile).orElseThrow();
    ShippedRigs.pose(model, rig.serializedId(), clip, name -> model.getBone(name).isPresent());
    final BakedGeoModel hatModel = ShippedRigs.model(hat.itemId());
    final float scale =
        CosmeticFit.hatWidth(hat, top.headWidth(), top.clearWidth()) / widthOf(hatModel);
    final Matrix4f hatToModel =
        LivePose.of(model.getBone(top.boneName()).orElseThrow())
            .translate(top.point())
            .rotate(new Quaternionf(top.restRotation()).conjugate())
            .scale(scale);
    final Set<String> ears = new HashSet<>(profile.bonesIn(Slot.EAR_LEFT));
    ears.addAll(profile.bonesIn(Slot.EAR_RIGHT));
    final List<PoseClearance.Box> earBoxes = PoseClearance.liveBoxesOf(model, ears);

    for (final Vector3f sample : samplesOf(hatModel)) {
      final Vector3f point = hatToModel.transformPosition(new Vector3f(sample));
      for (final PoseClearance.Box ear : earBoxes) {
        assertFalse(ear.contains(point), rig + " " + clip + " " + hat + " touches an ear");
      }
    }
  }

  private static float widthOf(final BakedGeoModel model) {
    final CubeGeometry.Bounds bounds = new CubeGeometry.Bounds();
    for (final GeoBone bone : model.topLevelBones()) {
      final CubeGeometry.Bounds part = CubeGeometry.posedBoundsOf(bone);
      bounds.include(part.min());
      bounds.include(part.max());
    }
    return bounds.max().x - bounds.min().x;
  }

  private static List<Vector3f> samplesOf(final BakedGeoModel model) {
    final List<Vector3f> samples = new ArrayList<>();
    for (final GeoBone bone : model.topLevelBones()) {
      for (final GeoCube cube : bone.getCubes()) {
        for (final GeoQuad quad : cube.quads()) {
          if (quad == null) {
            continue;
          }
          final Vector3f a = quad.vertices()[0].position();
          final Vector3f b = quad.vertices()[1].position();
          final Vector3f d = quad.vertices()[3].position();
          for (int i = 0; i <= SAMPLES; i++) {
            for (int j = 0; j <= SAMPLES; j++) {
              final float s = (float) i / SAMPLES;
              final float t = (float) j / SAMPLES;
              samples.add(
                  new Vector3f(a)
                      .add(new Vector3f(b).sub(a).mul(s))
                      .add(new Vector3f(d).sub(a).mul(t)));
            }
          }
        }
      }
    }
    return samples;
  }

  private static List<String> clipsOf(final String rig) {
    try (InputStreamReader reader =
        new InputStreamReader(
            Objects.requireNonNull(
                HeadTopLocatorTest.class.getResourceAsStream(
                    "/assets/dogs-unleashed/animations/" + rig + ".animation.json")),
            StandardCharsets.UTF_8)) {
      final JsonObject animations =
          JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("animations");
      return List.copyOf(animations.keySet());
    } catch (final java.io.IOException exception) {
      throw new java.io.UncheckedIOException(exception);
    }
  }
}
