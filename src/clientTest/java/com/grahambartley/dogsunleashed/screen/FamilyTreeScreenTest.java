package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.screen.FamilyTreeLayout.NodePosition;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class FamilyTreeScreenTest {

  private static final int CENTER_X = 200;
  private static final int CENTER_Y = 150;

  @ParameterizedTest(name = "zoom {0} clamps to {1}")
  @CsvSource({"0.1, 0.4", "0.4, 0.4", "1.0, 1.0", "2.5, 2.5", "5.0, 2.5"})
  @DisplayName("zoom stays inside its documented bounds")
  void zoomIsClamped(final double requested, final double expected) {
    assertEquals(expected, FamilyTreeScreen.clampZoom(requested), 0.0001);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("zoomScenarios")
  @DisplayName("zooming keeps the world point under the cursor fixed on screen")
  void zoomTowardCursorKeepsCursorPointStable(
      final String label,
      final double mouseX,
      final double mouseY,
      final double panX,
      final double panY,
      final double oldZoom,
      final double newZoom) {
    final double worldX = (mouseX - CENTER_X - panX) / oldZoom;
    final double worldY = (mouseY - CENTER_Y - panY) / oldZoom;

    final double[] newPan =
        FamilyTreeScreen.panAfterZoom(
            mouseX, mouseY, CENTER_X, CENTER_Y, panX, panY, oldZoom, newZoom);

    assertEquals(mouseX, CENTER_X + newPan[0] + worldX * newZoom, 0.0001, label + " x");
    assertEquals(mouseY, CENTER_Y + newPan[1] + worldY * newZoom, 0.0001, label + " y");
  }

  static Stream<Arguments> zoomScenarios() {
    return Stream.of(
        Arguments.of("zoom in at screen center", 200.0, 150.0, 0.0, 0.0, 1.0, 1.15),
        Arguments.of("zoom in off-center", 320.0, 90.0, 24.0, -18.0, 1.0, 1.15),
        Arguments.of("zoom out while panned", 100.0, 220.0, -60.0, 35.0, 2.0, 1.5),
        Arguments.of("repeated zoom out at min bound", 10.0, 10.0, 5.0, 5.0, 0.46, 0.4));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("nodeHits")
  @DisplayName("clicks inside a node card resolve to that dog")
  void nodeHitTestingResolvesCards(
      final String label,
      final double mouseX,
      final double mouseY,
      final double zoom,
      final String expectedId) {
    final Map<String, NodePosition> positions =
        Map.of(
            "center", new NodePosition(0, 0),
            "right", new NodePosition(1, 0),
            "above", new NodePosition(0, -1));

    assertEquals(
        expectedId,
        FamilyTreeScreen.nodeAt(mouseX, mouseY, positions, CENTER_X, CENTER_Y, 0, 0, zoom),
        label);
  }

  static Stream<Arguments> nodeHits() {
    return Stream.of(
        Arguments.of("center of the focus card", 200.0, 150.0, 1.0, "center"),
        Arguments.of(
            "center of the right-hand card",
            200.0 + FamilyTreeScreen.H_SPACING,
            150.0,
            1.0,
            "right"),
        Arguments.of(
            "center of the card one generation up",
            200.0,
            150.0 - FamilyTreeScreen.V_SPACING,
            1.0,
            "above"),
        Arguments.of("zoomed-out center still hits", 200.0, 150.0, 0.5, "center"),
        Arguments.of(
            "half a card left of the focus edge at half zoom",
            200.0 - FamilyTreeScreen.NODE_WIDTH * 0.5 / 2 - 1,
            150.0,
            0.5,
            null),
        Arguments.of(
            "empty canvas between rows",
            200.0,
            150.0 + FamilyTreeScreen.V_SPACING / 2.0,
            1.0,
            null));
  }

  @ParameterizedTest(name = "mouseX {0} on a {1}px screen")
  @CsvSource({"310, 400, true", "400, 400, true", "275, 400, false", "0, 400, false"})
  @DisplayName("the side panel claims exactly the mouse positions over it")
  void panelHitTesting(final double mouseX, final int screenWidth, final boolean expected) {
    assertEquals(expected, FamilyTreeScreen.isOverPanel(mouseX, screenWidth));
  }

  @ParameterizedTest(name = "screen width {0}")
  @CsvSource({"320", "400", "854", "1920"})
  @DisplayName("the canvas centers in the space left of the panel")
  void canvasCentersBesideThePanel(final int screenWidth) {
    final int canvasCenter = FamilyTreeScreen.canvasCenterX(screenWidth);
    final int panelStart = FamilyTreeScreen.panelX(screenWidth);

    assertEquals(panelStart / 2, canvasCenter);
    assertTrue(canvasCenter < panelStart, "canvas center must sit left of the panel");
    assertFalse(FamilyTreeScreen.isOverPanel(canvasCenter, screenWidth));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("fitZoomCases")
  @DisplayName("auto-fit zoom fills the canvas without upscaling past 1:1 or below min zoom")
  void fitZoomFramesTheTree(
      final String label,
      final double boundsW,
      final double boundsH,
      final double canvasW,
      final double canvasH,
      final double expected) {
    assertEquals(
        expected,
        FamilyTreeScreen.computeFitZoom(boundsW, boundsH, canvasW, canvasH),
        0.0001,
        label);
  }

  static Stream<Arguments> fitZoomCases() {
    return Stream.of(
        Arguments.of("tree smaller than canvas stays 1:1", 200.0, 150.0, 400.0, 300.0, 1.0),
        Arguments.of("wide tree scales to canvas width", 800.0, 100.0, 400.0, 300.0, 0.5),
        Arguments.of("tall tree scales to canvas height", 100.0, 600.0, 400.0, 300.0, 0.5),
        Arguments.of("enormous tree floors at min zoom", 10000.0, 10000.0, 400.0, 300.0, 0.4),
        Arguments.of("degenerate empty bounds stay 1:1", 0.0, 0.0, 400.0, 300.0, 1.0));
  }

  @Test
  @DisplayName("the node budget matches the documented cap")
  void nodeBudgetIsDocumented() {
    assertEquals(64, FamilyTreeScreen.MAX_VISIBLE_NODES);
  }

  @Test
  @DisplayName("an empty canvas resolves to no node")
  void emptyCanvasResolvesToNoNode() {
    assertNull(FamilyTreeScreen.nodeAt(200, 150, Map.of(), CENTER_X, CENTER_Y, 0, 0, 1.0));
  }
}
