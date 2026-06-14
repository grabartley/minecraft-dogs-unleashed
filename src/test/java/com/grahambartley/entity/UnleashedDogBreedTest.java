package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class UnleashedDogBreedTest {

  static Stream<Arguments> serializedIdAliases() {
    return Stream.of(
        Arguments.of("husky", UnleashedDogBreed.HUSKY),
        Arguments.of("dachshund", UnleashedDogBreed.DACHSHUND),
        Arguments.of("beagle", UnleashedDogBreed.BEAGLE),
        Arguments.of("goldenretriever", UnleashedDogBreed.GOLDEN_RETRIEVER),
        Arguments.of("golden_retriever", UnleashedDogBreed.GOLDEN_RETRIEVER),
        Arguments.of("shibainu", UnleashedDogBreed.SHIBA_INU),
        Arguments.of("shiba_inu", UnleashedDogBreed.SHIBA_INU));
  }

  @ParameterizedTest(name = "\"{0}\" -> {1}")
  @MethodSource("serializedIdAliases")
  @DisplayName("fromSerializedId resolves canonical ids and legacy aliases")
  void fromSerializedIdResolvesAliases(final String id, final UnleashedDogBreed expected) {
    assertEquals(expected, UnleashedDogBreed.fromSerializedId(id));
  }

  @ParameterizedTest(name = "{0} round-trips through its serialized id")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("every breed round-trips through its own serialized id")
  void everyBreedRoundTripsThroughSerializedId(final UnleashedDogBreed breed) {
    assertEquals(breed, UnleashedDogBreed.fromSerializedId(breed.serializedId()));
  }

  @ParameterizedTest(name = "unknown input \"{0}\"")
  @MethodSource("unknownInputs")
  @DisplayName("fromSerializedId falls back to HUSKY for unknown or missing input")
  void fromSerializedIdFallsBackToHusky(final String unknown) {
    assertEquals(UnleashedDogBreed.HUSKY, UnleashedDogBreed.fromSerializedId(unknown));
  }

  static Stream<String> unknownInputs() {
    return Stream.of("???", "", "labrador");
  }

  @ParameterizedTest(name = "unknown input {0}")
  @MethodSource("nullableUnknownInputs")
  @DisplayName("fromSerializedIdOrNull returns null for unknown or missing input")
  void fromSerializedIdOrNullReturnsNullForUnknownInput(final String unknown) {
    assertNull(UnleashedDogBreed.fromSerializedIdOrNull(unknown));
  }

  static Stream<Arguments> nullableUnknownInputs() {
    return Stream.of(Arguments.of("???"), Arguments.of(""), Arguments.of((Object) null));
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("every breed declares a non-empty mouth-anchor bone name")
  void everyBreedDeclaresMouthAnchorBoneName(final UnleashedDogBreed breed) {
    final String anchor = breed.mouthAnchorBoneName();
    assertNotNull(anchor, "mouth anchor for " + breed);
    assertFalse(anchor.isEmpty(), "mouth anchor for " + breed + " must be non-empty");
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("every breed declares carry profiles for tennis ball, stick, and frisbee")
  void everyBreedDeclaresCarryProfilesForEveryFetchItem(final UnleashedDogBreed breed) {
    final FetchCarryProfiles profiles = breed.fetchCarryProfiles();
    assertNotNull(profiles, "fetchCarryProfiles for " + breed);
    assertNotNull(profiles.tennisBall(), "tennis ball profile for " + breed);
    assertNotNull(profiles.stick(), "stick profile for " + breed);
    assertNotNull(profiles.frisbee(), "frisbee profile for " + breed);
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("every carry profile has a positive scale and finite offsets")
  void everyCarryProfileHasPositiveScaleAndFiniteOffsets(final UnleashedDogBreed breed) {
    final FetchCarryProfiles profiles = breed.fetchCarryProfiles();
    for (final CarryProfile profile :
        List.of(profiles.tennisBall(), profiles.stick(), profiles.frisbee())) {
      assertTrue(profile.scale() > 0f, "scale must be positive: " + breed + " " + profile);
      assertTrue(
          Double.isFinite(profile.verticalOffset()),
          "vertical offset must be finite: " + breed + " " + profile);
      assertTrue(
          Double.isFinite(profile.forwardOffset()),
          "forward offset must be finite: " + breed + " " + profile);
    }
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName(
      "frisbee carry scale exceeds tennis ball carry scale to compensate for the smaller frisbee"
          + " GROUND scale")
  void frisbeeCarryScaleExceedsTennisBallCarryScale(final UnleashedDogBreed breed) {
    final FetchCarryProfiles profiles = breed.fetchCarryProfiles();
    assertTrue(
        profiles.frisbee().scale() > profiles.tennisBall().scale(),
        "frisbee carry scale must exceed tennis ball carry scale for breed="
            + breed
            + ": frisbee="
            + profiles.frisbee().scale()
            + " tennisBall="
            + profiles.tennisBall().scale());
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("every breed declares spawn-egg colors that fit in a 24-bit channel")
  void everyBreedSpawnEggColorsFitIn24Bits(final UnleashedDogBreed breed) {
    final UnleashedDogBreed.SpawnEggColors colors = breed.spawnEggColors();
    assertTrue(colors.primary() >= 0x000000 && colors.primary() <= 0xFFFFFF);
    assertTrue(colors.secondary() >= 0x000000 && colors.secondary() <= 0xFFFFFF);
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("every breed declares positive entity dimensions")
  void everyBreedEntityDimensionsArePositive(final UnleashedDogBreed breed) {
    final UnleashedDogBreed.Dimensions dimensions = breed.dimensions();
    assertTrue(dimensions.width() > 0f, "width for " + breed);
    assertTrue(dimensions.height() > 0f, "height for " + breed);
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("every breed declares a coherent spawn configuration")
  void everyBreedSpawnConfigurationIsCoherent(final UnleashedDogBreed breed) {
    final UnleashedDogBreed.SpawnSettings spawn = breed.spawnSettings();
    assertTrue(spawn.weight() >= 1 && spawn.weight() <= 100, "weight for " + breed);
    assertTrue(spawn.minGroupSize() >= 1, "minGroupSize for " + breed);
    assertTrue(spawn.maxGroupSize() >= spawn.minGroupSize(), "maxGroupSize >= minGroupSize");
    assertTrue(spawn.biomes().length > 0, "every breed must have at least one biome");
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("derived asset paths embed the breed serialized id")
  void derivedAssetPathsEmbedSerializedId(final UnleashedDogBreed breed) {
    final String id = breed.serializedId();
    assertEquals("entity.dogs-unleashed." + id, breed.translationKey());
    assertEquals("animations/" + id + ".animation.json", breed.animationPath());
    assertEquals("textures/entity/" + id + ".png", breed.defaultTexturePath());
  }
}
