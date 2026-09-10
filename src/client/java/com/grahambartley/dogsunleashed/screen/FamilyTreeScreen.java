package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.network.DogConnectionsListener;
import com.grahambartley.dogsunleashed.network.ModNetworkingClient;
import com.grahambartley.dogsunleashed.network.payload.ConnectionDogSyncData;
import com.grahambartley.dogsunleashed.network.payload.SyncDogConnectionsPayload;
import com.grahambartley.dogsunleashed.screen.FamilyTreeLayout.NodePosition;
import com.grahambartley.dogsunleashed.screen.FamilyTreeLayout.Relations;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class FamilyTreeScreen extends Screen implements DogConnectionsListener {

  static final int H_SPACING = 78;
  static final int V_SPACING = 100;
  static final int NODE_WIDTH = 64;
  static final int NODE_HEIGHT = 72;
  static final int PANEL_WIDTH = 124;
  static final double MIN_ZOOM = 0.4;
  static final double MAX_ZOOM = 2.5;
  static final int MAX_VISIBLE_NODES = 64;
  private static final double ZOOM_STEP_FACTOR = 1.15;
  private static final int PORTRAIT_SIZE = 48;
  private static final int EDGE_COLOR = 0xFF8A8A8A;
  private static final int MATE_EDGE_COLOR = 0xFFB08A5A;
  private static final int CARD_COLOR = 0xC0333333;
  private static final int CARD_FOCUS_COLOR = 0xC0446644;
  private static final int CARD_SELECTED_BORDER = 0xFFFFDD66;
  private static final int CARD_HOVER_BORDER = 0xFF9C8E52;
  private static final int CARD_BORDER = 0xFF666666;
  private static final int CARD_DECEASED_BORDER = 0xFF555555;
  private static final int PANEL_COLOR = 0xE0202020;
  private static final int NOTICE_COLOR = 0xFFFFAA55;
  private static final int HINT_COLOR = 0xFF888888;
  private static final float LOW_HEALTH_COLOR_THRESHOLD = 0.5f;

  private final Screen parent;
  private final DogPortraitRenderer portraits = new DogPortraitRenderer();

  private final Map<String, ConnectionDogSyncData> nodesById = new HashMap<>();
  private final Map<String, Relations> relationsById = new HashMap<>();
  private final Set<String> expandedIds = new LinkedHashSet<>();
  private final Set<String> requestedIds = new LinkedHashSet<>();

  private String focusId;
  private @Nullable String selectedId;
  private @Nullable String lastExpandedId;
  private Map<String, NodePosition> positions = Map.of();
  private double zoom = 1.0;
  private double panX = 0;
  private double panY = 0;
  private boolean draggingCanvas = false;
  private boolean userAdjustedView = false;
  private boolean nodeLimitHit = false;
  private boolean anyListTruncated = false;

  private ButtonWidget toggleExpandButton;
  private ButtonWidget focusButton;

  public FamilyTreeScreen(final Screen parent, final ConnectionDogSyncData focusDog) {
    super(Text.translatable("screen.dogs-unleashed.family_tree.title"));
    this.parent = parent;
    this.focusId = focusDog.pet().petId();
    this.nodesById.put(focusId, focusDog);
    this.expandedIds.add(focusId);
    this.selectedId = focusId;
  }

  @Override
  protected void init() {
    addDrawableChild(
        ButtonWidget.builder(
                Text.translatable("screen.dogs-unleashed.family_tree.back"),
                button -> MinecraftClient.getInstance().setScreen(parent))
            .dimensions(6, 6, 50, 20)
            .build());

    toggleExpandButton =
        addDrawableChild(
            ButtonWidget.builder(
                    Text.translatable("screen.dogs-unleashed.family_tree.expand"),
                    button -> toggleSelectedExpansion())
                .dimensions(panelX(this.width) + 6, this.height - 56, PANEL_WIDTH - 12, 20)
                .build());
    focusButton =
        addDrawableChild(
            ButtonWidget.builder(
                    Text.translatable("screen.dogs-unleashed.family_tree.focus"),
                    button -> refocusOnSelected())
                .dimensions(panelX(this.width) + 6, this.height - 30, PANEL_WIDTH - 12, 20)
                .build());

    requestConnections(focusId);
    recomputeLayout();
  }

  @Override
  public void onDogConnections(final SyncDogConnectionsPayload payload) {
    final String id = payload.self().pet().petId();
    storeNode(payload.self());
    payload.parents().forEach(this::storeNode);
    payload.mates().forEach(this::storeNode);
    payload.siblings().forEach(this::storeNode);
    payload.children().forEach(this::storeNode);
    relationsById.put(
        id,
        new Relations(
            petIds(payload.parents()),
            petIds(payload.mates()),
            petIds(payload.siblings()),
            petIds(payload.children())));
    anyListTruncated |= payload.truncated();
    recomputeLayout();
  }

  private void storeNode(final ConnectionDogSyncData dog) {
    nodesById.put(dog.pet().petId(), dog);
  }

  private static List<String> petIds(final List<ConnectionDogSyncData> dogs) {
    return dogs.stream().map(dog -> dog.pet().petId()).toList();
  }

  private void recomputeLayout() {
    Set<String> visible = FamilyTreeLayout.computeVisible(focusId, expandedIds, relationsById);
    if (visible.size() > MAX_VISIBLE_NODES && lastExpandedId != null) {
      expandedIds.remove(lastExpandedId);
      nodeLimitHit = true;
      visible = FamilyTreeLayout.computeVisible(focusId, expandedIds, relationsById);
    }
    lastExpandedId = null;
    positions = FamilyTreeLayout.layout(focusId, expandedIds, relationsById);
    if (selectedId != null && !positions.containsKey(selectedId)) {
      selectedId = focusId;
    }
    if (!userAdjustedView) {
      autoFitView();
    }
  }

  private void autoFitView() {
    if (positions.isEmpty() || this.width == 0) {
      return;
    }
    float minX = Float.MAX_VALUE;
    float maxX = -Float.MAX_VALUE;
    float minY = Float.MAX_VALUE;
    float maxY = -Float.MAX_VALUE;
    for (final NodePosition pos : positions.values()) {
      minX = Math.min(minX, nodeWorldCenterX(pos) - NODE_WIDTH / 2.0f);
      maxX = Math.max(maxX, nodeWorldCenterX(pos) + NODE_WIDTH / 2.0f);
      minY = Math.min(minY, nodeWorldCenterY(pos) - NODE_HEIGHT / 2.0f);
      maxY = Math.max(maxY, nodeWorldCenterY(pos) + NODE_HEIGHT / 2.0f);
    }
    zoom = computeFitZoom(maxX - minX, maxY - minY, panelX(this.width) - 24, this.height - 70);
    panX = -((minX + maxX) / 2.0) * zoom;
    panY = -((minY + maxY) / 2.0) * zoom;
  }

  private void requestConnections(final String dogId) {
    if (requestedIds.add(dogId)) {
      ModNetworkingClient.sendRequestDogConnections(UUID.fromString(dogId));
    }
  }

  private void toggleSelectedExpansion() {
    if (selectedId == null) {
      return;
    }
    if (expandedIds.contains(selectedId) && !selectedId.equals(focusId)) {
      expandedIds.remove(selectedId);
      recomputeLayout();
      return;
    }
    expandDog(selectedId);
  }

  private void expandDog(final String dogId) {
    if (expandedIds.contains(dogId)) {
      return;
    }
    if (positions.size() >= MAX_VISIBLE_NODES) {
      nodeLimitHit = true;
      return;
    }
    nodeLimitHit = false;
    expandedIds.add(dogId);
    lastExpandedId = dogId;
    requestConnections(dogId);
    recomputeLayout();
  }

  private void refocusOnSelected() {
    if (selectedId == null || selectedId.equals(focusId)) {
      return;
    }
    focusId = selectedId;
    expandedIds.clear();
    expandedIds.add(focusId);
    requestedIds.clear();
    nodeLimitHit = false;
    anyListTruncated = false;
    userAdjustedView = false;
    panX = 0;
    panY = 0;
    requestConnections(focusId);
    recomputeLayout();
  }

  @Override
  public void render(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    super.render(context, mouseX, mouseY, delta);

    final int centerX = canvasCenterX(this.width);
    final int centerY = this.height / 2;

    context.enableScissor(0, 0, panelX(this.width), this.height);
    renderEdges(context, centerX, centerY);
    renderNodes(context, centerX, centerY, mouseX, mouseY);
    context.disableScissor();

    renderPanel(context);
    renderHeader(context);
    renderNotices(context);
  }

  private void renderHeader(final DrawContext context) {
    final ConnectionDogSyncData focus = nodesById.get(focusId);
    final String focusName = focus != null ? focus.pet().name() : "";
    context.drawCenteredTextWithShadow(
        this.textRenderer,
        Text.translatable("screen.dogs-unleashed.family_tree.header", focusName),
        canvasCenterX(this.width),
        10,
        0xFFFFFF);
    context.drawText(
        this.textRenderer,
        Text.translatable("screen.dogs-unleashed.family_tree.hint"),
        8,
        this.height - 12,
        HINT_COLOR,
        false);
  }

  private void renderNotices(final DrawContext context) {
    int noticeY = 24;
    if (nodeLimitHit) {
      context.drawCenteredTextWithShadow(
          this.textRenderer,
          Text.translatable("screen.dogs-unleashed.family_tree.node_limit"),
          canvasCenterX(this.width),
          noticeY,
          NOTICE_COLOR);
      noticeY += 12;
    }
    if (anyListTruncated) {
      context.drawCenteredTextWithShadow(
          this.textRenderer,
          Text.translatable("screen.dogs-unleashed.family_tree.truncated"),
          canvasCenterX(this.width),
          noticeY,
          NOTICE_COLOR);
    }
  }

  private void renderEdges(final DrawContext context, final int centerX, final int centerY) {
    final Set<String> visible = positions.keySet();
    final Map<String, Set<String>> parentsByChild =
        FamilyTreeLayout.visibleParents(relationsById, visible);
    final Map<String, Set<String>> matesById =
        FamilyTreeLayout.visibleMates(relationsById, visible);
    final int thickness = Math.max(1, (int) Math.round(2 * zoom));

    for (final Map.Entry<String, Set<String>> mates : matesById.entrySet()) {
      final NodePosition a = positions.get(mates.getKey());
      for (final String mateId : mates.getValue()) {
        final NodePosition b = positions.get(mateId);
        if (a == null || b == null || mates.getKey().compareTo(mateId) >= 0) {
          continue;
        }
        final int y = worldToScreenY(nodeWorldCenterY(a), centerY);
        fillHorizontal(
            context,
            worldToScreenX(nodeWorldCenterX(a), centerX),
            worldToScreenX(nodeWorldCenterX(b), centerX),
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

      final int sAnchorX = worldToScreenX(anchorX, centerX);
      final int sChildX = worldToScreenX(nodeWorldCenterX(child), centerX);
      final int sAnchorBottom = worldToScreenY(anchorBottomY, centerY);
      final int sBusY = worldToScreenY(busY, centerY);
      final int sChildTop = worldToScreenY(childTopY, centerY);
      fillVertical(context, sAnchorX, sAnchorBottom, sBusY, thickness, EDGE_COLOR);
      fillHorizontal(context, sAnchorX, sChildX, sBusY, thickness, EDGE_COLOR);
      fillVertical(context, sChildX, sBusY, sChildTop + thickness, thickness, EDGE_COLOR);
    }
  }

  private void renderNodes(
      final DrawContext context,
      final int centerX,
      final int centerY,
      final int mouseX,
      final int mouseY) {
    final String hoveredId = nodeAt(mouseX, mouseY, positions, centerX, centerY, panX, panY, zoom);
    for (final Map.Entry<String, NodePosition> entry : positions.entrySet()) {
      final ConnectionDogSyncData dog = nodesById.get(entry.getKey());
      if (dog == null) {
        continue;
      }
      final NodePosition pos = entry.getValue();
      final int left = worldToScreenX(nodeWorldCenterX(pos) - NODE_WIDTH / 2.0f, centerX);
      final int top = worldToScreenY(nodeWorldCenterY(pos) - NODE_HEIGHT / 2.0f, centerY);
      final int width = scaled(NODE_WIDTH);
      final int height = scaled(NODE_HEIGHT);

      final boolean isFocus = entry.getKey().equals(focusId);
      final boolean isSelected = entry.getKey().equals(selectedId);
      final boolean isHovered = entry.getKey().equals(hoveredId);
      context.fill(left, top, left + width, top + height, isFocus ? CARD_FOCUS_COLOR : CARD_COLOR);
      final int borderColor;
      if (isSelected) {
        borderColor = CARD_SELECTED_BORDER;
      } else if (isHovered) {
        borderColor = CARD_HOVER_BORDER;
      } else {
        borderColor = dog.pet().alive() ? CARD_BORDER : CARD_DECEASED_BORDER;
      }
      context.drawBorder(left, top, width, height, borderColor);

      final int portraitSize = scaled(PORTRAIT_SIZE);
      final int portraitX = left + (width - portraitSize) / 2;
      portraits.draw(
          context,
          this.textRenderer,
          dog.pet(),
          portraitX,
          top + scaled(2),
          portraitSize,
          mouseX,
          mouseY);

      final var matrices = context.getMatrices();
      matrices.push();
      matrices.translate(left + width / 2.0f, top + height - scaled(12), 0);
      matrices.scale((float) zoom, (float) zoom, 1.0f);
      final String name = this.textRenderer.trimToWidth(dog.pet().name(), NODE_WIDTH - 6);
      context.drawCenteredTextWithShadow(
          this.textRenderer, name, 0, 0, dog.pet().alive() ? 0xFFFFFF : 0xAAAAAA);
      matrices.pop();
    }
  }

  private void renderPanel(final DrawContext context) {
    final int x = panelX(this.width);
    context.fill(x, 0, this.width, this.height, PANEL_COLOR);
    context.fill(x, 0, x + 1, this.height, CARD_BORDER);

    final ConnectionDogSyncData dog = selectedId != null ? nodesById.get(selectedId) : null;
    final boolean hasSelection = dog != null;
    final boolean selectionIsFocus = hasSelection && selectedId.equals(focusId);
    toggleExpandButton.visible = hasSelection && !selectionIsFocus;
    focusButton.visible = hasSelection && !selectionIsFocus;
    if (!hasSelection) {
      return;
    }
    toggleExpandButton.setMessage(
        expandedIds.contains(selectedId)
            ? Text.translatable("screen.dogs-unleashed.family_tree.collapse")
            : Text.translatable("screen.dogs-unleashed.family_tree.expand"));

    final int textX = x + 6;
    int textY = 10;
    context.drawText(
        this.textRenderer,
        this.textRenderer.trimToWidth(dog.pet().name(), PANEL_WIDTH - 12),
        textX,
        textY,
        0xFFFFFF,
        true);
    textY += 14;
    context.drawText(
        this.textRenderer,
        DogBreedNames.displayName(dog.pet().breed(), dog.pet().composition()),
        textX,
        textY,
        0xAAAAAA,
        false);
    textY += 12;
    final String ownerName =
        dog.ownerName().isEmpty()
            ? Text.translatable("screen.dogs-unleashed.family_tree.owner_unknown").getString()
            : dog.ownerName();
    context.drawText(
        this.textRenderer,
        this.textRenderer.trimToWidth(
            Text.translatable("screen.dogs-unleashed.family_tree.owner", ownerName).getString(),
            PANEL_WIDTH - 12),
        textX,
        textY,
        0xAAAAAA,
        false);
    textY += 12;
    if (dog.pet().alive()) {
      final int healthColor =
          dog.pet().health() > dog.pet().maxHealth() * LOW_HEALTH_COLOR_THRESHOLD
              ? 0x55FF55
              : 0xFF5555;
      context.drawText(
          this.textRenderer,
          String.format("%.1f / %.1f ❤", dog.pet().health(), dog.pet().maxHealth()),
          textX,
          textY,
          healthColor,
          false);
      textY += 12;
      context.drawText(
          this.textRenderer,
          Text.translatable(
              dog.pet().baby()
                  ? "screen.dogs-unleashed.pet_details.age_baby"
                  : "screen.dogs-unleashed.pet_details.age_adult"),
          textX,
          textY,
          0xAAAAAA,
          false);
    } else {
      context.drawText(
          this.textRenderer,
          Text.translatable("screen.dogs-unleashed.pet_manager.deceased"),
          textX,
          textY,
          0xFF5555,
          false);
    }
  }

  @Override
  public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
    if (super.mouseClicked(mouseX, mouseY, button)) {
      return true;
    }
    if (button != 0 || isOverPanel(mouseX, this.width)) {
      return false;
    }
    final String clickedId =
        nodeAt(
            mouseX,
            mouseY,
            positions,
            canvasCenterX(this.width),
            this.height / 2,
            panX,
            panY,
            zoom);
    if (clickedId != null) {
      selectedId = clickedId;
      expandDog(clickedId);
      return true;
    }
    draggingCanvas = true;
    return true;
  }

  @Override
  public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
    if (button == 0) {
      draggingCanvas = false;
    }
    return super.mouseReleased(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseDragged(
      final double mouseX,
      final double mouseY,
      final int button,
      final double deltaX,
      final double deltaY) {
    if (draggingCanvas && button == 0) {
      userAdjustedView = true;
      panX += deltaX;
      panY += deltaY;
      return true;
    }
    return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
  }

  @Override
  public boolean mouseScrolled(
      final double mouseX,
      final double mouseY,
      final double horizontalAmount,
      final double verticalAmount) {
    if (isOverPanel(mouseX, this.width)) {
      return false;
    }
    userAdjustedView = true;
    final double newZoom =
        clampZoom(verticalAmount > 0 ? zoom * ZOOM_STEP_FACTOR : zoom / ZOOM_STEP_FACTOR);
    final double[] newPan =
        panAfterZoom(
            mouseX, mouseY, canvasCenterX(this.width), this.height / 2, panX, panY, zoom, newZoom);
    zoom = newZoom;
    panX = newPan[0];
    panY = newPan[1];
    return true;
  }

  @Override
  public void removed() {
    portraits.clear();
    super.removed();
  }

  @Override
  public void close() {
    MinecraftClient.getInstance().setScreen(parent);
  }

  @Override
  public boolean shouldPause() {
    return false;
  }

  private int worldToScreenX(final float worldX, final int centerX) {
    return (int) Math.round(centerX + panX + worldX * zoom);
  }

  private int worldToScreenY(final float worldY, final int centerY) {
    return (int) Math.round(centerY + panY + worldY * zoom);
  }

  private int scaled(final int size) {
    return Math.max(1, (int) Math.round(size * zoom));
  }

  private void fillHorizontal(
      final DrawContext context,
      final int x1,
      final int x2,
      final int y,
      final int thickness,
      final int color) {
    context.fill(Math.min(x1, x2), y, Math.max(x1, x2) + thickness, y + thickness, color);
  }

  private void fillVertical(
      final DrawContext context,
      final int x,
      final int y1,
      final int y2,
      final int thickness,
      final int color) {
    context.fill(x, Math.min(y1, y2), x + thickness, Math.max(y1, y2), color);
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
