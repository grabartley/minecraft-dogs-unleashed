package com.grahambartley.dogsunleashed.entity.personality;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class DogTraitTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogTrait.class)
  @DisplayName("every trait round-trips through its saved id")
  void serializedIdRoundTrips(final DogTrait trait) {
    assertEquals(trait, DogTrait.fromSerializedIdOrNull(trait.serializedId()));
    assertEquals("personality.dogs-unleashed." + trait.serializedId(), trait.translationKey());
  }

  @ParameterizedTest(name = "id \"{0}\"")
  @ValueSource(strings = {"", "BRAVE", "fearless"})
  @DisplayName("unknown ids read as no trait")
  void unknownIdIsNull(final String id) {
    assertNull(DogTrait.fromSerializedIdOrNull(id));
  }

  @ParameterizedTest(name = "{0} vs {1} -> {2}")
  @CsvSource({
    "BRAVE, TIMID, true",
    "TIMID, BRAVE, true",
    "LAZY, PLAYFUL, true",
    "PLAYFUL, LAZY, true",
    "BRAVE, GREEDY, false",
    "CLINGY, TIMID, false",
    "GREEDY, LAZY, false"
  })
  @DisplayName("opposite traits conflict both ways")
  void conflicts(final DogTrait first, final DogTrait second, final boolean conflicts) {
    assertEquals(conflicts, first.conflictsWith(second));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "BRAVE, true, 0.0, 2.0, true, false",
    "TIMID, false, 0.5, -2.5, false, true",
    "GREEDY, false, 0.0, 0.0, false, false",
    "LAZY, false, 0.0, 0.0, false, false",
    "CLINGY, false, 0.0, 0.0, false, false",
    "PLAYFUL, false, 0.0, 0.0, false, false"
  })
  @DisplayName("only brave and timid change how a dog faces a fight")
  void fightEffects(
      final DogTrait trait,
      final boolean holdsGround,
      final double fleeHealth,
      final double stance,
      final boolean growls,
      final boolean cowers) {
    assertEquals(holdsGround, trait.holdsGround());
    assertEquals(fleeHealth, trait.fleeHealthFraction());
    assertEquals(stance, trait.stanceOffset());
    assertEquals(growls, trait.growlsAtThreats());
    assertEquals(cowers, trait.cowersAtThreats());
    assertEquals(trait != DogTrait.TIMID, trait.joinsOwnerFights());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "BRAVE, false, false, 1.0, 1.0, 1.0, false, false",
    "TIMID, false, false, 1.0, 1.0, 1.0, false, false",
    "GREEDY, true, false, 1.0, 1.0, 1.0, false, false",
    "LAZY, false, true, 1.5, 0.8, 1.0, false, false",
    "CLINGY, false, false, 1.0, 1.0, 0.6, true, false",
    "PLAYFUL, false, false, 1.0, 1.0, 1.0, false, true"
  })
  @DisplayName("each everyday trait has its own small effect")
  void everydayEffects(
      final DogTrait trait,
      final boolean snacks,
      final boolean napsOnGround,
      final double napScale,
      final double idleSpeedScale,
      final double followScale,
      final boolean clingy,
      final boolean bringsToys) {
    assertEquals(snacks, trait.snacks());
    assertEquals(napsOnGround, trait.napsOnGround());
    assertEquals(napScale, trait.napScale());
    assertEquals(idleSpeedScale, trait.idleSpeedScale());
    assertEquals(followScale, trait.followDistanceScale());
    assertEquals(clingy, trait.whinesWhenLonely());
    assertEquals(clingy, trait.greetsWithHearts());
    assertEquals(bringsToys, trait.bringsToys());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogTrait.class)
  @DisplayName("no trait follows closer than the closest follow distance, the clingy 0.6")
  void closestFollowDistanceScale(final DogTrait trait) {
    assertEquals(0.6, DogTrait.closestFollowDistanceScale());
    assertTrue(trait.followDistanceScale() >= DogTrait.closestFollowDistanceScale());
  }
}
