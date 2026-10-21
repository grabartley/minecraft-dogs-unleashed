package com.grahambartley.dogsunleashed.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.AllelePair;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneLocus;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneTrait;
import com.grahambartley.dogsunleashed.entity.genome.DogGenes;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.personality.DogTrait;
import com.grahambartley.dogsunleashed.entity.personality.DogTraits;
import com.grahambartley.dogsunleashed.entity.variant.HuskyCoat;
import com.grahambartley.dogsunleashed.entity.variant.HuskyEyeColor;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
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
class PetDataTest {

  private static final float BASE_HEALTH = 18.0f;
  private static final BlockPos BASE_POS = new BlockPos(10, 64, -20);
  private static final String BASE_DIM = "minecraft:overworld";
  private static final boolean BASE_BABY = false;
  private static final int BASE_COAT = UnleashedDogEntity.UNSET_VARIANT;
  private static final int BASE_EYE = UnleashedDogEntity.UNSET_VARIANT;
  private static final int BASE_FUR = DogFurStage.NATURAL.ordinal();

  private static int baseCollar() {
    return DyeColor.RED.getId();
  }

  private static PetData baselinePet() {
    return new PetData(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UnleashedDogBreed.HUSKY,
        "Balto",
        BASE_HEALTH,
        20.0f,
        BASE_POS,
        BASE_DIM,
        PetLifeState.LIVING);
  }

  @ParameterizedTest(name = "differsFrom is true when {0} changes")
  @MethodSource("changedFieldCases")
  @DisplayName("differsFrom returns true when any tracked field changes")
  void differsFromDetectsEachChangedField(
      final String changedField,
      final float health,
      final BlockPos pos,
      final String dimension,
      final boolean baby,
      final int collar,
      final int coat,
      final int eye,
      final int fur) {
    assertTrue(
        baselinePet()
            .differsFrom(
                health, pos, dimension, new PortraitLook(baby, collar, coat, eye, fur, false), 0),
        "expected differsFrom to flag a change in " + changedField);
  }

  static Stream<Arguments> changedFieldCases() {
    return Stream.of(
        Arguments.of(
            "health",
            BASE_HEALTH - 5.0f,
            BASE_POS,
            BASE_DIM,
            BASE_BABY,
            baseCollar(),
            BASE_COAT,
            BASE_EYE,
            BASE_FUR),
        Arguments.of(
            "position",
            BASE_HEALTH,
            BASE_POS.add(1, 0, 0),
            BASE_DIM,
            BASE_BABY,
            baseCollar(),
            BASE_COAT,
            BASE_EYE,
            BASE_FUR),
        Arguments.of(
            "dimension",
            BASE_HEALTH,
            BASE_POS,
            "minecraft:the_nether",
            BASE_BABY,
            baseCollar(),
            BASE_COAT,
            BASE_EYE,
            BASE_FUR),
        Arguments.of(
            "dimension going null",
            BASE_HEALTH,
            BASE_POS,
            null,
            BASE_BABY,
            baseCollar(),
            BASE_COAT,
            BASE_EYE,
            BASE_FUR),
        Arguments.of(
            "baby",
            BASE_HEALTH,
            BASE_POS,
            BASE_DIM,
            !BASE_BABY,
            baseCollar(),
            BASE_COAT,
            BASE_EYE,
            BASE_FUR),
        Arguments.of(
            "collarColor",
            BASE_HEALTH,
            BASE_POS,
            BASE_DIM,
            BASE_BABY,
            baseCollar() + 1,
            BASE_COAT,
            BASE_EYE,
            BASE_FUR),
        Arguments.of(
            "coatVariant",
            BASE_HEALTH,
            BASE_POS,
            BASE_DIM,
            BASE_BABY,
            baseCollar(),
            BASE_COAT + 1,
            BASE_EYE,
            BASE_FUR),
        Arguments.of(
            "huskyEyeVariant",
            BASE_HEALTH,
            BASE_POS,
            BASE_DIM,
            BASE_BABY,
            baseCollar(),
            BASE_COAT,
            BASE_EYE + 1,
            BASE_FUR),
        Arguments.of(
            "furStage",
            BASE_HEALTH,
            BASE_POS,
            BASE_DIM,
            BASE_BABY,
            baseCollar(),
            BASE_COAT,
            BASE_EYE,
            DogFurStage.OVERGROWN.ordinal()));
  }

