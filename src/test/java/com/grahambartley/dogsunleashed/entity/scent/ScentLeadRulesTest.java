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
        Arguments.of("a mob within reach", ScentTargetKind.ENTITY, 1.0, 3.9, false, true),
        Arguments.of("a mob just out of reach", ScentTargetKind.ENTITY, 1.0, 4.1, true, false),
        Arguments.of("an ore right below", ScentTargetKind.BLOCK, 0.5, 9.0, false, true),
        Arguments.of("an ore two blocks over", ScentTargetKind.BLOCK, 2.0, 2.0, true, false),
        Arguments.of(
            "a dig site underfoot", ScentTargetKind.SUSPICIOUS_BLOCK, 1.5, 1.6, false, true),
        Arguments.of("a dig site nearby", ScentTargetKind.SUSPICIOUS_BLOCK, 1.6, 1.6, true, false),
        Arguments.of("a map mark underfoot", ScentTargetKind.LANDMARK, 0.4, 30.0, false, true),
        Arguments.of(
            "walking diagonally next to a mark", ScentTargetKind.LANDMARK, 0.9, 0.9, false, false),
        Arguments.of("walking past a map mark", ScentTargetKind.LANDMARK, 1.3, 1.3, false, false),
        Arguments.of(
            "stopped beside a mark it cannot stand on",
            ScentTargetKind.LANDMARK,
            1.4,
            1.4,
            true,
            true),
        Arguments.of(
            "stopped two blocks from a mark", ScentTargetKind.LANDMARK, 1.6, 1.6, true, false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("arrivals")
  @DisplayName("a mob counts as reached from a few blocks off, a block once the dog stands over it")
  void arrivalDependsOnTheTarget(
      final String label,
      final ScentTargetKind kind,
      final double horizontal,
      final double distance,
      final boolean stopped,
      final boolean arrived) {
    assertEquals(arrived, ScentLeadRules.hasArrived(kind, horizontal, distance, stopped));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "ENTITY, POINTING",
    "BLOCK, POINTING",
    "SUSPICIOUS_BLOCK, DIGGING",
    "LANDMARK, DIGGING"
  })
  @DisplayName("the dog digs at a dig site or a map mark and points at anything else")
  void arrivalPhaseDigsOnlyAtDigSites(final ScentTargetKind kind, final ScentPhase phase) {
    assertEquals(phase, ScentLeadRules.arrivalPhase(kind));
  }

  @ParameterizedTest(name = "{0} blocks away walks {1} of the way")
  @CsvSource({"0.0, 1.0", "10.0, 1.0", "16.0, 1.0", "32.0, 0.5", "1600.0, 0.01"})
  @DisplayName("a far target is approached one leg of at most 16 blocks at a time")
  void farTargetsAreWalkedInLegs(final double distance, final double fraction) {
    assertEquals(fraction, ScentLeadRules.legFraction(distance), 1.0E-9);
  }

  @ParameterizedTest(name = "{0} -> reach {1}")
  @CsvSource({"ENTITY, 1", "BLOCK, 1", "SUSPICIOUS_BLOCK, 1", "LANDMARK, 0"})
  @DisplayName("the path ends on a map mark but next to a mob or block")
  void pathEndsOnTheMarkOnly(final ScentTargetKind kind, final int reach) {
    assertEquals(reach, ScentLeadRules.pathReach(kind));
  }
}
