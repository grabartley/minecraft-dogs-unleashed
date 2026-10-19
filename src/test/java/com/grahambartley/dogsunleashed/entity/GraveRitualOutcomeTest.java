package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.pet.PetLifeState;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class GraveRitualOutcomeTest {

  @ParameterizedTest(name = "lost grave={0}, pet {1} -> NOTHING")
  @MethodSource("everyGraveAndPet")
  @DisplayName("a grave without a totem never reacts to lightning")
  void aGraveWithoutATotemDoesNothing(final boolean graveLost, final PetLifeState petState) {
    assertEquals(GraveRitualOutcome.NOTHING, GraveRitualOutcome.decide(false, graveLost, petState));
  }

  static Stream<Arguments> everyGraveAndPet() {
    return Stream.of(true, false)
        .flatMap(
            lost ->
                Stream.concat(Stream.of(PetLifeState.values()), Stream.of((PetLifeState) null))
                    .map(state -> Arguments.of(lost, state)));
  }

  @ParameterizedTest(name = "lost grave, pet {0} -> FIZZLE")
  @EnumSource(PetLifeState.class)
  @DisplayName("a grave marked lost fizzles whatever its pet record says")
  void aLostGraveFizzles(final PetLifeState petState) {
    assertEquals(GraveRitualOutcome.FIZZLE, GraveRitualOutcome.decide(true, true, petState));
  }

  @ParameterizedTest(name = "lost grave={0} -> FIZZLE")
  @MethodSource("bothGraveMarks")
  @DisplayName("a lost pet fizzles even on a grave made before graves were marked")
  void aLostPetFizzles(final boolean graveLost) {
    assertEquals(
        GraveRitualOutcome.FIZZLE, GraveRitualOutcome.decide(true, graveLost, PetLifeState.LOST));
  }

  static Stream<Boolean> bothGraveMarks() {
    return Stream.of(true, false);
  }

  static Stream<Arguments> unmarkedGraves() {
    return Stream.of(
        Arguments.of(PetLifeState.DECEASED, GraveRitualOutcome.RAISE),
        Arguments.of(PetLifeState.LIVING, GraveRitualOutcome.NOTHING),
        Arguments.of(PetLifeState.UNDEAD, GraveRitualOutcome.NOTHING),
        Arguments.of(null, GraveRitualOutcome.NOTHING));
  }

  @ParameterizedTest(name = "pet {0} -> {1}")
  @MethodSource("unmarkedGraves")
  @DisplayName("an unmarked grave with a totem raises only a deceased pet")
  void anUnmarkedGraveRaisesOnlyADeceasedPet(
      final PetLifeState petState, final GraveRitualOutcome expected) {
    assertEquals(expected, GraveRitualOutcome.decide(true, false, petState));
  }
}