  @ParameterizedTest(name = "differsFrom is false when the position is an equal {0}")
  @MethodSource("equalPositionCases")
  @DisplayName("differsFrom returns false when all tracked fields match")
  void differsFromIgnoresUnchangedState(final String label, final BlockPos equalPos) {
    assertFalse(
        baselinePet()
            .differsFrom(
                BASE_HEALTH,
                equalPos,
                BASE_DIM,
                new PortraitLook(BASE_BABY, baseCollar(), BASE_COAT, BASE_EYE, BASE_FUR, false),
                0),
        "expected differsFrom to report no change for an unchanged pet (" + label + ")");
  }

  static Stream<Arguments> equalPositionCases() {
    return Stream.of(
        Arguments.of("same instance", BASE_POS),
        Arguments.of("equal-by-value instance", new BlockPos(10, 64, -20)));
  }

  @Test
  @DisplayName("differsFrom returns true when the dog turns senior")
  void differsFromDetectsTurningSenior() {
    assertTrue(
        baselinePet()
            .differsFrom(
                BASE_HEALTH,
                BASE_POS,
                BASE_DIM,
                new PortraitLook(BASE_BABY, baseCollar(), BASE_COAT, BASE_EYE, BASE_FUR, true),
                0));
  }

  @ParameterizedTest(name = "bond points {0}")
  @ValueSource(ints = {1, 50, 700})
  @DisplayName("differsFrom returns true when the bond grows")
  void differsFromDetectsBondGrowth(final int bondPoints) {
    assertTrue(
        baselinePet()
            .differsFrom(
                BASE_HEALTH,
                BASE_POS,
                BASE_DIM,
                new PortraitLook(BASE_BABY, baseCollar(), BASE_COAT, BASE_EYE, BASE_FUR, false),
                bondPoints));
  }

  @ParameterizedTest(name = "saved {0} -> {1}")
  @CsvSource({"0, 0", "120, 120", "700, 700", "5000, 700", "-3, 0"})
  @DisplayName("bond points survive a save and reload, clamped to the bond range")
  void bondRoundTrips(final int saved, final int expected) {
    final NbtCompound nbt = baselinePet().toNbt();
    nbt.putInt(ModNbtKeys.BOND_POINTS, saved);
    final PetData reloaded = PetData.fromNbt(PetData.fromNbt(nbt).toNbt());
    assertEquals(expected, reloaded.getBondPoints());
  }

