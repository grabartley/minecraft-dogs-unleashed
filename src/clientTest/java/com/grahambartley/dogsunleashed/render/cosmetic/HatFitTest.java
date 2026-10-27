package com.grahambartley.dogsunleashed.render.cosmetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import com.grahambartley.dogsunleashed.render.rig.HeadTopLocator;
import com.grahambartley.dogsunleashed.render.rig.HeadTopLocator.HeadTop;
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

class HatFitTest {

  private static final float EPSILON = 1.0e-6f;
  private static final int SAMPLES = 6;
  private static final List<DogCosmetic> HATS =
      List.of(DogCosmetic.PARTY_HAT, DogCosmetic.STRAW_HAT);
  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);

  private static HatFit fitOf(final UnleashedDogBreed rig, final DogCosmetic hat) {
    return HatFit.of(ShippedRigs.model(rig.serializedId()), BUNDLED.profileOf(rig), hat)
        .orElseThrow();
  }

  private static Set<String> earsOf(final RigProfile profile) {
    final Set<String> ears = new HashSet<>(profile.bonesIn(Slot.EAR_LEFT));
    ears.addAll(profile.bonesIn(Slot.EAR_RIGHT));
    return ears;
  }

  static Stream<UnleashedDogBreed> rigs() {
    return Arrays.stream(UnleashedDogBreed.values())
        .filter(rig -> rig != UnleashedDogBreed.CROSS_BREED);
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

  private static HeadTop topWith(final List<HeadTopLocator.RisingEar> ears) {
    return new HeadTop(
        "head", new Vector3f(), new Quaternionf(), 4.0f, new Vector3f(0, 1, 0), ears);
  }

  @Test
  @DisplayName("an ear under the hat's footprint is hidden and one outside it is kept")
  void earsUnderTheHatAreHidden() {
    final HeadTop top =
        topWith(
            List.of(
                new HeadTopLocator.RisingEar(Set.of("inner"), box(0.5f, 1, -0.5f, 1.5f, 3, 0.5f)),
                new HeadTopLocator.RisingEar(Set.of("outer"), box(2.5f, 1, -0.5f, 3.5f, 3, 0.5f))));

    assertEquals(Set.of("inner"), HatFit.earsUnder(top, 2.0f));
    assertEquals(Set.of("inner", "outer"), HatFit.earsUnder(top, 6.0f));
  }

  @Test
  @DisplayName("a straw hat brim is 1.6 times as wide as the husky's head bones")
  void widthComesFromTheHeadBones() {
    final BakedGeoModel husky = ShippedRigs.model("husky");
    final CubeGeometry.Bounds head =
        SlotBounds.of(husky, BUNDLED.profileOf(UnleashedDogBreed.HUSKY).bonesIn(Slot.HEAD));

    assertEquals(
        1.6f * (head.max().x - head.min().x),
        fitOf(UnleashedDogBreed.HUSKY, DogCosmetic.STRAW_HAT).width(),
        1.0e-4f);
  }

  @Test
  @DisplayName("a head with no rising ears hides nothing")
  void noRisingEarsHidesNothing() {
    assertTrue(HatFit.earsUnder(topWith(List.of()), 10.0f).isEmpty());
  }

  @Test
  @DisplayName("every shipped hat is as deep as it is wide, as the square footprint assumes")
  void hatsAreSquare() {
    for (final DogCosmetic hat : HATS) {
      final CubeGeometry.Bounds bounds = boundsOf(ShippedRigs.model(hat.itemId()));
      assertEquals(
          bounds.max().x - bounds.min().x, bounds.max().z - bounds.min().z, EPSILON, hat.name());
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("a hat only hides ears")
  void hidesOnlyEars(final UnleashedDogBreed rig) {
    for (final DogCosmetic hat : HATS) {
      assertTrue(earsOf(BUNDLED.profileOf(rig)).containsAll(fitOf(rig, hat).hiddenEars()));
    }
  }

  @Test
  @DisplayName("pointed ears that stand under a hat are hidden")
  void pointedEarsAreHidden() {
    for (final UnleashedDogBreed rig :
        List.of(
            UnleashedDogBreed.HUSKY, UnleashedDogBreed.SHIBA_INU, UnleashedDogBreed.DIRE_WOLF)) {
      for (final DogCosmetic hat : HATS) {
        assertEquals(earsOf(BUNDLED.profileOf(rig)), fitOf(rig, hat).hiddenEars(), rig + " " + hat);
      }
    }
  }

  @Test
  @DisplayName("drop ears stay visible under a hat")
  void dropEarsStay() {
    for (final UnleashedDogBreed rig :
        List.of(
            UnleashedDogBreed.GOLDEN_RETRIEVER,
            UnleashedDogBreed.BEAGLE,
            UnleashedDogBreed.DACHSHUND)) {
      for (final DogCosmetic hat : HATS) {
        assertTrue(fitOf(rig, hat).hiddenEars().isEmpty(), rig + " " + hat);
      }
    }
  }

  static Stream<Arguments> rigClipsAndHats() {
    final List<Arguments> cases = new ArrayList<>();
    rigs()
        .forEach(
            rig -> {
              for (final String clip : clipsOf(rig.serializedId())) {
                for (final DogCosmetic hat : HATS) {
                  cases.add(Arguments.of(rig, clip, hat));
                }
              }
            });
    return cases.stream();
  }

  @ParameterizedTest(name = "{0} {1} {2}")
  @MethodSource("rigClipsAndHats")
  @DisplayName("a hat stays clear of every ear it leaves showing, in every clip")
  void hatClearsShownEars(final UnleashedDogBreed rig, final String clip, final DogCosmetic hat) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final RigProfile profile = BUNDLED.profileOf(rig);
    final HatFit fit = HatFit.of(model, profile, hat).orElseThrow();
    ShippedRigs.pose(model, rig.serializedId(), clip, name -> model.getBone(name).isPresent());
    final BakedGeoModel hatModel = ShippedRigs.model(hat.itemId());
    final float scale = fit.width() / widthOf(hatModel);
    final Matrix4f hatToModel =
        LivePose.of(model.getBone(fit.top().boneName()).orElseThrow())
            .translate(fit.top().point())
            .rotate(fit.top().restRotation().conjugate())
            .scale(scale);
    final Set<String> shownEars = earsOf(profile);
    shownEars.removeAll(fit.hiddenEars());
    final List<PoseClearance.Box> earBoxes = PoseClearance.liveBoxesOf(model, shownEars);

    for (final Vector3f sample : samplesOf(hatModel)) {
      final Vector3f point = hatToModel.transformPosition(new Vector3f(sample));
      for (final PoseClearance.Box ear : earBoxes) {
        assertFalse(ear.contains(point), rig + " " + clip + " " + hat + " touches an ear");
      }
    }
  }

  private static float widthOf(final BakedGeoModel model) {
    final CubeGeometry.Bounds bounds = boundsOf(model);
    return bounds.max().x - bounds.min().x;
  }

  private static CubeGeometry.Bounds boundsOf(final BakedGeoModel model) {
    final CubeGeometry.Bounds bounds = new CubeGeometry.Bounds();
    for (final GeoBone bone : model.topLevelBones()) {
      final CubeGeometry.Bounds part = CubeGeometry.posedBoundsOf(bone);
      bounds.include(part.min());
      bounds.include(part.max());
    }
    return bounds;
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
                HatFitTest.class.getResourceAsStream(
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
