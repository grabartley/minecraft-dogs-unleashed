package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.screen.FamilyTreeLayout.NodePosition;
import java.util.Collection;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

final class FamilyTreeViewport {

  static final int H_SPACING = 78;
  static final int V_SPACING = 100;
  static final int NODE_WIDTH = 64;
  static final int NODE_HEIGHT = 72;
  static final int PANEL_WIDTH = 124;
  static final double MIN_ZOOM = 0.4;
  static final double MAX_ZOOM = 2.5;
  private static final double ZOOM_STEP_FACTOR = 1.15;

  private double zoom = 1.0;
  private double panX = 0;
  private double panY = 0;
  private boolean userAdjusted = false;

  double zoom() {
    return zoom;
  }

  double panX() {
    return panX;
  }

  double panY() {
    return panY;
  }

  boolean userAdjusted() {
    return userAdjusted;
  }

  void reset() {
    userAdjusted = false;
    panX = 0;
    panY = 0;
  }

  void fitTo(
      final Collection<NodePosition> positions, final int screenWidth, final int screenHeight) {
    if (positions.isEmpty() || screenWidth == 0) {
      return;
    }
    float minX = Float.MAX_VALUE;
    float maxX = -Float.MAX_VALUE;
    float minY = Float.MAX_VALUE;
    float maxY = -Float.MAX_VALUE;
    for (final NodePosition pos : positions) {
      minX = Math.min(minX, nodeWorldCenterX(pos) - NODE_WIDTH / 2.0f);
      maxX = Math.max(maxX, nodeWorldCenterX(pos) + NODE_WIDTH / 2.0f);
      minY = Math.min(minY, nodeWorldCenterY(pos) - NODE_HEIGHT / 2.0f);
      maxY = Math.max(maxY, nodeWorldCenterY(pos) + NODE_HEIGHT / 2.0f);
    }
    zoom = computeFitZoom(maxX - minX, maxY - minY, panelX(screenWidth) - 24, screenHeight - 70);
    panX = -((minX + maxX) / 2.0) * zoom;
    panY = -((minY + maxY) / 2.0) * zoom;
  }

  void panBy(final double deltaX, final double deltaY) {
    userAdjusted = true;
    panX += deltaX;
    panY += deltaY;
  }

  void zoomAt(
      final double mouseX,
      final double mouseY,
      final int screenWidth,
      final int screenHeight,
      final boolean zoomIn) {
    userAdjusted = true;
    final double newZoom = clampZoom(zoomIn ? zoom * ZOOM_STEP_FACTOR : zoom / ZOOM_STEP_FACTOR);
    final double[] newPan =
        panAfterZoom(
            mouseX,
            mouseY,
            canvasCenterX(screenWidth),
            screenHeight / 2,
            panX,
            panY,
            zoom,
            newZoom);
    zoom = newZoom;
    panX = newPan[0];
    panY = newPan[1];
  }

  int worldToScreenX(final float worldX, final int centerX) {
    return (int) Math.round(centerX + panX + worldX * zoom);
  }

  int worldToScreenY(final float worldY, final int centerY) {
    return (int) Math.round(centerY + panY + worldY * zoom);
  }

  int scaled(final int size) {
    return Math.max(1, (int) Math.round(size * zoom));
  }

  @Nullable
  String nodeAt(
      final double mouseX,
      final double mouseY,
      final Map<String, NodePosition> positions,
      final int screenWidth,
      final int screenHeight) {
    return nodeAt(
        mouseX, mouseY, positions, canvasCenterX(screenWidth), screenHeight / 2, panX, panY, zoom);
  }

  static float nodeWorldCenterX(final NodePosition pos) {
    return pos.x() * H_SPACING;
  }

  static float nodeWorldCenterY(final NodePosition pos) {
    return pos.depth() * V_SPACING;
  }

  static int panelX(final int screenWidth) {
    return screenWidth - PANEL_WIDTH;
  }

  static int canvasCenterX(final int screenWidth) {
    return panelX(screenWidth) / 2;
  }

  static boolean isOverPanel(final double mouseX, final int screenWidth) {
    return mouseX >= panelX(screenWidth);
  }

  static double clampZoom(final double zoom) {
    return Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
  }

  static double computeFitZoom(
      final double boundsWidth,
      final double boundsHeight,
      final double canvasWidth,
      final double canvasHeight) {
    final double fit =
        Math.min(
            1.0,
            Math.min(
                canvasWidth / Math.max(boundsWidth, 1.0),
                canvasHeight / Math.max(boundsHeight, 1.0)));
    return clampZoom(fit);
  }

  static double[] panAfterZoom(
      final double mouseX,
      final double mouseY,
      final int centerX,
      final int centerY,
      final double panX,
      final double panY,
      final double oldZoom,
      final double newZoom) {
    final double worldX = (mouseX - centerX - panX) / oldZoom;
    final double worldY = (mouseY - centerY - panY) / oldZoom;
    return new double[] {mouseX - centerX - worldX * newZoom, mouseY - centerY - worldY * newZoom};
  }

  static @Nullable String nodeAt(
      final double mouseX,
      final double mouseY,
      final Map<String, NodePosition> positions,
      final int centerX,
      final int centerY,
      final double panX,
      final double panY,
      final double zoom) {
    for (final Map.Entry<String, NodePosition> entry : positions.entrySet()) {
      final double left =
          centerX + panX + (nodeWorldCenterX(entry.getValue()) - NODE_WIDTH / 2.0) * zoom;
      final double top =
          centerY + panY + (nodeWorldCenterY(entry.getValue()) - NODE_HEIGHT / 2.0) * zoom;
      if (mouseX >= left
          && mouseX < left + NODE_WIDTH * zoom
          && mouseY >= top
          && mouseY < top + NODE_HEIGHT * zoom) {
        return entry.getKey();
      }
    }
    return null;
  }
}
