package com.grahambartley.dogsunleashed.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class PetLifeStateTest {

  @ParameterizedTest(name = "{0} round-trips through its serializedName")
  @EnumSource(PetLifeState.class)
  @DisplayName("fromSerializedName round-trips every state")
  void fromSerializedNameRoundTripsEveryState(final PetLifeState state) {
    assertEquals(state, PetLifeState.fromSerializedName(state.serializedName()));
  }

  static Stream<Arguments> fallbackInputs() {
    return Stream.of(
        Arguments.of("null input", (String) null),
        Arguments.of("empty input", ""),
        Arguments.of("unknown identifier", "spectral"));
  }

  @ParameterizedTest(name = "{0} -> LIVING")
  @MethodSource("fallbackInputs")
  @DisplayName("fromSerializedName falls back to LIVING for missing or unknown input")
  void fromSerializedNameFallsBackToLiving(final String label, final String input) {
    assertEquals(PetLifeState.LIVING, PetLifeState.fromSerializedName(input));
  }

  @Test
  @DisplayName("the legacy alive flag maps onto the two states that existed before this enum")
  void legacyAliveFlagMapsToLivingAndDeceased() {
    assertEquals(PetLifeState.LIVING, PetLifeState.fromLegacyAliveFlag(true));
    assertEquals(PetLifeState.DECEASED, PetLifeState.fromLegacyAliveFlag(false));
  }

  static Stream<Arguments> predicateCases() {
    return Stream.of(
        Arguments.of(PetLifeState.LIVING, true, false, false),
        Arguments.of(PetLifeState.UNDEAD, true, false, true),
        Arguments.of(PetLifeState.DECEASED, false, true, false),
        Arguments.of(PetLifeState.LOST, false, false, false));
  }

  @ParameterizedTest(name = "{0}: alive={1} resurrectable={2} curable={3}")
  @MethodSource("predicateCases")
  @DisplayName("only the deceased can be raised, and only the undead can be cured")
  void statePredicatesMatchTheLifecycle(
      final PetLifeState state,
      final boolean alive,
      final boolean resurrectable,
      final boolean curable) {
    assertEquals(alive, state.isAlive(), "isAlive");
    assertEquals(resurrectable, state.isResurrectable(), "isResurrectable");
    assertEquals(curable, state.isCurable(), "isCurable");
  }

  @Test
  @DisplayName("a pet lost while undead can never be raised again")
  void lostIsTerminal() {
    assertFalse(PetLifeState.LOST.isResurrectable());
    assertFalse(PetLifeState.LOST.isAlive());
    assertTrue(PetLifeState.DECEASED.isResurrectable());
  }
}
