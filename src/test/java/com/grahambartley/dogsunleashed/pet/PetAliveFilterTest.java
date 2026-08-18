package com.grahambartley.dogsunleashed.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class PetAliveFilterTest {

  @ParameterizedTest(name = "{0} round-trips through its serializedName")
  @EnumSource(PetAliveFilter.class)
  @DisplayName("fromSerializedName round-trips every PetAliveFilter")
  void fromSerializedNameRoundTripsEveryFilter(final PetAliveFilter filter) {
    assertEquals(filter, PetAliveFilter.fromSerializedName(filter.serializedName()));
  }

  static Stream<Arguments> fallbackInputs() {
    return Stream.of(
        Arguments.of("null input", (String) null),
        Arguments.of("empty input", ""),
        Arguments.of("unknown identifier", "weird"));
  }

  @ParameterizedTest(name = "{0} -> ALIVE")
  @MethodSource("fallbackInputs")
  @DisplayName("fromSerializedName falls back to ALIVE for missing or unknown input")
  void fromSerializedNameFallsBackToAlive(final String label, final String input) {
    assertEquals(PetAliveFilter.ALIVE, PetAliveFilter.fromSerializedName(input));
  }

  static Stream<Arguments> appliesToCases() {
    return Stream.of(
        Arguments.of(PetAliveFilter.ALL, PetLifeState.LIVING, true),
        Arguments.of(PetAliveFilter.ALL, PetLifeState.UNDEAD, true),
        Arguments.of(PetAliveFilter.ALL, PetLifeState.DECEASED, true),
        Arguments.of(PetAliveFilter.ALL, PetLifeState.LOST, true),
        Arguments.of(PetAliveFilter.ALIVE, PetLifeState.LIVING, true),
        Arguments.of(PetAliveFilter.ALIVE, PetLifeState.UNDEAD, false),
        Arguments.of(PetAliveFilter.ALIVE, PetLifeState.DECEASED, false),
        Arguments.of(PetAliveFilter.ALIVE, PetLifeState.LOST, false),
        Arguments.of(PetAliveFilter.UNDEAD, PetLifeState.UNDEAD, true),
        Arguments.of(PetAliveFilter.UNDEAD, PetLifeState.LIVING, false),
        Arguments.of(PetAliveFilter.UNDEAD, PetLifeState.DECEASED, false),
        Arguments.of(PetAliveFilter.UNDEAD, PetLifeState.LOST, false),
        Arguments.of(PetAliveFilter.DECEASED, PetLifeState.DECEASED, true),
        Arguments.of(PetAliveFilter.DECEASED, PetLifeState.LOST, true),
        Arguments.of(PetAliveFilter.DECEASED, PetLifeState.LIVING, false),
        Arguments.of(PetAliveFilter.DECEASED, PetLifeState.UNDEAD, false));
  }

  @ParameterizedTest(name = "{0}.appliesTo({1}) = {2}")
  @MethodSource("appliesToCases")
  @DisplayName("appliesTo matches the filter against the pet's lifecycle state")
  void appliesToMatchesFilterAgainstLifeState(
      final PetAliveFilter filter, final PetLifeState lifeState, final boolean expected) {
    assertEquals(expected, filter.appliesTo(lifeState));
  }
}