  static Stream<DogTraits> personalities() {
    return Stream.of(
        DogTraits.NONE,
        DogTraits.of(DogTrait.BRAVE),
        DogTraits.of(DogTrait.GREEDY, DogTrait.CLINGY));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("personalities")
  @DisplayName("personality survives a save and reload so it outlives death")
  void personalityRoundTrips(final DogTraits traits) {
    final NbtCompound nbt = baselinePet().toNbt();
    nbt.put(ModNbtKeys.PERSONALITY, traits.toNbt());
    final PetData reloaded = PetData.fromNbt(PetData.fromNbt(nbt).toNbt());
    assertEquals(traits, reloaded.getPersonality());
  }

  @Test
  @DisplayName("a pet saved before personalities has none")
  void legacyPetHasNoPersonality() {
    final NbtCompound nbt = baselinePet().toNbt();
    nbt.remove(ModNbtKeys.PERSONALITY);
    assertTrue(PetData.fromNbt(nbt).getPersonality().isEmpty());
  }

  @ParameterizedTest(name = "age {0} ticks, senior {1}, senior parent {2}")
  @CsvSource({
    "0, false, false",
    "24000, false, true",
    "2400000, true, false",
    "9000000000, true, true"
  })
  @DisplayName("the adult age, senior flag and legacy survive a save and reload")
  void ageRoundTrips(final long ageTicks, final boolean senior, final boolean seniorParent) {
    final NbtCompound nbt = baselinePet().toNbt();
    nbt.putLong(ModNbtKeys.ADULT_AGE_TICKS, ageTicks);
    nbt.putBoolean(ModNbtKeys.SENIOR, senior);
    nbt.putBoolean(ModNbtKeys.SENIOR_PARENT, seniorParent);
    final PetData reloaded = PetData.fromNbt(PetData.fromNbt(nbt).toNbt());
    assertEquals(ageTicks, reloaded.getAdultAgeTicks());
    assertEquals(senior, reloaded.isSenior());
    assertEquals(senior, reloaded.portraitLook().senior());
    assertEquals(seniorParent, reloaded.hasSeniorParent());
  }

  @Test
  @DisplayName("a pet saved before aging loads as a young adult")
  void legacySaveIsNotSenior() {
    final NbtCompound nbt = baselinePet().toNbt();
    nbt.remove(ModNbtKeys.ADULT_AGE_TICKS);
    nbt.remove(ModNbtKeys.SENIOR);
    final PetData reloaded = PetData.fromNbt(nbt);
    assertEquals(0L, reloaded.getAdultAgeTicks());
    assertFalse(reloaded.isSenior());
  }

  @Test
  @DisplayName("the portrait fur stage survives a save and reload")
  void portraitFurRoundTrips() {
    final NbtCompound nbt = baselinePet().toNbt();
    nbt.putInt(ModNbtKeys.PORTRAIT_FUR_STAGE, DogFurStage.BOTCHED.ordinal());

    final PetData reloaded = PetData.fromNbt(PetData.fromNbt(nbt).toNbt());

    assertEquals(DogFurStage.BOTCHED.ordinal(), reloaded.getFurStage());
  }

  @Test
  @DisplayName("a pet saved before fur growth existed loads with a natural coat")
  void legacyPetHasNaturalFur() {
    final NbtCompound nbt = baselinePet().toNbt();
    nbt.remove(ModNbtKeys.PORTRAIT_FUR_STAGE);

    final PetData reloaded = PetData.fromNbt(nbt);

    assertEquals(DogFurStage.NATURAL.ordinal(), reloaded.getFurStage());
  }

  @Test
  @DisplayName("the growth stage, battles won and time in stage survive a save and reload")
  void growthRoundTrips() {
    final NbtCompound nbt = baselinePet().toNbt();
    nbt.putInt(ModNbtKeys.GROWTH_STAGE, 5);
    nbt.putInt(ModNbtKeys.GROWTH_BATTLES_WON, 17);
    nbt.putInt(ModNbtKeys.GROWTH_TICKS_IN_STAGE, 12345);

    final PetData reloaded = PetData.fromNbt(PetData.fromNbt(nbt).toNbt());

    assertEquals(5, reloaded.getGrowthStage());
    assertEquals(17, reloaded.getGrowthBattlesWon());
    assertEquals(12345, reloaded.getGrowthTicksInStage());
  }

  private static final UUID PARENT_A = UUID.nameUUIDFromBytes("parent-a".getBytes());
  private static final UUID PARENT_B = UUID.nameUUIDFromBytes("parent-b".getBytes());

  @ParameterizedTest(name = "{0}")
  @MethodSource("recordParentsCases")
  @DisplayName("recordParents fills only empty slots and reports whether anything changed")
  void recordParentsIsSetOnce(
      final String label,
      final UUID firstCallA,
      final UUID firstCallB,
      final UUID secondCallA,
      final UUID secondCallB,
      final boolean expectSecondCallChanged,
      final UUID expectedParentA,
      final UUID expectedParentB) {
    final PetData pet = baselinePet();
    pet.recordParents(firstCallA, firstCallB);

    assertEquals(
        expectSecondCallChanged,
        pet.recordParents(secondCallA, secondCallB),
        "changed flag for second recordParents call (" + label + ")");
    assertEquals(expectedParentA, pet.getParentAId(), "parentA (" + label + ")");
    assertEquals(expectedParentB, pet.getParentBId(), "parentB (" + label + ")");
  }

  static Stream<Arguments> recordParentsCases() {
    final UUID other = UUID.nameUUIDFromBytes("parent-other".getBytes());
    return Stream.of(
        Arguments.of(
            "both parents recorded at once",
            null,
            null,
            PARENT_A,
            PARENT_B,
            true,
            PARENT_A,
            PARENT_B),
        Arguments.of(
            "single known parent recorded alone", null, null, PARENT_A, null, true, PARENT_A, null),
        Arguments.of(
            "second-slot-only parent still recorded",
            null,
            null,
            null,
            PARENT_B,
            true,
            null,
            PARENT_B),
        Arguments.of(
            "duplicate parent collapses to one slot",
            null,
            null,
            PARENT_A,
            PARENT_A,
            true,
            PARENT_A,
            null),
        Arguments.of(
            "existing parents never overwritten",
            PARENT_A,
            PARENT_B,
            other,
            other,
            false,
            PARENT_A,
            PARENT_B),
        Arguments.of(
            "legacy one-parent record backfills the missing slot",
            PARENT_A,
            null,
            PARENT_A,
            PARENT_B,
            true,
            PARENT_A,
            PARENT_B),
        Arguments.of("null parents are a no-op", null, null, null, null, false, null, null));
  }

  @ParameterizedTest(name = "parentA={0}, parentB={1}")
  @MethodSource("parentNbtRoundTripCases")
  @DisplayName("parent ids survive the NBT round-trip, including records with unknown parents")
  void parentsSurviveNbtRoundTrip(final UUID parentAId, final UUID parentBId) {
    final PetData pet = baselinePet();
    pet.recordParents(parentAId, parentBId);

    final PetData reloaded = PetData.fromNbt(pet.toNbt());

    assertEquals(parentAId, reloaded.getParentAId(), "parentA after round-trip");
    assertEquals(parentBId, reloaded.getParentBId(), "parentB after round-trip");
  }

  static Stream<Arguments> parentNbtRoundTripCases() {
    return Stream.of(
        Arguments.of(PARENT_A, PARENT_B), Arguments.of(PARENT_A, null), Arguments.of(null, null));
  }

  @Test
  @DisplayName("records written before parentage existed read back with no parents")
  void legacyNbtWithoutParentsReadsAsUnknown() {
    final PetData reloaded = PetData.fromNbt(baselinePet().toNbt());

    assertNull(reloaded.getParentAId(), "legacy parentA");
    assertNull(reloaded.getParentBId(), "legacy parentB");
  }

  @Test
  @DisplayName("genome fields survive the NBT round-trip")
  void genomeFieldsSurviveNbtRoundTrip() {
    final List<BreedShare> composition =
        List.of(
            new BreedShare(UnleashedDogBreed.HUSKY, 0.575f),
            new BreedShare(UnleashedDogBreed.BEAGLE, 0.425f));
    final NbtCompound nbt = baselinePet().toNbt();
    nbt.putFloat(ModNbtKeys.MOVEMENT_SPEED, 0.31f);
    nbt.putFloat(ModNbtKeys.ATTACK_DAMAGE, 4.2f);
    nbt.put(ModNbtKeys.COMPOSITION, DogGenome.compositionToNbt(composition));

    final PetData read = PetData.fromNbt(nbt);

    assertEquals(0.31f, read.getMovementSpeed(), "movement speed");
    assertEquals(4.2f, read.getAttackDamage(), "attack damage");
    assertEquals(composition, read.getComposition(), "composition");
    assertEquals(
        composition,
        PetData.fromNbt(read.toNbt()).getComposition(),
        "composition after a second round-trip");
  }

  @ParameterizedTest(name = "{0} survives the NBT round-trip")
  @EnumSource(PetLifeState.class)
  @DisplayName("every lifecycle state survives the NBT round-trip")
  void lifeStateSurvivesNbtRoundTrip(final PetLifeState lifeState) {
    final PetData pet = baselinePet();
    pet.setLifeState(lifeState);

    assertEquals(lifeState, PetData.fromNbt(pet.toNbt()).getLifeState());
  }

  static Stream<Arguments> legacyAliveFlags() {
    return Stream.of(
        Arguments.of("alive", true, PetLifeState.LIVING),
        Arguments.of("not alive", false, PetLifeState.DECEASED));
  }

  @ParameterizedTest(name = "a legacy record marked {0} loads as {2}")
  @MethodSource("legacyAliveFlags")
  @DisplayName("records written before the lifecycle enum load from their old alive flag")
  void legacyNbtWithoutLifeStateReadsAliveFlag(
      final String label, final boolean alive, final PetLifeState expected) {
    final NbtCompound nbt = baselinePet().toNbt();
    nbt.remove(ModNbtKeys.LIFE_STATE);
    nbt.putBoolean(ModNbtKeys.ALIVE, alive);

    assertEquals(expected, PetData.fromNbt(nbt).getLifeState());
  }

  @Test
  @DisplayName("the genome survives the NBT round-trip so a resurrected pet keeps its stats")
  void genomeSurvivesNbtRoundTrip() {
    final DogGenome genome =
        new DogGenome(
            List.of(
                new BreedShare(UnleashedDogBreed.HUSKY, 0.6f),
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.4f)),
            21.5,
            0.295,
            4.1,
            DogGenome.BASELINE_FUR_GROWTH_RATE,
            UnleashedDogBreed.BEAGLE);
    final NbtCompound nbt = baselinePet().toNbt();
    nbt.put(ModNbtKeys.GENOME, genome.toNbt());

    final PetData reloaded = PetData.fromNbt(nbt);

    assertEquals(genome, reloaded.getGenome(), "genome");
    assertEquals(genome, PetData.fromNbt(reloaded.toNbt()).getGenome(), "genome after re-save");
  }

