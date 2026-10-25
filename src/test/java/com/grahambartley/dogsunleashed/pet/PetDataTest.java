package com.grahambartley.dogsunleashed.pet;

import static com.grahambartley.dogsunleashed.TestRegistries.REGISTRIES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.genome.AllelePair;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneLocus;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneTrait;
import com.grahambartley.dogsunleashed.entity.genome.DogGenes;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.personality.DogTrait;
import com.grahambartley.dogsunleashed.entity.personality.DogTraits;
import com.grahambartley.dogsunleashed.entity.trick.DogTrick;
import com.grahambartley.dogsunleashed.entity.trick.TrickProgress;
import com.grahambartley.dogsunleashed.entity.trick.TrickProgressNbt;
import com.grahambartley.dogsunleashed.entity.variant.HuskyCoat;
import com.grahambartley.dogsunleashed.entity.variant.HuskyEyeColor;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfitFixture;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
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

  private static final float BASE_HEALTH = PetDataFixture.HEALTH;
  private static final BlockPos BASE_POS = PetDataFixture.POS;
  private static final String BASE_DIM = PetDataFixture.DIMENSION;

  private static PetData baselinePet() {
    return PetDataFixture.pet();
  }

  private static PetData reload(final NbtCompound nbt) {
    return PetData.fromNbt(PetData.fromNbt(nbt, REGISTRIES).toNbt(REGISTRIES), REGISTRIES);
  }

  private static PortraitLook baselineLookWith(final Consumer<NbtCompound> change) {
    final NbtCompound nbt = baselinePet().toNbt(REGISTRIES);
    change.accept(nbt);
    return reload(nbt).portraitLook();
  }

  private static Consumer<NbtCompound> wearing(final DogPortraitOutfit outfit) {
    return nbt -> PetDataFixture.wear(nbt, outfit);
  }

  @ParameterizedTest(name = "differsFrom is true when {0} changes")
  @MethodSource("changedFieldCases")
  @DisplayName("differsFrom returns true when any tracked field changes")
  void differsFromDetectsEachChangedField(
      final String changedField,
      final float health,
      final BlockPos pos,
      final String dimension,
      final Consumer<NbtCompound> lookChange) {
    assertTrue(
        baselinePet().differsFrom(health, pos, dimension, baselineLookWith(lookChange), 0),
        "expected differsFrom to flag a change in " + changedField);
  }

  static Stream<Arguments> changedFieldCases() {
    final Consumer<NbtCompound> same = nbt -> {};
    return Stream.of(
        Arguments.of("health", BASE_HEALTH - 5.0f, BASE_POS, BASE_DIM, same),
        Arguments.of("position", BASE_HEALTH, BASE_POS.add(1, 0, 0), BASE_DIM, same),
        Arguments.of("dimension", BASE_HEALTH, BASE_POS, "minecraft:the_nether", same),
        Arguments.of("dimension going null", BASE_HEALTH, BASE_POS, null, same),
        lookCase("baby", nbt -> nbt.putBoolean(ModNbtKeys.PORTRAIT_BABY, true)),
        lookCase("coatVariant", nbt -> nbt.putInt(ModNbtKeys.PORTRAIT_COAT_VARIANT, 1)),
        lookCase("huskyEyeVariant", nbt -> nbt.putInt(ModNbtKeys.PORTRAIT_HUSKY_EYE, 1)),
        lookCase(
            "furStage",
            nbt -> nbt.putInt(ModNbtKeys.PORTRAIT_FUR_STAGE, DogFurStage.OVERGROWN.ordinal())),
        lookCase("growthStage", nbt -> nbt.putInt(ModNbtKeys.GROWTH_STAGE, 4)),
        lookCase("collar", wearing(DogPortraitOutfitFixture.collarOnly(DyeColor.LIME))),
        lookCase("senior coat", wearing(DogPortraitOutfit.NONE.withSenior(true))),
        lookCase("gear and mud", wearing(DogPortraitOutfitFixture.dressed())));
  }

  private static Arguments lookCase(final String field, final Consumer<NbtCompound> change) {
    return Arguments.of(field, BASE_HEALTH, BASE_POS, BASE_DIM, change);
  }

  @ParameterizedTest(name = "differsFrom is false when the position is an equal {0}")
  @MethodSource("equalPositionCases")
  @DisplayName("differsFrom returns false when all tracked fields match")
  void differsFromIgnoresUnchangedState(final String label, final BlockPos equalPos) {
    assertFalse(
        baselinePet().differsFrom(BASE_HEALTH, equalPos, BASE_DIM, baselinePet().portraitLook(), 0),
        "expected differsFrom to report no change for an unchanged pet (" + label + ")");
  }

  static Stream<Arguments> equalPositionCases() {
    return Stream.of(
        Arguments.of("same instance", BASE_POS),
        Arguments.of("equal-by-value instance", new BlockPos(10, 64, -20)));
  }

  @ParameterizedTest(name = "bond points {0}")
  @ValueSource(ints = {1, 50, 700})
  @DisplayName("differsFrom returns true when the bond grows")
  void differsFromDetectsBondGrowth(final int bondPoints) {
    assertTrue(
        baselinePet()
            .differsFrom(
                BASE_HEALTH, BASE_POS, BASE_DIM, baselinePet().portraitLook(), bondPoints));
  }

  static Stream<DogPortraitOutfit> savedOutfits() {
    return DogPortraitOutfitFixture.samples();
  }

  @ParameterizedTest
  @MethodSource("savedOutfits")
  @DisplayName("the portrait outfit survives a save and reload")
  void outfitRoundTrips(final DogPortraitOutfit outfit) {
    final PetData reloaded =
        reload(PetDataFixture.wearing(baselinePet(), outfit).toNbt(REGISTRIES));

    assertEquals(outfit, reloaded.getOutfit());
    assertEquals(outfit, reloaded.portraitLook().outfit());
  }

  @Test
  @DisplayName("a pet saved before outfits loads with no outfit")
  void legacyPetHasNoOutfit() {
    final NbtCompound nbt = baselinePet().toNbt(REGISTRIES);
    nbt.remove(ModNbtKeys.PORTRAIT_OUTFIT);

    assertEquals(DogPortraitOutfit.NONE, PetData.fromNbt(nbt, REGISTRIES).getOutfit());
  }

  @ParameterizedTest(name = "saved {0} -> {1}")
  @CsvSource({"0, 0", "120, 120", "700, 700", "5000, 700", "-3, 0"})
  @DisplayName("bond points survive a save and reload, clamped to the bond range")
  void bondRoundTrips(final int saved, final int expected) {
    final NbtCompound nbt = baselinePet().toNbt(REGISTRIES);
    nbt.putInt(ModNbtKeys.BOND_POINTS, saved);
    final PetData reloaded = reload(nbt);
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
    final NbtCompound nbt = baselinePet().toNbt(REGISTRIES);
    nbt.put(ModNbtKeys.PERSONALITY, traits.toNbt());
    final PetData reloaded = reload(nbt);
    assertEquals(traits, reloaded.getPersonality());
  }

  @Test
  @DisplayName("a pet saved before personalities has none")
  void legacyPetHasNoPersonality() {
    final NbtCompound nbt = baselinePet().toNbt(REGISTRIES);
    nbt.remove(ModNbtKeys.PERSONALITY);
    assertTrue(PetData.fromNbt(nbt, REGISTRIES).getPersonality().isEmpty());
  }

  static Stream<TrickProgress> trickProgresses() {
    return Stream.of(
        TrickProgress.NONE,
        TrickProgress.of(Map.of(DogTrick.BEG, 3)),
        TrickProgress.of(Map.of(DogTrick.ROLL_OVER, 5, DogTrick.PLAY_DEAD, 5)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("trickProgresses")
  @DisplayName("learned tricks survive a save and reload so they outlive death")
  void tricksRoundTrip(final TrickProgress progress) {
    final NbtCompound nbt = baselinePet().toNbt(REGISTRIES);
    nbt.put(ModNbtKeys.TRICKS, TrickProgressNbt.write(progress));
    final PetData reloaded = reload(nbt);
    assertEquals(progress, reloaded.getTricks());
  }

  @Test
  @DisplayName("a pet saved before tricks knows none")
  void legacyPetKnowsNoTricks() {
    final NbtCompound nbt = baselinePet().toNbt(REGISTRIES);
    nbt.remove(ModNbtKeys.TRICKS);
    assertEquals(TrickProgress.NONE, PetData.fromNbt(nbt, REGISTRIES).getTricks());
  }

  @ParameterizedTest(name = "age {0} ticks, senior parent {1}")
  @CsvSource({"0, false", "24000, true", "2400000, false", "9000000000, true"})
  @DisplayName("the adult age and senior legacy survive a save and reload")
  void ageRoundTrips(final long ageTicks, final boolean seniorParent) {
    final NbtCompound nbt = baselinePet().toNbt(REGISTRIES);
    nbt.putLong(ModNbtKeys.ADULT_AGE_TICKS, ageTicks);
    nbt.putBoolean(ModNbtKeys.SENIOR_PARENT, seniorParent);
    final PetData reloaded = reload(nbt);
    assertEquals(ageTicks, reloaded.getAdultAgeTicks());
    assertEquals(seniorParent, reloaded.hasSeniorParent());
  }

  @ParameterizedTest
  @EnumSource(
      value = DyeColor.class,
      names = {"BLUE", "LIME"})
  @DisplayName("a pet saved before outfits moves its collar into its outfit")
  void legacyCollarMovesIntoTheOutfit(final DyeColor collar) {
    final NbtCompound nbt = baselinePet().toNbt(REGISTRIES);
    nbt.remove(ModNbtKeys.PORTRAIT_OUTFIT);
    nbt.putInt(ModNbtKeys.PORTRAIT_COLLAR, collar.getId());

    final PetData reloaded = reload(nbt);

    assertEquals(DogPortraitOutfitFixture.collarOnly(collar), reloaded.getOutfit());
    assertEquals(collar.getId(), reloaded.getCollarColorId());
  }

  @Test
  @DisplayName("a pet saved before aging loads as a young adult")
  void legacySaveIsNotSenior() {
    final NbtCompound nbt = baselinePet().toNbt(REGISTRIES);
    nbt.remove(ModNbtKeys.ADULT_AGE_TICKS);
    final PetData reloaded = PetData.fromNbt(nbt, REGISTRIES);
    assertEquals(0L, reloaded.getAdultAgeTicks());
  }

  @Test
  @DisplayName("the portrait fur stage survives a save and reload")
  void portraitFurRoundTrips() {
    final NbtCompound nbt = baselinePet().toNbt(REGISTRIES);
    nbt.putInt(ModNbtKeys.PORTRAIT_FUR_STAGE, DogFurStage.BOTCHED.ordinal());

    final PetData reloaded = reload(nbt);

    assertEquals(DogFurStage.BOTCHED.ordinal(), reloaded.getFurStage());
  }

  @Test
  @DisplayName("a pet saved before fur growth existed loads with a natural coat")
  void legacyPetHasNaturalFur() {
    final NbtCompound nbt = baselinePet().toNbt(REGISTRIES);
    nbt.remove(ModNbtKeys.PORTRAIT_FUR_STAGE);

    final PetData reloaded = PetData.fromNbt(nbt, REGISTRIES);

    assertEquals(DogFurStage.NATURAL.ordinal(), reloaded.getFurStage());
  }

  @Test
  @DisplayName("the growth stage, battles won and time in stage survive a save and reload")
  void growthRoundTrips() {
    final NbtCompound nbt = baselinePet().toNbt(REGISTRIES);
    nbt.putInt(ModNbtKeys.GROWTH_STAGE, 5);
    nbt.putInt(ModNbtKeys.GROWTH_BATTLES_WON, 17);
    nbt.putInt(ModNbtKeys.GROWTH_TICKS_IN_STAGE, 12345);

    final PetData reloaded = reload(nbt);

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

    final PetData reloaded = PetData.fromNbt(pet.toNbt(REGISTRIES), REGISTRIES);

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
    final PetData reloaded = PetData.fromNbt(baselinePet().toNbt(REGISTRIES), REGISTRIES);

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
    final NbtCompound nbt = baselinePet().toNbt(REGISTRIES);
    nbt.putFloat(ModNbtKeys.MOVEMENT_SPEED, 0.31f);
    nbt.putFloat(ModNbtKeys.ATTACK_DAMAGE, 4.2f);
    nbt.put(ModNbtKeys.COMPOSITION, DogGenome.compositionToNbt(composition));

    final PetData read = PetData.fromNbt(nbt, REGISTRIES);

    assertEquals(0.31f, read.getMovementSpeed(), "movement speed");
    assertEquals(4.2f, read.getAttackDamage(), "attack damage");
    assertEquals(composition, read.getComposition(), "composition");
    assertEquals(
        composition,
        PetData.fromNbt(read.toNbt(REGISTRIES), REGISTRIES).getComposition(),
        "composition after a second round-trip");
  }

  @ParameterizedTest(name = "{0} survives the NBT round-trip")
  @EnumSource(PetLifeState.class)
  @DisplayName("every lifecycle state survives the NBT round-trip")
  void lifeStateSurvivesNbtRoundTrip(final PetLifeState lifeState) {
    final PetData pet = baselinePet();
    pet.setLifeState(lifeState);

    assertEquals(lifeState, PetData.fromNbt(pet.toNbt(REGISTRIES), REGISTRIES).getLifeState());
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
    final NbtCompound nbt = baselinePet().toNbt(REGISTRIES);
    nbt.remove(ModNbtKeys.LIFE_STATE);
    nbt.putBoolean(ModNbtKeys.ALIVE, alive);

    assertEquals(expected, PetData.fromNbt(nbt, REGISTRIES).getLifeState());
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
    final NbtCompound nbt = baselinePet().toNbt(REGISTRIES);
    nbt.put(ModNbtKeys.GENOME, genome.toNbt());

    final PetData reloaded = PetData.fromNbt(nbt, REGISTRIES);

    assertEquals(genome, reloaded.getGenome(), "genome");
    assertEquals(
        genome,
        PetData.fromNbt(reloaded.toNbt(REGISTRIES), REGISTRIES).getGenome(),
        "genome after re-save");
  }

  @Test
  @DisplayName("records written before genomes existed read back with defaults")
  void legacyNbtWithoutGenomeFieldsReadsDefaults() {
    final NbtCompound nbt = baselinePet().toNbt(REGISTRIES);
    nbt.remove(ModNbtKeys.MOVEMENT_SPEED);
    nbt.remove(ModNbtKeys.ATTACK_DAMAGE);

    final PetData reloaded = PetData.fromNbt(nbt, REGISTRIES);

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
    final NbtCompound nbt = baselinePet().toNbt(REGISTRIES);
    nbt.put(ModNbtKeys.GENES, genes.toNbt());

    final PetData reloaded = PetData.fromNbt(nbt, REGISTRIES);

    assertEquals(genes, reloaded.getGenes());
    assertEquals(
        genes,
        PetData.fromNbt(reloaded.toNbt(REGISTRIES), REGISTRIES).getGenes(),
        "genes after re-save");
  }

  @Test
  @DisplayName("records written before genes existed get genes matching their saved look")
  void legacyRecordDerivesGenesFromItsLook() {
    final NbtCompound nbt = baselinePet().toNbt(REGISTRIES);
    nbt.putInt(ModNbtKeys.PORTRAIT_COAT_VARIANT, HuskyCoat.SABLE.ordinal());
    nbt.putInt(ModNbtKeys.PORTRAIT_HUSKY_EYE, HuskyEyeColor.BLUE_HAZEL.ordinal());

    final DogGenes genes = PetData.fromNbt(nbt, REGISTRIES).getGenes();

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
    final NbtCompound nbt = cross.toNbt(REGISTRIES);
    nbt.put(
        ModNbtKeys.COMPOSITION,
        DogGenome.compositionToNbt(
            List.of(
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.7f),
                new BreedShare(UnleashedDogBreed.HUSKY, 0.3f))));
    assertEquals(UnleashedDogBreed.BEAGLE, PetData.fromNbt(nbt, REGISTRIES).getRigSourceBreed());
    assertEquals(UnleashedDogBreed.HUSKY, baselinePet().getRigSourceBreed());
  }
}
