package com.grahambartley.dogsunleashed.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BreedingOwnerResolverTest {

  private static final UUID PRIMARY = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID SECONDARY = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID STRANGER = UUID.fromString("33333333-3333-3333-3333-333333333333");

  static Stream<Arguments> inheritanceCases() {
    return Stream.of(
        Arguments.of("primary present, secondary present -> primary", PRIMARY, SECONDARY, PRIMARY),
        Arguments.of("primary present, secondary null -> primary", PRIMARY, null, PRIMARY),
        Arguments.of("primary null, secondary present -> secondary", null, SECONDARY, SECONDARY),
        Arguments.of("primary null, secondary null -> null", null, null, null));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("inheritanceCases")
  @DisplayName("inherited owner uuid prefers the primary parent and falls back to secondary")
  void resolveInheritedOwnerUuidPrefersPrimary(
      final String label, final UUID primary, final UUID secondary, final UUID expected) {
    assertEquals(expected, BreedingOwnerResolver.resolveInheritedOwnerUuid(primary, secondary));
  }

  static Stream<Arguments> newbornCases() {
    return Stream.of(
        Arguments.of("primary owner bred them -> primary", PRIMARY, PRIMARY, SECONDARY, PRIMARY),
        Arguments.of(
            "secondary owner bred them -> secondary", SECONDARY, PRIMARY, SECONDARY, SECONDARY),
        Arguments.of("stranger bred them -> primary", STRANGER, PRIMARY, SECONDARY, PRIMARY),
        Arguments.of(
            "stranger, no primary owner -> secondary", STRANGER, null, SECONDARY, SECONDARY),
        Arguments.of("nobody bred them -> primary", null, PRIMARY, SECONDARY, PRIMARY),
        Arguments.of("stranger, no owners -> null", STRANGER, null, null, null));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("newbornCases")
  @DisplayName("a newborn goes to the breeding player only when they own a parent")
  void resolveNewbornOwnerUuidNeverPicksAStranger(
      final String label,
      final UUID lovingPlayer,
      final UUID primary,
      final UUID secondary,
      final UUID expected) {
    assertEquals(
        expected, BreedingOwnerResolver.resolveNewbornOwnerUuid(lovingPlayer, primary, secondary));
  }
}