  @Test
  @DisplayName("records written before genomes existed read back with defaults")
  void legacyNbtWithoutGenomeFieldsReadsDefaults() {
    final NbtCompound nbt = baselinePet().toNbt();
    nbt.remove(ModNbtKeys.MOVEMENT_SPEED);
    nbt.remove(ModNbtKeys.ATTACK_DAMAGE);

    final PetData reloaded = PetData.fromNbt(nbt);

    assertEquals(0.0f, reloaded.getMovementSpeed(), "legacy movement speed");
    assertEquals(0.0f, reloaded.getAttackDamage(), "legacy attack damage");
    assertTrue(reloaded.getComposition().isEmpty(), "legacy composition");
  }

  @Test
  @DisplayName("genes survive a pet record round trip")
  void genesRoundTrip() {
    final DogGeneLocus coat = new DogGeneLocus(UnleashedDogBreed.HUSKY, DogGeneTrait.COAT);
    final DogGenes genes =
        new DogGenes(
            Map.of(
                coat, new AllelePair(HuskyCoat.BLACK_WHITE.ordinal(), HuskyCoat.WHITE.ordinal())));
    final NbtCompound nbt = baselinePet().toNbt();
    nbt.put(ModNbtKeys.GENES, genes.toNbt());

    final PetData reloaded = PetData.fromNbt(nbt);

    assertEquals(genes, reloaded.getGenes());
    assertEquals(genes, PetData.fromNbt(reloaded.toNbt()).getGenes(), "genes after re-save");
  }

