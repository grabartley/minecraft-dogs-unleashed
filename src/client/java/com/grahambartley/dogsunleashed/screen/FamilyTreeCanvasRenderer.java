package com.grahambartley.dogsunleashed.screen;

import static com.grahambartley.dogsunleashed.screen.FamilyTreeViewport.NODE_HEIGHT;
import static com.grahambartley.dogsunleashed.screen.FamilyTreeViewport.NODE_WIDTH;
import static com.grahambartley.dogsunleashed.screen.FamilyTreeViewport.nodeWorldCenterX;
import static com.grahambartley.dogsunleashed.screen.FamilyTreeViewport.nodeWorldCenterY;

import com.grahambartley.dogsunleashed.network.payload.ConnectionDogSyncData;
import com.grahambartley.dogsunleashed.screen.FamilyTreeLayout.NodePosition;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.Nullable;

final class FamilyTreeCanvasRenderer {

  private static final int CARD_BORDER = 0xFF666666;
  private static final int PORTRAIT_SIZE = 48;
  private static final int CARRIER_MARKER_SIZE = 5;
  private static final int CARRIER_MARKER_INSET = 3;
  private static final int EDGE_COLOR = 0xFF8A8A8A;
  private static final int MATE_EDGE_COLOR = 0xFFB08A5A;
  private static final int CARD_COLOR = 0xC0333333;
  private static final int CARD_FOCUS_COLOR = 0xC0446644;
  private static final int CARD_SELECTED_BORDER = 0xFFFFDD66;
  private static final int CARD_HOVER_BORDER = 0xFF9C8E52;
  private static final int CARD_DECEASED_BORDER = 0xFF555555;

  private final DogPortraitRenderer portraits = new DogPortraitRenderer();

  void render(
      final DrawContext context,
      final TextRenderer textRenderer,
      final FamilyTreeModel model,
      final FamilyTreeViewport viewport,
      final int screenWidth,
      final int screenHeight,
      final int mouseX,
      final int mouseY) {
    final int centerX = FamilyTreeViewport.canvasCenterX(screenWidth);
    final int centerY = screenHeight / 2;
    renderEdges(context, model, viewport, centerX, centerY);
    renderNodes(
        context,
        textRenderer,
        model,
        viewport,
        centerX,
        centerY,
        viewport.nodeAt(mouseX, mouseY, model.positions(), screenWidth, screenHeight),
        mouseX,
        mouseY);
  }

  void clear() {
    portraits.clear();
  }

  private static void renderEdges(
      final DrawContext context,
      final FamilyTreeModel model,
      final FamilyTreeViewport viewport,
      final int centerX,
      final int centerY) {
    final Map<String, NodePosition> positions = model.positions();
    final Set<String> visible = positions.keySet();
    final Map<String, Set<String>> parentsByChild =
        FamilyTreeLayout.visibleParents(model.parentIds(), visible);
    final Map<String, Set<String>> matesById =
        FamilyTreeLayout.visibleMates(model.relations(), visible);
    final int thickness = Math.max(1, (int) Math.round(2 * viewport.zoom()));

    for (final Map.Entry<String, Set<String>> mates : matesById.entrySet()) {
      final NodePosition a = positions.get(mates.getKey());
      for (final String mateId : mates.getValue()) {
        final NodePosition b = positions.get(mateId);
        if (a == null || b == null || mates.getKey().compareTo(mateId) >= 0) {
          continue;
        }
        final int y = viewport.worldToScreenY(nodeWorldCenterY(a), centerY);
        fillHorizontal(
            context,
            viewport.worldToScreenX(nodeWorldCenterX(a), centerX),
            viewport.worldToScreenX(nodeWorldCenterX(b), centerX),
            y,
            thickness,
            MATE_EDGE_COLOR);
      }
    }

    for (final Map.Entry<String, Set<String>> entry : parentsByChild.entrySet()) {
      final NodePosition child = positions.get(entry.getKey());
      if (child == null) {
        continue;
      }
      float anchorX = 0;
      float anchorBottomY = 0;
      int parentCount = 0;
      for (final String parentId : entry.getValue()) {
        final NodePosition parent = positions.get(parentId);
        if (parent != null) {
          anchorX += nodeWorldCenterX(parent);
          anchorBottomY = nodeWorldCenterY(parent) + NODE_HEIGHT / 2.0f;
          parentCount++;
        }
      }
      if (parentCount == 0) {
        continue;
      }
      anchorX /= parentCount;
      final float childTopY = nodeWorldCenterY(child) - NODE_HEIGHT / 2.0f;
      final float busY = (anchorBottomY + childTopY) / 2.0f;

      final int sAnchorX = viewport.worldToScreenX(anchorX, centerX);
      final int sChildX = viewport.worldToScreenX(nodeWorldCenterX(child), centerX);
      final int sAnchorBottom = viewport.worldToScreenY(anchorBottomY, centerY);
      final int sBusY = viewport.worldToScreenY(busY, centerY);
      final int sChildTop = viewport.worldToScreenY(childTopY, centerY);
      fillVertical(context, sAnchorX, sAnchorBottom, sBusY, thickness, EDGE_COLOR);
      fillHorizontal(context, sAnchorX, sChildX, sBusY, thickness, EDGE_COLOR);
      fillVertical(context, sChildX, sBusY, sChildTop + thickness, thickness, EDGE_COLOR);
    }
  }

