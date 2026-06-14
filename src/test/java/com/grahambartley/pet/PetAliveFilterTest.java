package com.grahambartley.pet;

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
        Arguments.of(PetAliveFilter.ALL, true, true),
        Arguments.of(PetAliveFilter.ALL, false, true),
        Arguments.of(PetAliveFilter.ALIVE, true, true),
        Arguments.of(PetAliveFilter.ALIVE, false, false),
        Arguments.of(PetAliveFilter.DECEASED, true, false),
        Arguments.of(PetAliveFilter.DECEASED, false, true));
  }

  @ParameterizedTest(name = "{0}.appliesTo(alive={1}) = {2}")
  @MethodSource("appliesToCases")
  @DisplayName("appliesTo matches the filter against the entity's alive flag")
  void appliesToMatchesFilterAgainstAliveFlag(
      final PetAliveFilter filter, final boolean alive, final boolean expected) {
    assertEquals(expected, filter.appliesTo(alive));
  }
}
