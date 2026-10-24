package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed.FurProfile;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class DogFurRulesTest {

  private static final FurProfile HUSKY_PROFILE = new FurProfile(DogFurStage.OVERGROWN, 144000, 2);

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = UnleashedDogBreed.class,
      names = {"HUSKY", "BEAGLE", "DACHSHUND", "SHIBA_INU", "GOLDEN_RETRIEVER", "PINSCHER"})
  @DisplayName("a pure dog with or without a genome uses its breed's own profile")
  void pureBreedsUseTheirProfile(final UnleashedDogBreed breed) {
    assertEquals(breed.furProfile(), DogFurRules.profileFor(breed, null));
    assertEquals(breed.furProfile(), DogFurRules.profileFor(breed, DogGenome.pure(breed)));
  }

  @Test
  @DisplayName("a half husky, half beagle blends max stage, growth time and yield by share")
  void crossBreedBlendsByShare() {
    final DogGenome genome =
        new DogGenome(
            List.of(
                new BreedShare(UnleashedDogBreed.HUSKY, 0.5f),
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f)),
            20.0,
            0.3,
            4.0,
            DogGenome.BASELINE_FUR_GROWTH_RATE,
            UnleashedDogBreed.HUSKY);

    final FurProfile blended = DogFurRules.profileFor(UnleashedDogBreed.CROSS_BREED, genome);

    assertEquals(DogFurStage.OVERGROWN, blended.maxStage());
    assertEquals(180000, blended.baseTicksPerStage());
    assertEquals(2, blended.shearYieldPerStage());
  }

  @Test
  @DisplayName("a half husky, half shiba inu caps at shaggy, since the shiba inu stops at natural")
  void huskyShibaCrossCapsAtShaggy() {
    final DogGenome genome =
        new DogGenome(
            List.of(
                new BreedShare(UnleashedDogBreed.HUSKY, 0.5f),
                new BreedShare(UnleashedDogBreed.SHIBA_INU, 0.5f)),
            20.0,
            0.3,
            4.0,
            DogGenome.BASELINE_FUR_GROWTH_RATE,
            UnleashedDogBreed.HUSKY);

    final FurProfile blended = DogFurRules.profileFor(UnleashedDogBreed.CROSS_BREED, genome);

    assertEquals(DogFurStage.SHAGGY, blended.maxStage());
    assertEquals(162000, blended.baseTicksPerStage());
    assertEquals(2, blended.shearYieldPerStage());
  }

  @ParameterizedTest(name = "{0} capped at {1} settles to {2}")
  @CsvSource({
    "OVERGROWN, NATURAL, NATURAL",
    "SHAGGY, NATURAL, NATURAL",
    "NATURAL, NATURAL, NATURAL",
    "TRIMMED, NATURAL, TRIMMED",
    "BOTCHED, NATURAL, BOTCHED",
    "OVERGROWN, SHAGGY, SHAGGY",
    "SHAGGY, SHAGGY, SHAGGY",
    "NATURAL, SHAGGY, NATURAL",
    "TRIMMED, SHAGGY, TRIMMED",
    "BOTCHED, SHAGGY, BOTCHED"
  })
  @DisplayName("a coat above its breed's cap settles to the cap and any other coat stays put")
  void settledStage(final DogFurStage stage, final DogFurStage cap, final DogFurStage expected) {
    assertEquals(expected, DogFurRules.settledStage(stage, cap));
  }

  @Test
  @DisplayName("a mostly beagle cross caps at shaggy like a beagle")
  void mostlyBeagleCapsAtShaggy() {
    final DogGenome genome =
        new DogGenome(
            List.of(
                new BreedShare(UnleashedDogBreed.HUSKY, 0.2f),
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.8f)),
            20.0,
            0.3,
            4.0,
            DogGenome.BASELINE_FUR_GROWTH_RATE,
            UnleashedDogBreed.BEAGLE);

    assertEquals(
        DogFurStage.SHAGGY,
        DogFurRules.profileFor(UnleashedDogBreed.CROSS_BREED, genome).maxStage());
  }

  @ParameterizedTest(name = "from {0} at rate {1} and {2}% is {3} ticks")
  @CsvSource({
    "BOTCHED, 1.0, 100, 72000",
    "TRIMMED, 1.0, 100, 144000",
    "NATURAL, 1.0, 100, 216000",
    "SHAGGY, 1.0, 100, 288000",
    "NATURAL, 2.0, 100, 108000",
    "SHAGGY, 1.0, 200, 144000",
    "TRIMMED, 0.5, 50, 576000"
  })
  @DisplayName(
      "ticks per stage scale the base by the stage, then divide by the genome rate and config")
  void ticksPerStageScales(
      final DogFurStage from, final double rate, final int percent, final int expected) {
    assertEquals(
        OptionalInt.of(expected), DogFurRules.ticksPerStage(HUSKY_PROFILE, from, rate, percent));
  }

  @ParameterizedTest(name = "rate {0} at {1}%")
  @CsvSource({"1.0, 0", "0.0, 100", "-1.0, 100"})
  @DisplayName("a zero multiplier or a non-positive rate freezes growth")
  void zeroFreezesGrowth(final double rate, final int percent) {
    assertTrue(
        DogFurRules.ticksPerStage(HUSKY_PROFILE, DogFurStage.TRIMMED, rate, percent).isEmpty());
  }

  @ParameterizedTest(name = "from {0} at {1}% is {2}%")
  @CsvSource({
    "NATURAL, 100, 5",
    "SHAGGY, 100, 12",
    "OVERGROWN, 100, 20",
    "OVERGROWN, 200, 40",
    "OVERGROWN, 500, 100",
    "SHAGGY, 0, 0",
    "TRIMMED, 500, 0"
  })
  @DisplayName("botch chance scales with the config multiplier and caps at certain")
  void botchChance(final DogFurStage from, final int percent, final int expected) {
    assertEquals(expected, DogFurRules.botchChancePercent(from, percent));
  }

  @ParameterizedTest(name = "roll {1} from {0} botched={2}")
  @CsvSource({"SHAGGY, 11, true", "SHAGGY, 12, false", "NATURAL, 0, true", "NATURAL, 5, false"})
  @DisplayName("a roll below the botch chance botches the trim")
  void rollAgainstChance(final DogFurStage from, final int roll, final boolean expected) {
    assertEquals(expected, DogFurRules.isBotched(from, 100, roll));
  }

  @ParameterizedTest(name = "{0} x{1} botched={2} yields {3}")
  @CsvSource({
    "NATURAL, 2, false, 2",
    "SHAGGY, 2, false, 4",
    "OVERGROWN, 2, false, 6",
    "OVERGROWN, 2, true, 12",
    "NATURAL, 1, true, 2",
    "SHAGGY, 1, true, 4"
  })
  @DisplayName("yield is per stage removed down to a clean trim, doubled on a botch")
  void shearYield(
      final DogFurStage from, final int perStage, final boolean botched, final int expected) {
    assertEquals(expected, DogFurRules.shearYield(from, perStage, botched));
  }

  static Stream<Arguments> spawnRolls() {
    return Stream.of(
        Arguments.of(0, DogFurStage.OVERGROWN, DogFurStage.NATURAL),
        Arguments.of(69, DogFurStage.OVERGROWN, DogFurStage.NATURAL),
        Arguments.of(70, DogFurStage.OVERGROWN, DogFurStage.SHAGGY),
        Arguments.of(94, DogFurStage.OVERGROWN, DogFurStage.SHAGGY),
        Arguments.of(95, DogFurStage.OVERGROWN, DogFurStage.OVERGROWN),
        Arguments.of(99, DogFurStage.OVERGROWN, DogFurStage.OVERGROWN),
        Arguments.of(99, DogFurStage.SHAGGY, DogFurStage.SHAGGY));
  }

  @ParameterizedTest(name = "roll {0} capped at {1} -> {2}")
  @MethodSource("spawnRolls")
  @DisplayName("wild adults roll 70% natural, 25% shaggy, 5% overgrown, capped by breed")
  void spawnStage(final int roll, final DogFurStage cap, final DogFurStage expected) {
    assertEquals(expected, DogFurRules.spawnStage(roll, cap));
  }
}