  private void renderNodes(
      final DrawContext context,
      final TextRenderer textRenderer,
      final FamilyTreeModel model,
      final FamilyTreeViewport viewport,
      final int centerX,
      final int centerY,
      final @Nullable String hoveredId,
      final int mouseX,
      final int mouseY) {
    for (final Map.Entry<String, NodePosition> entry : model.positions().entrySet()) {
      final ConnectionDogSyncData dog = model.node(entry.getKey());
      if (dog == null) {
        continue;
      }
      final NodePosition pos = entry.getValue();
      final int left = viewport.worldToScreenX(nodeWorldCenterX(pos) - NODE_WIDTH / 2.0f, centerX);
      final int top = viewport.worldToScreenY(nodeWorldCenterY(pos) - NODE_HEIGHT / 2.0f, centerY);
      final int width = viewport.scaled(NODE_WIDTH);
      final int height = viewport.scaled(NODE_HEIGHT);

      final boolean isFocus = entry.getKey().equals(model.focusId());
      final boolean isSelected = entry.getKey().equals(model.selectedId());
      final boolean isHovered = entry.getKey().equals(hoveredId);
      context.fill(left, top, left + width, top + height, isFocus ? CARD_FOCUS_COLOR : CARD_COLOR);
      context.drawBorder(
          left, top, width, height, borderColor(isSelected, isHovered, dog.pet().alive()));
      if (FamilyTreeGeneLines.isCarrier(dog)) {
        final int markerSize = viewport.scaled(CARRIER_MARKER_SIZE);
        final int markerInset = viewport.scaled(CARRIER_MARKER_INSET);
        context.fill(
            left + width - markerInset - markerSize,
            top + markerInset,
            left + width - markerInset,
            top + markerInset + markerSize,
            0xFF000000 | FamilyTreeGeneLines.CARRIER_COLOR);
      }

      final int portraitSize = viewport.scaled(PORTRAIT_SIZE);
      final int portraitX = left + (width - portraitSize) / 2;
      portraits.draw(
          context,
          textRenderer,
          dog.pet(),
          portraitX,
          top + viewport.scaled(2),
          portraitSize,
          mouseX,
          mouseY);

      final var matrices = context.getMatrices();
      matrices.push();
      matrices.translate(left + width / 2.0f, top + height - viewport.scaled(12), 0);
      matrices.scale((float) viewport.zoom(), (float) viewport.zoom(), 1.0f);
      final String name = textRenderer.trimToWidth(dog.pet().name(), NODE_WIDTH - 6);
      context.drawCenteredTextWithShadow(
          textRenderer, name, 0, 0, dog.pet().alive() ? 0xFFFFFF : 0xAAAAAA);
      matrices.pop();
    }
  }

  static int borderColor(final boolean selected, final boolean hovered, final boolean alive) {
    if (selected) {
      return CARD_SELECTED_BORDER;
    }
    if (hovered) {
      return CARD_HOVER_BORDER;
    }
    return alive ? CARD_BORDER : CARD_DECEASED_BORDER;
  }

  private static void fillHorizontal(
      final DrawContext context,
      final int x1,
      final int x2,
      final int y,
      final int thickness,
      final int color) {
    context.fill(Math.min(x1, x2), y, Math.max(x1, x2) + thickness, y + thickness, color);
  }

  private static void fillVertical(
      final DrawContext context,
      final int x,
      final int y1,
      final int y2,
      final int thickness,
      final int color) {
    context.fill(x, Math.min(y1, y2), x + thickness, Math.max(y1, y2), color);
  }
}
