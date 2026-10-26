package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.TestRegistries.REGISTRIES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.TestRegistries;
import com.grahambartley.dogsunleashed.config.AgingSettings;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.DogBondStep;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.personality.DogTrait;
import com.grahambartley.dogsunleashed.entity.personality.DogTraits;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.pet.PetAge;
import com.grahambartley.dogsunleashed.pet.PetAwayStatus;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetDataFixture;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import com.grahambartley.dogsunleashed.portrait.DogLook;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfitFixture;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PetSyncDataTest {

  @Test
  @DisplayName("codec round-trips a cross-breed pet with genome fields")
  void codecRoundTripsCrossBreedGenomeFields() {
    final PetSyncData pet =
        PetSyncDataFixture.pet()
            .breed(UnleashedDogBreed.CROSS_BREED)
            .name("Pixel")
            .composition(
                List.of(
                    new BreedShare(UnleashedDogBreed.HUSKY, 0.5f),
                    new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f)))
            .build();
    final RegistryByteBuf buf = TestRegistries.buf();
    PetSyncData.CODEC.encode(buf, pet);
    assertEquals(pet, PetSyncData.CODEC.decode(buf));
  }

  @ParameterizedTest(name = "codec round-trips a {0} pet")
  @EnumSource(PetLifeState.class)
  @DisplayName("codec round-trips every lifecycle state so the client can render undead pets")
  void codecRoundTripsEveryLifeState(final PetLifeState lifeState) {
    final PetSyncData pet =
        PetSyncDataFixture.pet()
            .breed(UnleashedDogBreed.HUSKY)
            .name("Balto")
            .lifeState(lifeState)
            .build();
    final RegistryByteBuf buf = TestRegistries.buf();
    PetSyncData.CODEC.encode(buf, pet);

    final PetSyncData decoded = PetSyncData.CODEC.decode(buf);
    assertEquals(pet, decoded);
    assertEquals(lifeState.isAlive(), decoded.alive(), "alive");
    assertEquals(lifeState == PetLifeState.UNDEAD, decoded.undead(), "undead");
  }

  @ParameterizedTest(name = "baby {0}, senior {1} round-trips as {2}")
  @CsvSource({"false, false, ADULT", "false, true, SENIOR", "true, false, PUPPY"})
  @DisplayName("codec round-trips the senior flag so the client can label seniors")
  void codecRoundTripsSenior(final boolean baby, final boolean senior, final PetAge age) {
    final PetSyncData pet =
        PetSyncDataFixture.pet()
            .breed(UnleashedDogBreed.BEAGLE)
            .name("Old Tom")
            .baby(baby)
            .senior(senior)
            .build();
    final RegistryByteBuf buf = TestRegistries.buf();
    PetSyncData.CODEC.encode(buf, pet);
    final PetSyncData decoded = PetSyncData.CODEC.decode(buf);
    assertEquals(pet, decoded);
    assertEquals(age, decoded.age());
  }

  @ParameterizedTest(name = "aging {0}, {1}, age {2} -> senior {3}")
  @CsvSource({
    "true, LIVING, 2400000, true",
    "true, LIVING, 2399999, false",
    "false, LIVING, 2400000, false",
    "true, UNDEAD, 2400000, false",
    "true, DECEASED, 2400000, true"
  })
  @DisplayName("the synced senior label follows the live aging setting, not a stale flag")
  void seniorFollowsTheLiveSetting(
      final boolean enabled,
      final PetLifeState lifeState,
      final long ageTicks,
      final boolean expected) {
    final DogsUnleashedConfig original = DogsUnleashed.SERVER_CONFIG;
    try {
      DogsUnleashed.SERVER_CONFIG = original.withAging(new AgingSettings(enabled, 100));
      final NbtCompound nbt = PetDataFixture.savedBeforeLooks(UnleashedDogBreed.BEAGLE);
      nbt.putString(ModNbtKeys.LIFE_STATE, lifeState.serializedName());
      nbt.putLong(ModNbtKeys.ADULT_AGE_TICKS, ageTicks);
      assertEquals(
          expected,
          PetSyncData.from(PetData.fromNbt(nbt, REGISTRIES), PetAwayStatus.HOME).senior());
    } finally {
      DogsUnleashed.SERVER_CONFIG = original;
    }
  }

  @ParameterizedTest(name = "bond {0} round-trips")
  @EnumSource(DogBondStep.class)
  @DisplayName("codec round-trips every bond step so the client can name it")
  void codecRoundTripsBond(final DogBondStep bond) {
    final PetSyncData pet = PetSyncDataFixture.pet().bond(bond).build();
    final RegistryByteBuf buf = TestRegistries.buf();
    PetSyncData.CODEC.encode(buf, pet);
    assertEquals(bond, PetSyncData.CODEC.decode(buf).bond());
  }

  @ParameterizedTest(name = "{0} saved bond points -> {1}")
  @CsvSource({"0, STRANGER", "49, STRANGER", "50, FAMILIAR", "349, FRIEND", "700, DEVOTED"})
  @DisplayName("a stored pet syncs the bond step its saved points have reached")
  void storedPetSyncsItsBondStep(final int points, final DogBondStep expected) {
    final NbtCompound nbt = PetDataFixture.savedBeforeLooks(UnleashedDogBreed.BEAGLE);
    nbt.putInt(ModNbtKeys.BOND_POINTS, points);
    assertEquals(
        expected, PetSyncData.from(PetData.fromNbt(nbt, REGISTRIES), PetAwayStatus.HOME).bond());
  }

  static Stream<Arguments> displayBreedCases() {
    return Stream.of(
        Arguments.of(
            "pure breed ignores composition",
            PetSyncDataFixture.pet().breed(UnleashedDogBreed.BEAGLE).name("Rex").build(),
            UnleashedDogBreed.BEAGLE),
        Arguments.of(
            "cross-breed without composition falls back to its own breed",
            PetSyncDataFixture.pet().breed(UnleashedDogBreed.CROSS_BREED).name("Pixel").build(),
            UnleashedDogBreed.CROSS_BREED),
        Arguments.of(
            "cross-breed resolves the dominant composition breed",
            PetSyncDataFixture.pet()
                .breed(UnleashedDogBreed.CROSS_BREED)
                .name("Pixel")
                .composition(
                    List.of(
                        new BreedShare(UnleashedDogBreed.SHIBA_INU, 0.25f),
                        new BreedShare(UnleashedDogBreed.HUSKY, 0.75f)))
                .build(),
            UnleashedDogBreed.HUSKY),
        Arguments.of(
            "cross-breed breaks share ties by serialized id",
            PetSyncDataFixture.pet()
                .breed(UnleashedDogBreed.CROSS_BREED)
                .name("Pixel")
                .composition(
                    List.of(
                        new BreedShare(UnleashedDogBreed.SHIBA_INU, 0.5f),
                        new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f)))
                .build(),
            UnleashedDogBreed.BEAGLE));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("displayBreedCases")
  @DisplayName("displayBreed resolves the breed the client should render")
  void displayBreedResolvesRenderBreed(
      final String label, final PetSyncData pet, final UnleashedDogBreed expected) {
    assertEquals(expected, pet.displayBreed());
  }

  static Stream<DogTraits> personalities() {
    return Stream.of(
        DogTraits.NONE,
        DogTraits.of(DogTrait.PLAYFUL),
        DogTraits.of(DogTrait.TIMID, DogTrait.LAZY));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("personalities")
  @DisplayName("codec round-trips the personality so the cards can show it")
  void codecRoundTripsPersonality(final DogTraits traits) {
    final PetSyncData pet = PetSyncDataFixture.pet().personality(traits).build();
    final RegistryByteBuf buf = TestRegistries.buf();
    PetSyncData.CODEC.encode(buf, pet);
    assertEquals(traits, PetSyncData.CODEC.decode(buf).personality());
  }

  static Stream<Arguments> looks() {
    return Stream.of(
        Arguments.of(0, DogPortraitOutfit.NONE),
        Arguments.of(4, DogPortraitOutfitFixture.dressed()));
  }

  @ParameterizedTest(name = "growth stage {0}")
  @MethodSource("looks")
  @DisplayName("codec round-trips the growth stage and outfit so previews show the dog as it is")
  void codecRoundTripsLook(final int growthStage, final DogPortraitOutfit outfit) {
    final PetSyncData pet =
        PetSyncDataFixture.pet()
            .breed(UnleashedDogBreed.DIRE_WOLF)
            .growthStage(growthStage)
            .outfit(outfit)
            .build();
    final RegistryByteBuf buf = TestRegistries.buf();
    PetSyncData.CODEC.encode(buf, pet);
    final PetSyncData decoded = PetSyncData.CODEC.decode(buf);
    assertEquals(growthStage, decoded.look().growthStage());
    assertEquals(outfit, decoded.outfit());
  }

  @ParameterizedTest(name = "growth stage {0}")
  @MethodSource("looks")
  @DisplayName("a stored pet syncs its look and outfit as they were saved")
  void storedPetSyncsItsLook(final int growthStage, final DogPortraitOutfit outfit) {
    final NbtCompound nbt = PetDataFixture.savedBeforeLooks(UnleashedDogBreed.DIRE_WOLF);
    nbt.putInt(ModNbtKeys.GROWTH_STAGE, growthStage);
    PetDataFixture.wear(nbt, outfit);
    final PetSyncData synced =
        PetSyncData.from(PetData.fromNbt(nbt, REGISTRIES), PetAwayStatus.HOME);
    assertEquals(growthStage, synced.look().growthStage());
    assertEquals(outfit, synced.outfit());
  }

  @Test
  @DisplayName("a synced pet carries no genes, so hidden carried genes stay on the server")
  void syncedLookHidesGenes() {
    final PetData pet = PetDataFixture.pet();

    final PetSyncData synced = PetSyncData.from(pet, PetAwayStatus.HOME);

    assertNotNull(pet.getLook().genes());
    assertNull(synced.look().genes());
    assertEquals(pet.getLook().withGenes(null), synced.look());
  }

  private static final List<BreedShare> BEAGLE_GOLDEN =
      List.of(
          new BreedShare(UnleashedDogBreed.GOLDEN_RETRIEVER, 0.5f),
          new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f));

  static Stream<Arguments> eyeVariantCases() {
    return Stream.of(
        Arguments.of("husky keeps its eye variant", UnleashedDogBreed.HUSKY, List.of(), 2),
        Arguments.of(
            "beagle has no eye variants so the preview leaves it unset",
            UnleashedDogBreed.BEAGLE,
            List.of(),
            UnleashedDogEntity.UNSET_VARIANT),
        Arguments.of(
            "a husky-dominant cross keeps the eye variant its rig breed supports",
            UnleashedDogBreed.CROSS_BREED,
            List.of(
                new BreedShare(UnleashedDogBreed.HUSKY, 0.75f),
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.25f)),
            2),
        Arguments.of(
            "a beagle-dominant cross leaves the eye variant unset",
            UnleashedDogBreed.CROSS_BREED,
            BEAGLE_GOLDEN,
            UnleashedDogEntity.UNSET_VARIANT));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("eyeVariantCases")
  @DisplayName("the preview's eye variant follows the breed whose rig it renders on")
  void previewEyesFollowTheRigBreed(
      final String description,
      final UnleashedDogBreed breed,
      final List<BreedShare> composition,
      final int expected) {
    final PetSyncData pet =
        PetSyncDataFixture.pet().breed(breed).composition(composition).variants(3, 2).build();

    final DogLook look = pet.portraitLook();

    assertEquals(expected, look.statue().eyeColorVariant());
    assertEquals(3, look.statue().coatVariant());
    assertEquals(breed, look.statue().breed());
  }

  @ParameterizedTest(name = "a {0} pet previews undead={1}")
  @CsvSource({"LIVING, false", "UNDEAD, true", "DECEASED, false", "LOST, false"})
  @DisplayName("only an undead pet previews as the zombie dog")
  void onlyUndeadPetsPreviewUndead(final PetLifeState lifeState, final boolean expected) {
    final PetSyncData pet =
        PetSyncDataFixture.pet().breed(UnleashedDogBreed.BEAGLE).lifeState(lifeState).build();

    assertEquals(expected, pet.portraitLook().undead());
  }

  @ParameterizedTest(name = "senior {0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("the preview wears the pet's outfit and the synced senior coat")
  void previewWearsOutfitAndSeniorCoat(final boolean senior) {
    final PetSyncData pet =
        PetSyncDataFixture.pet()
            .outfit(DogPortraitOutfitFixture.dressed())
            .senior(senior)
            .growthStage(3)
            .build();

    final DogLook look = pet.portraitLook();

    assertEquals(DogPortraitOutfitFixture.dressed(), look.outfit());
    assertEquals(senior, look.senior());
    assertEquals(3, look.statue().growthStage());
  }

  @Test
  @DisplayName("a cross-breed preview carries the pet's whole breed mix")
  void crossBreedPreviewCarriesTheMix() {
    final PetSyncData pet =
        PetSyncDataFixture.pet()
            .breed(UnleashedDogBreed.CROSS_BREED)
            .composition(BEAGLE_GOLDEN)
            .build();

    assertEquals(DogGenome.sortedComposition(BEAGLE_GOLDEN), pet.composition());
    assertEquals(
        DogGenome.sortedComposition(BEAGLE_GOLDEN), pet.portraitLook().statue().composition());
  }
}
