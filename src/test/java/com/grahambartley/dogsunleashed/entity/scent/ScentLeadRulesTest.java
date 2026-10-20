package com.grahambartley.dogsunleashed.entity.scent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class ScentLeadRulesTest {

  @ParameterizedTest(name = "owner {0} blocks behind waits={1}")
  @CsvSource({"0.0, false", "11.9, false", "12.0, false", "12.1, true", "40.0, true"})
  @DisplayName("the dog waits once its owner is more than 12 blocks behind")
  void waitsForAnOwnerFallingBehind(final double ownerDistance, final boolean waits) {
    assertEquals(waits, ScentLeadRules.shouldWaitForOwner(ownerDistance));
  }

  static Stream<Arguments> arrivals() {
    return Stream.of(
        Arguments.of("a mob within reach", ScentTargetKind.ENTITY, 1.0, 2.0, true),
        Arguments.of("a mob just out of reach", ScentTargetKind.ENTITY, 1.0, 2.6, false),
        Arguments.of("an ore right below", ScentTargetKind.BLOCK, 0.5, 9.0, true),
        Arguments.of("an ore two blocks over", ScentTargetKind.BLOCK, 2.0, 2.0, false),
        Arguments.of("a dig site underfoot", ScentTargetKind.SUSPICIOUS_BLOCK, 1.5, 1.6, true),
        Arguments.of("a dig site nearby", ScentTargetKind.SUSPICIOUS_BLOCK, 1.6, 1.6, false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("arrivals")
  @DisplayName("a mob counts as reached up close, a block once the dog stands over it")
  void arrivalDependsOnTheTarget(
      final String label,
      final ScentTargetKind kind,
      final double horizontal,
      final double distance,
      final boolean arrived) {
    assertEquals(arrived, ScentLeadRules.hasArrived(kind, horizontal, distance));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"ENTITY, POINTING", "BLOCK, POINTING", "SUSPICIOUS_BLOCK, DIGGING"})
  @DisplayName("the dog digs at a dig site and points at anything else")
  void arrivalPhaseDigsOnlyAtDigSites(final ScentTargetKind kind, final ScentPhase phase) {
    assertEquals(phase, ScentLeadRules.arrivalPhase(kind));
  }
}