  @Test
  @DisplayName("records written before genes existed get genes matching their saved look")
  void legacyRecordDerivesGenesFromItsLook() {
    final NbtCompound nbt = baselinePet().toNbt();
    nbt.putInt(ModNbtKeys.PORTRAIT_COAT_VARIANT, HuskyCoat.SABLE.ordinal());
    nbt.putInt(ModNbtKeys.PORTRAIT_HUSKY_EYE, HuskyEyeColor.BLUE_HAZEL.ordinal());

    final DogGenes genes = PetData.fromNbt(nbt).getGenes();

    assertEquals(
        AllelePair.matching(HuskyCoat.SABLE.ordinal()),
        genes.pair(new DogGeneLocus(UnleashedDogBreed.HUSKY, DogGeneTrait.COAT)));
    assertEquals(
        AllelePair.matching(HuskyEyeColor.BLUE_HAZEL.ordinal()),
        genes.pair(new DogGeneLocus(UnleashedDogBreed.HUSKY, DogGeneTrait.EYES)));
  }

  @Test
  @DisplayName("a cross record renders with its dominant breed, or the fallback rig without a mix")
  void rigSourceBreedFollowsComposition() {
    final PetData cross =
        new PetData(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UnleashedDogBreed.CROSS_BREED,
            "Mutt",
            BASE_HEALTH,
            20.0f,
            BASE_POS,
            BASE_DIM,
            PetLifeState.LIVING);
    assertEquals(UnleashedDogBreed.FALLBACK_RIG, cross.getRigSourceBreed());
    final NbtCompound nbt = cross.toNbt();
    nbt.put(
        ModNbtKeys.COMPOSITION,
        DogGenome.compositionToNbt(
            List.of(
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.7f),
                new BreedShare(UnleashedDogBreed.HUSKY, 0.3f))));
    assertEquals(UnleashedDogBreed.BEAGLE, PetData.fromNbt(nbt).getRigSourceBreed());
    assertEquals(UnleashedDogBreed.HUSKY, baselinePet().getRigSourceBreed());
  }
}
