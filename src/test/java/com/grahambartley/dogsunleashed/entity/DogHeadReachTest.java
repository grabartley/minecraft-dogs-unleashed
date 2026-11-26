package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class DogHeadReachTest {

  private static final Path ASSETS = Path.of("src/client/resources/assets/dogs-unleashed");
  private static final float TOLERANCE = 0.01f;

  private static JsonObject read(final Path path) throws IOException {
    try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      return JsonParser.parseReader(reader).getAsJsonObject();
    }
  }

  private static Set<String> names(final JsonObject profile, final String slot) {
    final Set<String> names = new HashSet<>();
    if (profile.has(slot)) {
      profile.getAsJsonArray(slot).forEach(name -> names.add(name.getAsString()));
    }
    return names;
  }

  private static float[] bounds(
      final JsonArray bones, final Set<String> names, final float facing) {
    float neck = Float.MAX_VALUE;
    float snout = -Float.MAX_VALUE;
    float minX = Float.MAX_VALUE;
    float maxX = -Float.MAX_VALUE;
    float top = -Float.MAX_VALUE;
    for (final JsonElement element : bones) {
      final JsonObject bone = element.getAsJsonObject();
      if (!names.contains(bone.get("name").getAsString()) || !bone.has("cubes")) {
        continue;
      }
      for (final JsonElement cubeElement : bone.getAsJsonArray("cubes")) {
        final JsonObject cube = cubeElement.getAsJsonObject();
        final JsonArray origin = cube.getAsJsonArray("origin");
        final JsonArray size = cube.getAsJsonArray("size");
        final float inflate = cube.has("inflate") ? cube.get("inflate").getAsFloat() : 0.0f;
        final float z0 = (origin.get(2).getAsFloat() - inflate) * facing;
        final float z1 = (origin.get(2).getAsFloat() + size.get(2).getAsFloat() + inflate) * facing;
        neck = Math.min(neck, Math.min(z0, z1));
        snout = Math.max(snout, Math.max(z0, z1));
        minX = Math.min(minX, origin.get(0).getAsFloat() - inflate);
        maxX = Math.max(maxX, origin.get(0).getAsFloat() + size.get(0).getAsFloat() + inflate);
        top = Math.max(top, origin.get(1).getAsFloat() + size.get(1).getAsFloat() + inflate);
      }
    }
    return new float[] {neck, snout, (maxX - minX) / 2.0f, top};
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = UnleashedDogBreed.class,
      names = "CROSS_BREED",
      mode = EnumSource.Mode.EXCLUDE)
  @DisplayName("each rig's head reach matches its shipped geometry")
  void headReachMatchesTheRig(final UnleashedDogBreed rig) throws IOException {
    final JsonArray bones =
        read(ASSETS.resolve("geo/" + rig.serializedId() + ".geo.json"))
            .getAsJsonArray("minecraft:geometry")
            .get(0)
            .getAsJsonObject()
            .getAsJsonArray("bones");
    final JsonObject profile = read(ASSETS.resolve("rig_profiles/" + rig.serializedId() + ".json"));
    final float facing = rig.renderTransforms().bodyYawOffsetDegrees() == 180.0f ? 1.0f : -1.0f;
    final Set<String> head = names(profile, "head");
    final Set<String> headAndMouth = new HashSet<>(head);
    headAndMouth.addAll(names(profile, "mouth"));
    final float[] skull = bounds(bones, head, facing);
    final float[] withMouth = bounds(bones, headAndMouth, facing);
    final DogHeadReach reach = DogHeadReach.rigUnits(rig);
    assertEquals(skull[0], reach.neck(), TOLERANCE, "neck");
    assertEquals(withMouth[1], reach.snout(), TOLERANCE, "snout");
    assertEquals(skull[2], reach.halfWidth(), TOLERANCE, "half width");
    assertEquals(skull[3], reach.top(), TOLERANCE, "top");
  }

  @ParameterizedTest(name = "factor {0}")
  @CsvSource({"0.5", "0.09375", "2"})
  @DisplayName("scaling a head reach scales every measure")
  void scalingScalesEverything(final float factor) {
    final DogHeadReach reach = new DogHeadReach(1.0f, 2.0f, 3.0f, 4.0f).scaled(factor);
    assertEquals(factor, reach.neck(), 1.0e-6f);
    assertEquals(2.0f * factor, reach.snout(), 1.0e-6f);
    assertEquals(3.0f * factor, reach.halfWidth(), 1.0e-6f);
    assertEquals(4.0f * factor, reach.top(), 1.0e-6f);
  }
}
