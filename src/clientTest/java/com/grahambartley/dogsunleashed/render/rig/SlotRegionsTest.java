package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;

class SlotRegionsTest {

  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);

  private record Rect(int x, int y, int width, int height) {}

  static Stream<Arguments> rigsAndSlots() {
    return Arrays.stream(UnleashedDogBreed.values())
        .filter(rig -> rig != UnleashedDogBreed.CROSS_BREED)
        .flatMap(
            rig ->
                Arrays.stream(Slot.values())
                    .filter(slot -> !BUNDLED.profileOf(rig).bonesIn(slot).isEmpty())
                    .map(slot -> Arguments.of(rig, slot)));
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("rigsAndSlots")
  @DisplayName("every slot region on a shipped rig lies inside the texture atlas")
  void regionsFallInsideTheAtlas(final UnleashedDogBreed rig, final Slot slot) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final List<SlotRegion> regions = SlotRegions.of(model, BUNDLED.profileOf(rig), slot);

    assertFalse(regions.isEmpty(), rig + " " + slot + " has no texture footprint");
    for (final SlotRegion region : regions) {
      assertTrue(region.x() >= 0 && region.y() >= 0, region + " starts off the atlas");
      assertTrue(region.width() > 0 && region.height() > 0, region + " is empty");
      assertTrue(
          region.x() + region.width() <= model.properties().textureWidth(),
          region + " runs off the right of the atlas");
      assertTrue(
          region.y() + region.height() <= model.properties().textureHeight(),
          region + " runs off the bottom of the atlas");
    }
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("rigsAndSlots")
  @DisplayName("every slot's regions are the box UV faces of exactly the cubes the slot names")
  void regionsMatchTheBoxUvOfTheNamedCubes(final UnleashedDogBreed rig, final Slot slot) {
    final RigProfile profile = BUNDLED.profileOf(rig);
    final List<SlotRegion> regions =
        SlotRegions.of(ShippedRigs.model(rig.serializedId()), profile, slot);

    for (final SlotRegion region : regions) {
      assertTrue(profile.bonesIn(slot).contains(region.boneName()), region + " is off the slot");
    }
    assertEquals(
        sorted(boxUvFaces(rig.serializedId(), profile.bonesIn(slot))),
        sorted(regions.stream().map(r -> new Rect(r.x(), r.y(), r.width(), r.height())).toList()));
  }

  @Test
  @DisplayName("the beagle head's six faces cover the box UV laid out from its corner at 0, 40")
  void beagleHeadFacesMatchTheirHandLaidOutBoxUv() {
    final List<SlotRegion> head =
        SlotRegions.of(
            ShippedRigs.model("beagle"), BUNDLED.profileOf(UnleashedDogBreed.BEAGLE), Slot.HEAD);

    assertEquals(
        List.of(
            new Rect(0, 46, 6, 6),
            new Rect(6, 40, 6, 6),
            new Rect(6, 46, 6, 6),
            new Rect(12, 40, 6, 6),
            new Rect(12, 46, 6, 6),
            new Rect(18, 46, 6, 6)),
        sorted(head.stream().map(r -> new Rect(r.x(), r.y(), r.width(), r.height())).toList()));
  }

  @Test
  @DisplayName("a face whose box UV runs past the atlas edge is cut back to the atlas")
  void facesPastTheAtlasEdgeAreClipped() {
    final RigProfile profile = BUNDLED.profileOf(UnleashedDogBreed.SHIBA_INU);
    final BakedGeoModel model = ShippedRigs.model("shibainu");

    final int bottom =
        SlotRegions.of(model, profile, Slot.TAIL).stream()
            .mapToInt(region -> region.y() + region.height())
            .max()
            .orElseThrow();

    assertEquals((int) model.properties().textureHeight(), bottom);
  }

  @Test
  @DisplayName("a face with fractional UV edges covers every texel it touches")
  void fractionalEdgesCoverEveryTouchedTexel() {
    final CubeGeometry.Face face =
        QuadUvTest.unitFace(
            new float[] {3.7f / 64, 8.2f / 64, 8.2f / 64, 3.7f / 64},
            new float[] {10.0f / 64, 10.0f / 64, 12.5f / 64, 12.5f / 64});

    assertEquals(
        Optional.of(new SlotRegion("neck", Direction.NORTH, 3, 10, 6, 3)),
        SlotRegions.regionOf(face, 64, 64));
  }

  @Test
  @DisplayName("a face with no texture area has no region")
  void flatFaceHasNoRegion() {
    final CubeGeometry.Face face =
        QuadUvTest.unitFace(
            new float[] {0.25f, 0.25f, 0.25f, 0.25f}, new float[] {0.0f, 0.0f, 0.5f, 0.5f});

    assertTrue(SlotRegions.regionOf(face, 64, 64).isEmpty());
  }

  @Test
  @DisplayName("a bone the rig does not have adds no region")
  void missingBoneAddsNothing() {
    final RigProfile profile =
        new RigProfile(Map.of(Slot.EYES, Set.of("no_such_bone")), Map.of(), List.of());

    assertTrue(SlotRegions.of(ShippedRigs.model("beagle"), profile, Slot.EYES).isEmpty());
  }

  @Test
  @DisplayName("a rig's slot regions are worked out once and then reused")
  void regionsAreCachedPerRig() {
    final AtomicInteger loads = new AtomicInteger();
    final SlotRegions regions = counting(loads, Optional.of(ShippedRigs.model("beagle")));

    final List<SlotRegion> first = regions.regionsOf(UnleashedDogBreed.BEAGLE, Slot.HEAD);
    final List<SlotRegion> second = regions.regionsOf(UnleashedDogBreed.BEAGLE, Slot.HEAD);

    assertSame(first, second);
    assertEquals(1, loads.get());
  }

  @Test
  @DisplayName("clearing the cache works the regions out again from the reloaded rig")
  void clearForgetsCachedRegions() {
    final AtomicInteger loads = new AtomicInteger();
    final SlotRegions regions = counting(loads, Optional.of(ShippedRigs.model("beagle")));

    final List<SlotRegion> before = regions.regionsOf(UnleashedDogBreed.BEAGLE, Slot.HEAD);
    regions.clear();
    final List<SlotRegion> after = regions.regionsOf(UnleashedDogBreed.BEAGLE, Slot.HEAD);

    assertNotSame(before, after);
    assertEquals(before, after);
    assertEquals(2, loads.get());
  }

  @Test
  @DisplayName("a rig whose model has not loaded yet has no regions and is asked again later")
  void unloadedModelIsNotCached() {
    final AtomicInteger loads = new AtomicInteger();
    final SlotRegions regions = counting(loads, Optional.empty());

    assertTrue(regions.regionsOf(UnleashedDogBreed.BEAGLE, Slot.HEAD).isEmpty());
    assertTrue(regions.regionsOf(UnleashedDogBreed.BEAGLE, Slot.HEAD).isEmpty());
    assertEquals(2, loads.get());
  }

  private static SlotRegions counting(
      final AtomicInteger loads, final Optional<BakedGeoModel> model) {
    return new SlotRegions(
        rig -> {
          loads.incrementAndGet();
          return model;
        },
        BUNDLED::profileOf);
  }

  private static List<Rect> boxUvFaces(final String rig, final Set<String> boneNames) {
    final JsonObject geometry = geometry(rig);
    final int atlasWidth = geometry.getAsJsonObject("description").get("texture_width").getAsInt();
    final int atlasHeight =
        geometry.getAsJsonObject("description").get("texture_height").getAsInt();
    final List<Rect> faces = new ArrayList<>();
    for (final JsonElement element : geometry.getAsJsonArray("bones")) {
      final JsonObject bone = element.getAsJsonObject();
      if (!boneNames.contains(bone.get("name").getAsString()) || !bone.has("cubes")) {
        continue;
      }
      for (final JsonElement cubeElement : bone.getAsJsonArray("cubes")) {
        final JsonObject cube = cubeElement.getAsJsonObject();
        final JsonArray size = cube.getAsJsonArray("size");
        final JsonArray uv = cube.getAsJsonArray("uv");
        final float w = (float) Math.floor(size.get(0).getAsFloat());
        final float h = (float) Math.floor(size.get(1).getAsFloat());
        final float d = (float) Math.floor(size.get(2).getAsFloat());
        final float u = uv.get(0).getAsFloat();
        final float v = uv.get(1).getAsFloat();
        addFace(faces, u + d, v + d, w, h, atlasWidth, atlasHeight);
        addFace(faces, u + d + w + d, v + d, w, h, atlasWidth, atlasHeight);
        addFace(faces, u, v + d, d, h, atlasWidth, atlasHeight);
        addFace(faces, u + d + w, v + d, d, h, atlasWidth, atlasHeight);
        addFace(faces, u + d, v, w, d, atlasWidth, atlasHeight);
        addFace(faces, u + d + w, v, w, d, atlasWidth, atlasHeight);
      }
    }
    return faces;
  }

  private static void addFace(
      final List<Rect> faces,
      final float u,
      final float v,
      final float width,
      final float height,
      final int atlasWidth,
      final int atlasHeight) {
    final int x = Math.max(0, (int) u);
    final int y = Math.max(0, (int) v);
    final int right = Math.min(atlasWidth - 1, (int) (u + width) - 1);
    final int bottom = Math.min(atlasHeight - 1, (int) (v + height) - 1);
    if (right >= x && bottom >= y) {
      faces.add(new Rect(x, y, right - x + 1, bottom - y + 1));
    }
  }

  private static List<Rect> sorted(final List<Rect> rects) {
    return rects.stream()
        .sorted(
            Comparator.comparingInt(Rect::x)
                .thenComparingInt(Rect::y)
                .thenComparingInt(Rect::width)
                .thenComparingInt(Rect::height))
        .toList();
  }

  private static JsonObject geometry(final String rig) {
    final InputStream stream =
        SlotRegionsTest.class.getResourceAsStream(
            "/assets/dogs-unleashed/geo/" + rig + ".geo.json");
    return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
        .getAsJsonObject()
        .getAsJsonArray("minecraft:geometry")
        .get(0)
        .getAsJsonObject();
  }
}
