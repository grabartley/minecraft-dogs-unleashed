package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class DogUndeadStateTest {

  private static final DogGenome MIXED_GENOME =
      new DogGenome(
          List.of(
              new BreedShare(UnleashedDogBreed.HUSKY, 0.6f),
              new BreedShare(UnleashedDogBreed.DACHSHUND, 0.4f)),
          17.0,
          0.28,
          3.6,
          DogGenome.BASELINE_FUR_GROWTH_RATE,
          UnleashedDogBreed.HUSKY);

  @ParameterizedTest(name = "{0} keeps its preset numbers when it has no genome")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("a dog with no genome takes its living baseline from the breed preset")
  void livingBaselineFallsBackToTheBreedPreset(final UnleashedDogBreed breed) {
    assertEquals(
        breed.attributes().maxHealth(), DogUndeadState.livingMaxHealth(breed, null), "max health");
    assertEquals(
        breed.attributes().attackDamage(),
        DogUndeadState.livingAttackDamage(breed, null),
        "attack damage");
  }

  @Test
  @DisplayName("a cross-breed's living baseline is its genome, not the cross-breed preset")
  void livingBaselinePrefersTheGenome() {
    assertEquals(
        MIXED_GENOME.maxHealth(),
        DogUndeadState.livingMaxHealth(UnleashedDogBreed.CROSS_BREED, MIXED_GENOME));
    assertEquals(
        MIXED_GENOME.attackDamage(),
        DogUndeadState.livingAttackDamage(UnleashedDogBreed.CROSS_BREED, MIXED_GENOME));
  }

  static Stream<Arguments> scalingCases() {
    return Stream.of(
        Arguments.of("a husky's health", 25.0),
        Arguments.of("a dachshund's health", 10.0),
        Arguments.of("a genome's health", 17.0));
  }

  @ParameterizedTest(name = "{0} halves when undead")
  @MethodSource("scalingCases")
  @DisplayName("undead stats are the living baseline scaled by the documented multipliers")
  void undeadStatsScaleTheLivingBaseline(final String label, final double living) {
    assertEquals(
        living * DogUndeadState.MAX_HEALTH_MULTIPLIER, DogUndeadState.undeadMaxHealth(living));
    assertEquals(
        living * DogUndeadState.ATTACK_DAMAGE_MULTIPLIER,
        DogUndeadState.undeadAttackDamage(living));
  }

  @ParameterizedTest(name = "{0} is strictly weaker undead than alive")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("every breed comes back weaker than it was in life")
  void everyBreedComesBackWeaker(final UnleashedDogBreed breed) {
    final double livingHealth = DogUndeadState.livingMaxHealth(breed, null);
    final double livingDamage = DogUndeadState.livingAttackDamage(breed, null);

    assertTrue(
        DogUndeadState.undeadMaxHealth(livingHealth) < livingHealth,
        breed + " undead max health must be below its living max health");
    assertTrue(
        DogUndeadState.undeadAttackDamage(livingDamage) < livingDamage,
        breed + " undead attack damage must be below its living attack damage");
  }

  @ParameterizedTest(name = "undead {0}")
  @CsvSource({"true, 0.5, 0.5", "false, 1.0, 1.0"})
  @DisplayName("only an undead dog has its stats scaled down")
  void onlyUndeadDogsAreScaled(
      final boolean undead, final double healthScale, final double damageScale) {
    assertEquals(healthScale, DogUndeadState.maxHealthScale(undead));
    assertEquals(damageScale, DogUndeadState.attackDamageScale(undead));
  }

  @ParameterizedTest(name = "an undead {0} has half its living stage stats")
  @EnumSource(DireWolfGrowthStage.class)
  @DisplayName("a halved base plus a halved bonus is exactly half the stage's living stats")
  void undeadStageStatsAreHalfTheLivingStage(final DireWolfGrowthStage stage) {
    final UnleashedDogBreed.Attributes base = UnleashedDogBreed.DIRE_WOLF.attributes();
    final double health =
        DogUndeadState.undeadMaxHealth(base.maxHealth())
            + DogGrowthRules.stageBonus(
                stage.maxHealth(), base.maxHealth(), DogUndeadState.maxHealthScale(true));
    final double damage =
        DogUndeadState.undeadAttackDamage(base.attackDamage())
            + DogGrowthRules.stageBonus(
                stage.attackDamage(), base.attackDamage(), DogUndeadState.attackDamageScale(true));
    assertEquals(stage.maxHealth() / 2.0, health, 1.0e-9, "max health");
    assertEquals(stage.attackDamage() / 2.0, damage, 1.0e-9, "attack damage");
  }
}
