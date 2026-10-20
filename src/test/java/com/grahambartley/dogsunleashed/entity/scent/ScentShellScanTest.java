package com.grahambartley.dogsunleashed.entity.scent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ScentShellScanTest {

  static Stream<Arguments> volumes() {
    return Stream.of(
        Arguments.of(0, 0),
        Arguments.of(1, 0),
        Arguments.of(3, 2),
        Arguments.of(5, 1),
        Arguments.of(8, 3));
  }

  @ParameterizedTest(name = "radius {0}, vertical reach {1}")
  @MethodSource("volumes")
  @DisplayName("a scan with no match visits every spot in the box exactly once, then gives up")
  void visitsTheWholeBoxOnce(final int radius, final int verticalReach) {
    final Set<List<Integer>> expected = new HashSet<>();
    for (int x = -radius; x <= radius; x++) {
      for (int y = -verticalReach; y <= verticalReach; y++) {
        for (int z = -radius; z <= radius; z++) {
          expected.add(List.of(x, y, z));
        }
      }
    }
    final List<List<Integer>> visited = new ArrayList<>();
    final ScentShellScan scan = new ScentShellScan(radius, verticalReach);

    ScentShellScan.Status status = ScentShellScan.Status.SEARCHING;
    while (status == ScentShellScan.Status.SEARCHING) {
      status =
          scan.advance(
              7,
              (dx, dy, dz) -> {
                visited.add(List.of(dx, dy, dz));
                return false;
              });
    }

    assertEquals(ScentShellScan.Status.EXHAUSTED, status);
    assertEquals(expected.size(), visited.size(), "every spot is visited once");
    assertEquals(expected, new HashSet<>(visited));
    assertNull(scan.found());
  }

  @ParameterizedTest(name = "budget {0}")
  @MethodSource("budgets")
  @DisplayName("no single tick probes more spots than its budget")
  void neverProbesPastTheBudget(final int budget) {
    final ScentShellScan scan = new ScentShellScan(6, 2);
    while (scan.status() == ScentShellScan.Status.SEARCHING) {
      final int[] probes = {0};
      scan.advance(
          budget,
          (dx, dy, dz) -> {
            probes[0]++;
            return false;
          });
      assertTrue(probes[0] <= budget, "probed " + probes[0] + " with a budget of " + budget);
    }
  }

  static Stream<Integer> budgets() {
    return Stream.of(1, 5, 64, 4096);
  }

  @Test
  @DisplayName("spots are searched from the dog outward, one ring at a time")
  void searchesNearestRingsFirst() {
    final ScentShellScan scan = new ScentShellScan(4, 0);
    final List<Integer> rings = new ArrayList<>();
    while (scan.status() == ScentShellScan.Status.SEARCHING) {
      scan.advance(
          3,
          (dx, dy, dz) -> {
            rings.add(Math.max(Math.abs(dx), Math.abs(dz)));
            return false;
          });
    }
    for (int i = 1; i < rings.size(); i++) {
      assertTrue(rings.get(i) >= rings.get(i - 1), "ring order broke at probe " + i);
    }
  }

  @Test
  @DisplayName("a match in a nearer ring wins over one further out")
  void nearerRingWins() {
    final ScentShellScan scan = new ScentShellScan(10, 0);
    final Set<List<Integer>> targets = Set.of(List.of(0, 0, 7), List.of(-2, 0, 1));

    runToEnd(scan, targets);

    assertEquals(new ScentShellScan.Offset(-2, 0, 1), scan.found());
  }

  @Test
  @DisplayName("within one ring the straight-line nearest match wins over the corner")
  void nearestWithinTheRingWins() {
    final ScentShellScan scan = new ScentShellScan(10, 0);
    final Set<List<Integer>> targets = Set.of(List.of(-3, 0, -3), List.of(3, 0, 0));

    runToEnd(scan, targets);

    assertEquals(new ScentShellScan.Offset(3, 0, 0), scan.found());
  }

  @Test
  @DisplayName("once a ring holds a match the scan stops before the next ring")
  void stopsAtTheEndOfTheMatchingRing() {
    final ScentShellScan scan = new ScentShellScan(10, 1);
    final int[] probes = {0};

    ScentShellScan.Status status = ScentShellScan.Status.SEARCHING;
    while (status == ScentShellScan.Status.SEARCHING) {
      status =
          scan.advance(
              50,
              (dx, dy, dz) -> {
                probes[0]++;
                return dx == 2 && dy == 0 && dz == 0;
              });
    }

    assertEquals(ScentShellScan.Status.FOUND, status);
    assertEquals(5 * 5 * 3, probes[0], "rings 0 to 2 across three layers");
  }

  @Test
  @DisplayName("each ring holds every cell on its square edge")
  void ringSizesMatchTheirSquareEdge() {
    for (int ring = 0; ring <= 6; ring++) {
      final int side = 2 * ring + 1;
      final int inner = Math.max(0, side - 2);
      assertEquals(side * side - inner * inner, ScentShellScan.cellsInShell(ring), "ring " + ring);
    }
  }

  @Test
  @DisplayName("a negative radius or reach is refused")
  void negativeSizesAreRefused() {
    assertThrows(IllegalArgumentException.class, () -> new ScentShellScan(-1, 0));
    assertThrows(IllegalArgumentException.class, () -> new ScentShellScan(0, -1));
  }

  private static void runToEnd(final ScentShellScan scan, final Set<List<Integer>> targets) {
    while (scan.status() == ScentShellScan.Status.SEARCHING) {
      scan.advance(16, (dx, dy, dz) -> targets.contains(List.of(dx, dy, dz)));
    }
  }
}
