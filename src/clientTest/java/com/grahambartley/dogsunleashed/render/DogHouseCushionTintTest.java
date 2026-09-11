package com.grahambartley.dogsunleashed.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class DogHouseCushionTintTest {

  private static final int INHERITED = 0xFFFFFFFF;

  static Stream<String> nonCushionBones() {
    return Stream.of("bb_main", "roof", "cushion_frame", "Cushion", "");
  }

  @ParameterizedTest
  @MethodSource("nonCushionBones")
  void bonesOtherThanTheCushionKeepTheirInheritedTint(final String boneName) {
    assertEquals(INHERITED, DogHouseCushionTint.forBone(boneName, DyeColor.RED, INHERITED));
  }

  @ParameterizedTest
  @EnumSource(DyeColor.class)
  void theCushionTakesTheDyeColour(final DyeColor color) {
    final int tint =
        DogHouseCushionTint.forBone(DogHouseCushionTint.CUSHION_BONE, color, INHERITED);
    assertEquals(color.getEntityColor() & 0x00FFFFFF, tint & 0x00FFFFFF);
  }

  @ParameterizedTest
  @EnumSource(DyeColor.class)
  void everyCushionTintIsFullyOpaque(final DyeColor color) {
    final int tint =
        DogHouseCushionTint.forBone(DogHouseCushionTint.CUSHION_BONE, color, INHERITED);
    assertEquals(0xFF, tint >>> 24);
  }

  @Test
  void everyDyeGivesTheCushionADistinctTint() {
    final long distinct =
        Stream.of(DyeColor.values())
            .map(
                color ->
                    DogHouseCushionTint.forBone(DogHouseCushionTint.CUSHION_BONE, color, INHERITED))
            .distinct()
            .count();
    assertEquals(DyeColor.values().length, distinct);
  }

  @Test
  void theCushionIgnoresTheTintItInherited() {
    assertNotEquals(
        DogHouseCushionTint.forBone(DogHouseCushionTint.CUSHION_BONE, DyeColor.RED, INHERITED),
        DogHouseCushionTint.forBone(DogHouseCushionTint.CUSHION_BONE, DyeColor.BLUE, INHERITED));
    assertEquals(
        DogHouseCushionTint.forBone(DogHouseCushionTint.CUSHION_BONE, DyeColor.RED, INHERITED),
        DogHouseCushionTint.forBone(DogHouseCushionTint.CUSHION_BONE, DyeColor.RED, 0xFF00FF00));
  }

  @Test
  void theShippedModelCarriesTheCushionBoneTheTintLooksFor() throws Exception {
    final JsonObject geo;
    try (InputStream in =
        DogHouseCushionTintTest.class.getResourceAsStream(
            "/assets/dogs-unleashed/geo/dog_house.geo.json")) {
      assertNotNull(in, "dog_house.geo.json is not on the client resources classpath");
      geo =
          JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
              .getAsJsonObject();
    }

    final Set<String> boneNames = new HashSet<>();
    geo.getAsJsonArray("minecraft:geometry")
        .get(0)
        .getAsJsonObject()
        .getAsJsonArray("bones")
        .forEach(bone -> boneNames.add(bone.getAsJsonObject().get("name").getAsString()));

    assertTrue(
        boneNames.contains(DogHouseCushionTint.CUSHION_BONE),
        "expected a bone named '" + DogHouseCushionTint.CUSHION_BONE + "' but found " + boneNames);
    assertTrue(
        boneNames.size() > 1,
        "the cushion shares its bone with the rest of the house, so dyeing it would dye everything");
  }
}
