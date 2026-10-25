package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.network.payload.PetSyncData;
import com.grahambartley.dogsunleashed.network.payload.PetSyncDataFixture;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfitFixture;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PortraitDogAppearanceTest {

  private static final String PET_ID = "6f2b8c1a-0d4e-4f2a-9c3b-7a1e5d8f0b26";
  private static final float MAX_HEALTH = 25.0f;
  private static final float MOVEMENT_SPEED = 0.29f;
  private static final float ATTACK_DAMAGE = 3.5f;

  private static final List<BreedShare> BEAGLE_GOLDEN =
      List.of(
          new BreedShare(UnleashedDogBreed.GOLDEN_RETRIEVER, 0.5f),
          new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f));

  static Stream<Arguments> entityBreedCases() {
    return Stream.of(
        Arguments.of(
            "cross-breed keeps the cross-breed entity type rather than its dominant parent",
            pet(UnleashedDogBreed.CROSS_BREED, BEAGLE_GOLDEN),
            UnleashedDogBreed.CROSS_BREED),
        Arguments.of(
            "pure breed uses its own entity type",
            pet(UnleashedDogBreed.BEAGLE, List.of()),
            UnleashedDogBreed.BEAGLE),
        Arguments.of(
            "cross-breed without a synced composition still uses the cross-breed entity type",
            pet(UnleashedDogBreed.CROSS_BREED, List.of()),
            UnleashedDogBreed.CROSS_BREED),
        Arguments.of(
            "a bred pure breed carrying a single-share composition keeps its own entity type",
            pet(UnleashedDogBreed.HUSKY, List.of(new BreedShare(UnleashedDogBreed.HUSKY, 1.0f))),
            UnleashedDogBreed.HUSKY));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("entityBreedCases")
  @DisplayName("the portrait entity type follows the pet's own breed")
  void entityBreedFollowsPetBreed(
      final String description, final PetSyncData pet, final UnleashedDogBreed expected) {
    assertEquals(expected, PortraitDogAppearance.of(pet).entityBreed());
  }

  @Test
  @DisplayName("a cross-breed portrait carries the pet's full composition")
  void crossBreedCarriesComposition() {
    final DogGenome genome =
        PortraitDogAppearance.of(pet(UnleashedDogBreed.CROSS_BREED, BEAGLE_GOLDEN)).look().genome();

    assertNotNull(genome);
    assertEquals(DogGenome.sortedComposition(BEAGLE_GOLDEN), genome.composition());
  }

  @Test
  @DisplayName("a cross-breed portrait genome takes its attributes from the synced pet")
  void crossBreedGenomeUsesSyncedAttributes() {
    final DogGenome genome =
        PortraitDogAppearance.of(pet(UnleashedDogBreed.CROSS_BREED, BEAGLE_GOLDEN)).look().genome();

    assertNotNull(genome);
    assertEquals(MAX_HEALTH, genome.maxHealth(), 0.0001);
    assertEquals(MOVEMENT_SPEED, genome.movementSpeed(), 0.0001);
    assertEquals(ATTACK_DAMAGE, genome.attackDamage(), 0.0001);
    assertEquals(DogGenome.dominantOf(BEAGLE_GOLDEN), genome.voiceBreed());
  }

  static Stream<Arguments> genomePresenceCases() {
    return Stream.of(
        Arguments.of(
            "cross-breed with a composition gets a genome",
            pet(UnleashedDogBreed.CROSS_BREED, BEAGLE_GOLDEN),
            true),
        Arguments.of(
            "pure breed with no composition stays on the pure-breed path",
            pet(UnleashedDogBreed.BEAGLE, List.of()),
            false),
        Arguments.of(
            "cross-breed with no synced composition stays on the pure-breed path",
            pet(UnleashedDogBreed.CROSS_BREED, List.of()),
            false),
        Arguments.of(
            "a bred pure breed with a single-share composition gets a genome",
            pet(UnleashedDogBreed.HUSKY, List.of(new BreedShare(UnleashedDogBreed.HUSKY, 1.0f))),
            true));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("genomePresenceCases")
  @DisplayName("the portrait genome is present exactly when the pet synced a composition")
  void genomePresenceFollowsComposition(
      final String description, final PetSyncData pet, final boolean expected) {
    final DogGenome genome = PortraitDogAppearance.of(pet).look().genome();

    if (expected) {
      assertNotNull(genome);
    } else {
      assertNull(genome);
    }
  }

  @Test
  @DisplayName("the appearance carries the pet's own id, so each pet caches its own portrait")
  void entityIdIsThePetId() {
    assertEquals(
        UUID.fromString(PET_ID),
        PortraitDogAppearance.of(pet(UnleashedDogBreed.CROSS_BREED, BEAGLE_GOLDEN)).entityId());
  }

  static Stream<Arguments> eyeVariantCases() {
    return Stream.of(
        Arguments.of("husky keeps its eye variant", pet(UnleashedDogBreed.HUSKY, List.of()), 2),
        Arguments.of(
            "beagle has no eye variants so the portrait leaves it unset",
            pet(UnleashedDogBreed.BEAGLE, List.of()),
            UnleashedDogEntity.UNSET_VARIANT),
        Arguments.of(
            "a husky-dominant cross keeps the eye variant its rig breed supports",
            pet(
                UnleashedDogBreed.CROSS_BREED,
                List.of(
                    new BreedShare(UnleashedDogBreed.HUSKY, 0.75f),
                    new BreedShare(UnleashedDogBreed.BEAGLE, 0.25f))),
            2),
        Arguments.of(
            "a beagle-dominant cross leaves the eye variant unset",
            pet(UnleashedDogBreed.CROSS_BREED, BEAGLE_GOLDEN),
            UnleashedDogEntity.UNSET_VARIANT));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("eyeVariantCases")
  @DisplayName("the eye variant follows the breed whose rig the portrait renders on")
  void eyeVariantFollowsRigBreed(
      final String description, final PetSyncData pet, final int expected) {
    assertEquals(expected, PortraitDogAppearance.of(pet).look().eyeColorVariant());
  }

  @Test
  @DisplayName("the applied nbt carries the genome so the renderer can blend the cross-breed")
  void nbtCarriesGenomeForCrossBreeds() {
    final NbtCompound nbt =
        PortraitDogAppearance.of(pet(UnleashedDogBreed.CROSS_BREED, BEAGLE_GOLDEN)).look().toNbt();

    assertTrue(nbt.contains(ModNbtKeys.GENOME));
    assertEquals(
        DogGenome.sortedComposition(BEAGLE_GOLDEN),
        DogGenome.compositionFromNbt(
            nbt.getCompound(ModNbtKeys.GENOME).getCompound(ModNbtKeys.GENOME_COMPOSITION)));
  }

  @Test
  @DisplayName("the applied nbt omits the genome for a pet with no composition")
  void nbtOmitsGenomeWithoutComposition() {
    assertFalse(
        PortraitDogAppearance.of(pet(UnleashedDogBreed.BEAGLE, List.of()))
            .look()
            .toNbt()
            .contains(ModNbtKeys.GENOME));
  }

  @Test
  @DisplayName("the applied nbt carries the coat and eye variants the pet synced")
  void nbtCarriesVariants() {
    final NbtCompound nbt =
        PortraitDogAppearance.of(pet(UnleashedDogBreed.HUSKY, List.of())).look().toNbt();

    assertEquals(3, nbt.getInt(ModNbtKeys.COAT_VARIANT));
    assertEquals(2, nbt.getInt(ModNbtKeys.EYE_COLOR_VARIANT));
  }

  @Test
  @DisplayName("the applied nbt omits unset variants so the entity keeps its rolled defaults")
  void nbtOmitsUnsetVariants() {
    final NbtCompound nbt =
        PortraitDogAppearance.of(
                pet(
                    UnleashedDogBreed.BEAGLE,
                    List.of(),
                    UnleashedDogEntity.UNSET_VARIANT,
                    UnleashedDogEntity.UNSET_VARIANT))
            .look()
            .toNbt();

    assertFalse(nbt.contains(ModNbtKeys.COAT_VARIANT));
    assertFalse(nbt.contains(ModNbtKeys.EYE_COLOR_VARIANT));
  }

  static Stream<Arguments> lifeStateCases() {
    return Stream.of(
        Arguments.of(PetLifeState.LIVING, false),
        Arguments.of(PetLifeState.UNDEAD, true),
        Arguments.of(PetLifeState.DECEASED, false),
        Arguments.of(PetLifeState.LOST, false));
  }

  @ParameterizedTest(name = "a {0} pet portrays undead={1}")
  @MethodSource("lifeStateCases")
  @DisplayName("only an undead pet's portrait dresses up as the zombie entity type")
  void onlyUndeadPetsPortrayTheZombieType(final PetLifeState lifeState, final boolean expected) {
    final PetSyncData pet =
        PetSyncDataFixture.pet().breed(UnleashedDogBreed.BEAGLE).lifeState(lifeState).build();

    assertEquals(expected, PortraitDogAppearance.of(pet).undead());
  }

  private static PetSyncData pet(
      final UnleashedDogBreed breed, final List<BreedShare> composition) {
    return pet(breed, composition, 3, 2);
  }

  private static PetSyncData pet(
      final UnleashedDogBreed breed,
      final List<BreedShare> composition,
      final int coatVariant,
      final int eyeVariant) {
    return PetSyncDataFixture.pet()
        .id(PET_ID)
        .breed(breed)
        .name("Pixel")
        .health(12.5f, MAX_HEALTH)
        .stats(MOVEMENT_SPEED, ATTACK_DAMAGE)
        .composition(composition)
        .variants(coatVariant, eyeVariant)
        .build();
  }

  @Test
  @DisplayName("the portrait dog is given the pet's fur stage")
  void nbtCarriesFur() {
    final PetSyncData overgrown =
        PetSyncDataFixture.pet()
            .breed(UnleashedDogBreed.HUSKY)
            .furStage(DogFurStage.OVERGROWN.ordinal())
            .build();

    final NbtCompound nbt = PortraitDogAppearance.of(overgrown).look().toNbt();

    assertEquals(DogFurStage.OVERGROWN.ordinal(), nbt.getInt(ModNbtKeys.FUR_STAGE));
  }

  static Stream<Arguments> lookCases() {
    return Stream.of(
        Arguments.of(0, DogPortraitOutfit.NONE),
        Arguments.of(3, DogPortraitOutfitFixture.dressed()));
  }

  @ParameterizedTest(name = "growth stage {0}")
  @MethodSource("lookCases")
  @DisplayName("the portrait dog wears the pet's outfit and grows to its stage")
  void appearanceCarriesOutfitAndGrowth(final int growthStage, final DogPortraitOutfit outfit) {
    final PetSyncData dressed =
        PetSyncDataFixture.pet()
            .breed(UnleashedDogBreed.DIRE_WOLF)
            .growthStage(growthStage)
            .outfit(outfit)
            .build();

    final PortraitDogAppearance appearance = PortraitDogAppearance.of(dressed);

    assertEquals(outfit, appearance.outfit());
    assertEquals(growthStage, appearance.look().growthStage());
  }
}
