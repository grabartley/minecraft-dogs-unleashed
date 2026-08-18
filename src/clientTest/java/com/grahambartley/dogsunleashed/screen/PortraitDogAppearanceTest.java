package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.network.payload.PetSyncData;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
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
        PortraitDogAppearance.of(pet(UnleashedDogBreed.CROSS_BREED, BEAGLE_GOLDEN)).genome();

    assertNotNull(genome);
    assertEquals(DogGenome.sortedComposition(BEAGLE_GOLDEN), genome.composition());
  }

  @Test
  @DisplayName("a cross-breed portrait genome takes its attributes from the synced pet")
  void crossBreedGenomeUsesSyncedAttributes() {
    final DogGenome genome =
        PortraitDogAppearance.of(pet(UnleashedDogBreed.CROSS_BREED, BEAGLE_GOLDEN)).genome();

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
    final DogGenome genome = PortraitDogAppearance.of(pet).genome();

    if (expected) {
      assertNotNull(genome);
    } else {
      assertNull(genome);
    }
  }

  @Test
  @DisplayName("the portrait entity borrows the pet id so ear inheritance matches the world")
  void entityIdIsThePetId() {
    assertEquals(
        UUID.fromString(PET_ID),
        PortraitDogAppearance.of(pet(UnleashedDogBreed.CROSS_BREED, BEAGLE_GOLDEN)).entityId());
  }

  @ParameterizedTest(name = "collar id {0} maps to {1}")
  @CsvSource({
    "0, WHITE",
    "15, BLACK",
    "16, WHITE",
    "-1, BLACK",
    "-16, WHITE",
  })
  @DisplayName("the collar colour wraps into range instead of throwing on out-of-range ids")
  void collarColorWrapsIntoRange(final int collarColorId, final DyeColor expected) {
    assertEquals(expected, PortraitDogAppearance.of(pet(collarColorId)).collarColor());
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
    assertEquals(expected, PortraitDogAppearance.of(pet).eyeColorVariant());
  }

  @Test
  @DisplayName("the applied nbt carries the genome so the renderer can blend the cross-breed")
  void nbtCarriesGenomeForCrossBreeds() {
    final NbtCompound nbt =
        PortraitDogAppearance.of(pet(UnleashedDogBreed.CROSS_BREED, BEAGLE_GOLDEN)).toNbt();

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
            .toNbt()
            .contains(ModNbtKeys.GENOME));
  }

  @Test
  @DisplayName("the applied nbt carries the coat and eye variants the pet synced")
  void nbtCarriesVariants() {
    final NbtCompound nbt =
        PortraitDogAppearance.of(pet(UnleashedDogBreed.HUSKY, List.of())).toNbt();

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
                    UnleashedDogEntity.UNSET_VARIANT,
                    0))
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
    final PetSyncData base = pet(UnleashedDogBreed.BEAGLE, List.of());
    final PetSyncData pet =
        new PetSyncData(
            base.petId(),
            base.breed(),
            base.name(),
            base.health(),
            base.maxHealth(),
            base.posX(),
            base.posY(),
            base.posZ(),
            base.dimension(),
            lifeState,
            base.baby(),
            base.collarColor(),
            base.coatVariant(),
            base.huskyEyeVariant(),
            base.movementSpeed(),
            base.attackDamage(),
            base.composition());

    assertEquals(expected, PortraitDogAppearance.of(pet).undead());
  }

  private static PetSyncData pet(
      final UnleashedDogBreed breed, final List<BreedShare> composition) {
    return pet(breed, composition, 3, 2, 0);
  }

  private static PetSyncData pet(final int collarColorId) {
    return pet(UnleashedDogBreed.BEAGLE, List.of(), 3, 2, collarColorId);
  }

  private static PetSyncData pet(
      final UnleashedDogBreed breed,
      final List<BreedShare> composition,
      final int coatVariant,
      final int eyeVariant,
      final int collarColorId) {
    return new PetSyncData(
        PET_ID,
        breed,
        "Pixel",
        12.5f,
        MAX_HEALTH,
        10,
        64,
        -20,
        "minecraft:overworld",
        PetLifeState.LIVING,
        false,
        collarColorId,
        coatVariant,
        eyeVariant,
        MOVEMENT_SPEED,
        ATTACK_DAMAGE,
        composition);
  }
}
