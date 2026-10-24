package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeKeys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class UnleashedDogBreedTest {

  static Stream<Arguments> serializedIdAliases() {
    return Stream.of(
        Arguments.of("husky", UnleashedDogBreed.HUSKY),
        Arguments.of("dachshund", UnleashedDogBreed.DACHSHUND),
        Arguments.of("beagle", UnleashedDogBreed.BEAGLE),
        Arguments.of("goldenretriever", UnleashedDogBreed.GOLDEN_RETRIEVER),
        Arguments.of("golden_retriever", UnleashedDogBreed.GOLDEN_RETRIEVER),
        Arguments.of("shibainu", UnleashedDogBreed.SHIBA_INU),
        Arguments.of("shiba_inu", UnleashedDogBreed.SHIBA_INU),
        Arguments.of("direwolf", UnleashedDogBreed.DIRE_WOLF),
        Arguments.of("dire_wolf", UnleashedDogBreed.DIRE_WOLF),
        Arguments.of("pinscher", UnleashedDogBreed.PINSCHER),
        Arguments.of("crossbreed", UnleashedDogBreed.CROSS_BREED),
        Arguments.of("cross_breed", UnleashedDogBreed.CROSS_BREED));
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
  @MethodSource("rigBreeds")
  @DisplayName("every rig breed declares carry profiles for the tennis ball and the stick")
  void everyRigBreedDeclaresCarryProfilesForEveryFetchItem(final UnleashedDogBreed breed) {
    final FetchCarryProfiles profiles = breed.fetchCarryProfiles().orElseThrow();
    assertNotNull(profiles.tennisBall(), "tennis ball profile for " + breed);
    assertNotNull(profiles.stick(), "stick profile for " + breed);
  }

  static Stream<UnleashedDogBreed> rigBreeds() {
    return Arrays.stream(UnleashedDogBreed.values())
        .filter(breed -> breed != UnleashedDogBreed.CROSS_BREED);
  }

  @Test
  @DisplayName("the cross-breed declares no carry profiles because it renders with another rig")
  void crossBreedDeclaresNoCarryProfiles() {
    assertTrue(UnleashedDogBreed.CROSS_BREED.fetchCarryProfiles().isEmpty());
    assertTrue(UnleashedDogBreed.CROSS_BREED.carryProfileFor(null).isEmpty());
  }

  @ParameterizedTest
  @MethodSource("rigBreeds")
  @DisplayName("every carry profile has finite offsets")
  void everyCarryProfileHasFiniteOffsets(final UnleashedDogBreed breed) {
    final FetchCarryProfiles profiles = breed.fetchCarryProfiles().orElseThrow();
    for (final CarryProfile profile : List.of(profiles.tennisBall(), profiles.stick())) {
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

  static Stream<Arguments> expectedDimensions() {
    return Stream.of(
        Arguments.of(UnleashedDogBreed.HUSKY, 0.8f, 1.1f),
        Arguments.of(UnleashedDogBreed.DACHSHUND, 0.8f, 1.1f),
        Arguments.of(UnleashedDogBreed.BEAGLE, 0.8f, 1.1f),
        Arguments.of(UnleashedDogBreed.GOLDEN_RETRIEVER, 0.8f, 1.1f),
        Arguments.of(UnleashedDogBreed.SHIBA_INU, 0.8f, 1.1f),
        Arguments.of(UnleashedDogBreed.PINSCHER, 0.8f, 1.1f),
        Arguments.of(UnleashedDogBreed.DIRE_WOLF, 0.8f, 1.1f),
        Arguments.of(UnleashedDogBreed.CROSS_BREED, 0.8f, 1.1f));
  }

  @ParameterizedTest(name = "{0} dimensions = ({1} x {2})")
  @MethodSource("expectedDimensions")
  @DisplayName("each breed exposes its documented width and height")
  void breedDimensionsMatchDocumentedValues(
      final UnleashedDogBreed breed, final float expectedWidth, final float expectedHeight) {
    final UnleashedDogBreed.Dimensions dimensions = breed.dimensions();
    assertEquals(
        expectedWidth, dimensions.width(), 0.001f, breed + " width should match documented value");
    assertEquals(
        expectedHeight,
        dimensions.height(),
        0.001f,
        breed + " height should match documented value");
  }

  static Stream<Arguments> expectedAttributes() {
    return Stream.of(
        Arguments.of(UnleashedDogBreed.HUSKY, 25.0, 0.30, 5.0),
        Arguments.of(UnleashedDogBreed.DACHSHUND, 10.0, 0.25, 2.0),
        Arguments.of(UnleashedDogBreed.BEAGLE, 17.0, 0.29, 3.0),
        Arguments.of(UnleashedDogBreed.GOLDEN_RETRIEVER, 24.0, 0.30, 4.0),
        Arguments.of(UnleashedDogBreed.SHIBA_INU, 18.0, 0.32, 3.5),
        Arguments.of(UnleashedDogBreed.PINSCHER, 12.0, 0.31, 2.0),
        Arguments.of(UnleashedDogBreed.DIRE_WOLF, 30.0, 0.32, 6.0),
        Arguments.of(UnleashedDogBreed.CROSS_BREED, 18.0, 0.29, 3.5));
  }

  @ParameterizedTest(name = "{0} attributes = (hp={1}, speed={2}, attack={3})")
  @MethodSource("expectedAttributes")
  @DisplayName("each breed exposes its documented combat attributes")
  void breedAttributesMatchDocumentedValues(
      final UnleashedDogBreed breed,
      final double expectedMaxHealth,
      final double expectedMovementSpeed,
      final double expectedAttackDamage) {
    final UnleashedDogBreed.Attributes attributes = breed.attributes();
    assertEquals(expectedMaxHealth, attributes.maxHealth(), 0.001, breed + " max health");
    assertEquals(
        expectedMovementSpeed, attributes.movementSpeed(), 0.001, breed + " movement speed");
    assertEquals(expectedAttackDamage, attributes.attackDamage(), 0.001, breed + " attack damage");
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("the natural-spawning predicate mirrors the presence of spawn settings")
  void naturalSpawningPredicateMirrorsSpawnSettingsPresence(final UnleashedDogBreed breed) {
    assertEquals(
        breed.spawnSettings() != null,
        breed.isNaturallySpawning(),
        "isNaturallySpawning must match spawnSettings presence for " + breed);
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("only the cross-breed is excluded from natural spawning")
  void onlyCrossBreedIsExcludedFromNaturalSpawning(final UnleashedDogBreed breed) {
    assertEquals(breed != UnleashedDogBreed.CROSS_BREED, breed.isNaturallySpawning());
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("every naturally spawning breed declares a coherent spawn configuration")
  void everyNaturallySpawningBreedSpawnConfigurationIsCoherent(final UnleashedDogBreed breed) {
    final UnleashedDogBreed.SpawnSettings spawn = breed.spawnSettings();
    if (spawn == null) {
      return;
    }
    assertTrue(spawn.weight() >= 1 && spawn.weight() <= 100, "weight for " + breed);
    assertTrue(spawn.minGroupSize() >= 1, "minGroupSize for " + breed);
    assertTrue(spawn.maxGroupSize() >= spawn.minGroupSize(), "maxGroupSize >= minGroupSize");
    assertTrue(spawn.biomes().length > 0, "every breed must have at least one biome");
  }

  static Stream<Arguments> expectedVoices() {
    return Stream.of(
        Arguments.of(UnleashedDogBreed.HUSKY, true, false),
        Arguments.of(UnleashedDogBreed.DACHSHUND, false, true),
        Arguments.of(UnleashedDogBreed.BEAGLE, false, true),
        Arguments.of(UnleashedDogBreed.GOLDEN_RETRIEVER, false, true),
        Arguments.of(UnleashedDogBreed.SHIBA_INU, false, true),
        Arguments.of(UnleashedDogBreed.PINSCHER, false, true),
        Arguments.of(UnleashedDogBreed.DIRE_WOLF, true, true),
        Arguments.of(UnleashedDogBreed.CROSS_BREED, false, false));
  }

  @ParameterizedTest(name = "{0} howls={1} barks={2}")
  @MethodSource("expectedVoices")
  @DisplayName("each breed exposes its documented voice behavior")
  void breedVoiceMatchesDocumentedValues(
      final UnleashedDogBreed breed, final boolean expectedHowls, final boolean expectedBarks) {
    assertEquals(expectedHowls, breed.howls(), breed + " howls");
    assertEquals(expectedBarks, breed.hasBarkSound(), breed + " has a bark sound");
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("only the husky carries eye color variants")
  void onlyHuskyCarriesEyeColorVariants(final UnleashedDogBreed breed) {
    assertEquals(breed == UnleashedDogBreed.HUSKY, breed.hasEyeColorVariants());
  }

  static Stream<Arguments> expectedRenderTransforms() {
    return Stream.of(
        Arguments.of(UnleashedDogBreed.HUSKY, 1.3f, 0.5f, 0.0f, 1.31f),
        Arguments.of(UnleashedDogBreed.DACHSHUND, 1.3f, 0.75f, 180.0f, 0.57f),
        Arguments.of(UnleashedDogBreed.BEAGLE, 1.5f, 0.75f, 0.0f, 0.88f),
        Arguments.of(UnleashedDogBreed.GOLDEN_RETRIEVER, 1.7f, 0.85f, 0.0f, 1.31f),
        Arguments.of(UnleashedDogBreed.SHIBA_INU, 1.5f, 0.75f, 0.0f, 0.88f),
        Arguments.of(UnleashedDogBreed.PINSCHER, 1.0f, 0.5f, 0.0f, 0.59f),
        Arguments.of(UnleashedDogBreed.DIRE_WOLF, 1.47f, 0.735f, 0.0f, 1.31f),
        Arguments.of(UnleashedDogBreed.CROSS_BREED, 1.5f, 0.75f, 0.0f, 0.88f));
  }

  @ParameterizedTest(name = "{0} render transforms = (adult={1}, baby={2}, yaw={3}, portrait={4})")
  @MethodSource("expectedRenderTransforms")
  @DisplayName("each breed exposes its documented render transforms")
  void breedRenderTransformsMatchDocumentedValues(
      final UnleashedDogBreed breed,
      final float expectedAdultScale,
      final float expectedBabyScale,
      final float expectedBodyYawOffsetDegrees,
      final float expectedPortraitHeight) {
    final UnleashedDogBreed.RenderTransforms transforms = breed.renderTransforms();
    assertEquals(expectedAdultScale, transforms.adultScale(), 0.001f, breed + " adult scale");
    assertEquals(expectedBabyScale, transforms.babyScale(), 0.001f, breed + " baby scale");
    assertEquals(
        expectedBodyYawOffsetDegrees,
        transforms.bodyYawOffsetDegrees(),
        0.001f,
        breed + " body yaw offset");
    assertEquals(
        expectedPortraitHeight, transforms.portraitHeight(), 0.001f, breed + " portrait height");
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("every breed renders babies smaller than adults")
  void everyBreedRendersBabiesSmallerThanAdults(final UnleashedDogBreed breed) {
    final UnleashedDogBreed.RenderTransforms transforms = breed.renderTransforms();
    assertTrue(
        transforms.babyScale() < transforms.adultScale(),
        "baby scale must be below adult scale for " + breed);
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("derived asset paths embed the breed serialized id")
  void derivedAssetPathsEmbedSerializedId(final UnleashedDogBreed breed) {
    final String id = breed.serializedId();
    assertEquals("entity.dogs-unleashed." + id, breed.translationKey());
    assertEquals(
        Identifier.of("dogs-unleashed", "animations/" + id + ".animation.json"),
        breed.animationId());
    assertEquals("geo/" + id + ".geo.json", breed.geoPath());
  }

  static Stream<Arguments> furProfiles() {
    return Stream.of(
        Arguments.of(UnleashedDogBreed.HUSKY, DogFurStage.OVERGROWN, 144000, 2),
        Arguments.of(UnleashedDogBreed.GOLDEN_RETRIEVER, DogFurStage.OVERGROWN, 144000, 2),
        Arguments.of(UnleashedDogBreed.SHIBA_INU, DogFurStage.OVERGROWN, 180000, 2),
        Arguments.of(UnleashedDogBreed.PINSCHER, DogFurStage.SHAGGY, 216000, 1),
        Arguments.of(UnleashedDogBreed.DIRE_WOLF, DogFurStage.OVERGROWN, 144000, 2),
        Arguments.of(UnleashedDogBreed.BEAGLE, DogFurStage.SHAGGY, 216000, 1),
        Arguments.of(UnleashedDogBreed.DACHSHUND, DogFurStage.SHAGGY, 216000, 1),
        Arguments.of(UnleashedDogBreed.CROSS_BREED, DogFurStage.SHAGGY, 180000, 1));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("furProfiles")
  @DisplayName("each breed grows fur at the documented pace up to its documented cap")
  void furProfilesMatchTheSpec(
      final UnleashedDogBreed breed,
      final DogFurStage maxStage,
      final int baseTicksPerStage,
      final int yieldPerStage) {
    assertEquals(
        new UnleashedDogBreed.FurProfile(maxStage, baseTicksPerStage, yieldPerStage),
        breed.furProfile());
  }

  static Stream<Arguments> expectedHowlSounds() {
    return Stream.of(
        Arguments.of(UnleashedDogBreed.HUSKY, "dogs-unleashed:entity.husky.howl"),
        Arguments.of(UnleashedDogBreed.DIRE_WOLF, "dogs-unleashed:entity.direwolf.howl"));
  }

  @ParameterizedTest(name = "{0} howls with {1}")
  @MethodSource("expectedHowlSounds")
  @DisplayName("each howling breed resolves its own howl sound")
  void howlingBreedsResolveTheirOwnHowl(final UnleashedDogBreed breed, final String soundId) {
    assertNotNull(breed.howlSound(), breed + " howl sound");
    assertEquals(soundId, breed.howlSound().getId().toString());
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("only the dire wolf rig opens its jaw to bark")
  void onlyTheDireWolfMovesItsJawToBark(final UnleashedDogBreed breed) {
    assertEquals(breed == UnleashedDogBreed.DIRE_WOLF, breed.barkMovesJaw());
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("only the dire wolf growls")
  void onlyTheDireWolfGrowls(final UnleashedDogBreed breed) {
    if (breed == UnleashedDogBreed.DIRE_WOLF) {
      assertEquals("dogs-unleashed:entity.direwolf.growl", breed.growlSound().getId().toString());
    } else {
      assertNull(breed.growlSound(), breed + " growl sound");
    }
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("a breed warns of intruders with its own bark under an alert sound id")
  void alertSoundFollowsTheBark(final UnleashedDogBreed breed) {
    if (breed.barkSound() == null) {
      assertNull(breed.alertSound(), breed + " alert sound");
    } else {
      assertEquals(
          breed.barkSound().getId().toString().replace(".bark", ".alert"),
          breed.alertSound().getId().toString());
    }
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("a breed has a howl sound exactly when it howls")
  void howlSoundPresenceMatchesHowling(final UnleashedDogBreed breed) {
    assertEquals(breed.howls(), breed.howlSound() != null, breed + " howl sound presence");
  }

  static Stream<Arguments> expectedNatures() {
    return Stream.of(
        Arguments.of(UnleashedDogBreed.HUSKY, 3, false, false, true, false, false, 32),
        Arguments.of(UnleashedDogBreed.DACHSHUND, 3, false, false, true, false, false, 32),
        Arguments.of(UnleashedDogBreed.BEAGLE, 3, false, false, true, false, false, 64),
        Arguments.of(UnleashedDogBreed.GOLDEN_RETRIEVER, 3, false, false, true, false, false, 32),
        Arguments.of(UnleashedDogBreed.SHIBA_INU, 3, false, false, true, false, false, 32),
        Arguments.of(UnleashedDogBreed.PINSCHER, 3, false, false, true, false, false, 32),
        Arguments.of(UnleashedDogBreed.DIRE_WOLF, 3, true, true, false, true, true, 32),
        Arguments.of(UnleashedDogBreed.CROSS_BREED, 3, false, false, true, false, false, 32));
  }

  @ParameterizedTest(
      name =
          "{0} tames 1 in {1}, own breed only={2}, staged growth={3}, beds={4}, lives in packs={5},"
              + " trust={6}, scent range={7}")
  @MethodSource("expectedNatures")
  @DisplayName("each breed declares its taming chance, breeding rule and growth model")
  void breedNatureMatchesTheSpec(
      final UnleashedDogBreed breed,
      final int tameSuccessChance,
      final boolean breedsOnlyWithOwnBreed,
      final boolean growsThroughStages,
      final boolean sleepsInBeds,
      final boolean livesInPacks,
      final boolean tamesThroughTrust,
      final int scentRange) {
    assertEquals(
        new UnleashedDogBreed.Nature(
            tameSuccessChance,
            breedsOnlyWithOwnBreed,
            growsThroughStages,
            sleepsInBeds,
            livesInPacks,
            tamesThroughTrust,
            scentRange),
        breed.nature());
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("cross-breed founders spawn naturally and breed with other breeds")
  void crossBreedFoundersSpawnAndBreedOpenly(final UnleashedDogBreed breed) {
    assertEquals(
        breed.isNaturallySpawning() && breed != UnleashedDogBreed.DIRE_WOLF,
        breed.isCrossBreedFounder(),
        breed + " founder");
  }

  @Test
  @DisplayName("dire wolves spawn rarely, only in the coldest biomes, one pack leader at a time")
  void direWolfSpawnSettingsMatchTheSpec() {
    final UnleashedDogBreed.SpawnSettings spawn = UnleashedDogBreed.DIRE_WOLF.spawnSettings();
    assertNotNull(spawn);
    assertEquals(2, spawn.weight());
    assertEquals(1, spawn.minGroupSize());
    assertEquals(1, spawn.maxGroupSize());
    assertEquals(
        Set.of(
            BiomeKeys.ICE_SPIKES,
            BiomeKeys.FROZEN_PEAKS,
            BiomeKeys.JAGGED_PEAKS,
            BiomeKeys.SNOWY_SLOPES,
            BiomeKeys.SNOWY_TAIGA),
        Set.of(spawn.biomes()));
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("only the dire wolf lives in packs")
  void onlyDireWolfLivesInPacks(final UnleashedDogBreed breed) {
    assertEquals(
        breed == UnleashedDogBreed.DIRE_WOLF, breed.nature().livesInPacks(), breed + " packs");
  }

  @Test
  @DisplayName("each Nature builder method changes only its own trait")
  void natureBuilderChangesOneTraitAtATime() {
    final UnleashedDogBreed.Nature ordinary = UnleashedDogBreed.Nature.ORDINARY;
    assertEquals(
        new UnleashedDogBreed.Nature(3, true, false, true, false, false, 32),
        ordinary.breedingOnlyWithOwnBreed());
    assertEquals(
        new UnleashedDogBreed.Nature(3, false, true, true, false, false, 32),
        ordinary.growingThroughStages());
    assertEquals(
        new UnleashedDogBreed.Nature(3, false, false, false, false, false, 32),
        ordinary.sleepingOutsideBeds());
    assertEquals(
        new UnleashedDogBreed.Nature(3, false, false, true, true, false, 32),
        ordinary.livingInPacks());
    assertEquals(
        new UnleashedDogBreed.Nature(3, false, false, true, false, true, 32),
        ordinary.tamingThroughTrust());
    assertEquals(
        new UnleashedDogBreed.Nature(3, false, false, true, false, false, 64),
        ordinary.trackingScentUpTo(64));
  }
}
