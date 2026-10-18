package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class CommandWheelRingTest {

  private static final int MAIN = 10;
  private static final double INNER = 40;
  private static final double OUTER = 100;
  private static final double HUB = 36;

  @Test
  @DisplayName("with no sub-actions the main ring keeps its ten sectors and no Actions sector")
  void noSubActionsLeavesTheWheelAsBefore() {
    final CommandWheelRing ring = new CommandWheelRing(MAIN, 0);
    assertFalse(ring.hasActionsSector());
    assertEquals(MAIN, ring.sectorCount());
    assertFalse(ring.isActionsSector(MAIN));
    assertFalse(ring.openSubRing());
    assertFalse(ring.isSubRingOpen());
  }

  @Test
  @DisplayName("sub-actions add one Actions sector after the existing ones")
  void actionsSectorComesLast() {
    final CommandWheelRing ring = new CommandWheelRing(MAIN, 3);
    assertEquals(MAIN + 1, ring.sectorCount());
    assertTrue(ring.isActionsSector(MAIN));
    for (int i = 0; i < MAIN; i++) {
      assertFalse(ring.isActionsSector(i), "existing sector " + i + " must keep its action");
    }
  }

  @Test
  @DisplayName("opening the Actions sector swaps the ring to the sub-actions")
  void openingShowsSubActions() {
    final CommandWheelRing ring = new CommandWheelRing(MAIN, 3);
    assertTrue(ring.openSubRing());
    assertTrue(ring.isSubRingOpen());
    assertEquals(3, ring.sectorCount());
    assertFalse(ring.isActionsSector(MAIN));
    assertFalse(ring.openSubRing());
  }

  @Test
  @DisplayName("Escape returns to the main ring once and does nothing more on the main ring")
  void escapeReturnsToMainRing() {
    final CommandWheelRing ring = new CommandWheelRing(MAIN, 2);
    ring.openSubRing();
    assertTrue(ring.returnToMainRing());
    assertFalse(ring.isSubRingOpen());
    assertEquals(MAIN + 1, ring.sectorCount());
    assertFalse(ring.returnToMainRing());
  }

  static Stream<Arguments> pointerPositions() {
    return Stream.of(
        Arguments.of("wheel centre", 0, 0, true),
        Arguments.of("inside the hub", 20, -20, true),
        Arguments.of("on the hub edge", 0, -HUB, false),
        Arguments.of("on the ring", 0, -70, false),
        Arguments.of("outside the wheel", 200, 0, false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("pointerPositions")
  @DisplayName("moving the mouse back into the hub returns to the main ring")
  void hubReturnsToMainRing(
      final String label, final double dx, final double dy, final boolean returns) {
    final CommandWheelRing ring = new CommandWheelRing(MAIN, 2);
    ring.openSubRing();
    assertEquals(returns, ring.returnToMainRingIfInHub(dx, dy, HUB));
    assertEquals(!returns, ring.isSubRingOpen());
  }

  @Test
  @DisplayName("the hub does nothing while the main ring is showing")
  void hubIgnoredOnMainRing() {
    final CommandWheelRing ring = new CommandWheelRing(MAIN, 2);
    assertFalse(ring.returnToMainRingIfInHub(0, 0, HUB));
  }

  @ParameterizedTest(name = "sub-action {0} of 3")
  @ValueSource(ints = {0, 1, 2})
  @DisplayName("each sub-action's sector centre selects it on the open sub-ring")
  void subRingSectorsSelectThemselves(final int sector) {
    final CommandWheelRing ring = new CommandWheelRing(MAIN, 3);
    ring.openSubRing();
    final double angle = -Math.PI / 2 + sector * 2 * Math.PI / ring.sectorCount();
    final double radius = (INNER + OUTER) / 2.0;
    assertEquals(
        sector,
        DogCommandWheelScreen.sectorIndexAt(
            Math.cos(angle) * radius, Math.sin(angle) * radius, ring.sectorCount(), INNER, OUTER));
  }
}
