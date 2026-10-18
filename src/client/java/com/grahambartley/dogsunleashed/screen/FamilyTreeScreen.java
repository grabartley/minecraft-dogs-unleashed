package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.network.DogConnectionsListener;
import com.grahambartley.dogsunleashed.network.ModNetworkingClient;
import com.grahambartley.dogsunleashed.network.payload.ConnectionDogSyncData;
import com.grahambartley.dogsunleashed.network.payload.SyncDogConnectionsPayload;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class FamilyTreeScreen extends Screen implements DogConnectionsListener {

  private static final int NOTICE_COLOR = 0xFFFFAA55;
  private static final int HINT_COLOR = 0xFF888888;

  private final Screen parent;
  private final FamilyTreeModel model;
  private final FamilyTreeViewport viewport = new FamilyTreeViewport();
  private final FamilyTreeCanvasRenderer canvas = new FamilyTreeCanvasRenderer();
  private boolean draggingCanvas = false;

  private ButtonWidget toggleExpandButton;
  private ButtonWidget focusButton;

  public FamilyTreeScreen(final Screen parent, final ConnectionDogSyncData focusDog) {
    super(Text.translatable("screen.dogs-unleashed.family_tree.title"));
    this.parent = parent;
    this.model =
        new FamilyTreeModel(
            focusDog, id -> ModNetworkingClient.sendRequestDogConnections(UUID.fromString(id)));
  }

  @Override
  protected void init() {
    addDrawableChild(
        ButtonWidget.builder(
                Text.translatable("screen.dogs-unleashed.family_tree.back"),
                button -> MinecraftClient.getInstance().setScreen(parent))
            .dimensions(6, 6, 50, 20)
            .build());

    final int buttonX = FamilyTreeViewport.panelX(this.width) + 6;
    toggleExpandButton =
        addDrawableChild(
            ButtonWidget.builder(
                    Text.translatable("screen.dogs-unleashed.family_tree.expand"),
                    button -> {
                      model.toggleSelectedExpansion();
                      fitIfAuto();
                    })
                .dimensions(buttonX, this.height - 56, FamilyTreeDetailPanel.CONTENT_WIDTH, 20)
                .build());
    focusButton =
        addDrawableChild(
            ButtonWidget.builder(
                    Text.translatable("screen.dogs-unleashed.family_tree.focus"),
                    button -> refocusOnSelected())
                .dimensions(buttonX, this.height - 30, FamilyTreeDetailPanel.CONTENT_WIDTH, 20)
                .build());

    model.start();
    fitIfAuto();
  }

  @Override
  public void onDogConnections(final SyncDogConnectionsPayload payload) {
    model.accept(payload);
    fitIfAuto();
  }

  private void refocusOnSelected() {
    if (model.refocusOnSelected()) {
      viewport.reset();
      fitIfAuto();
    }
  }

  private void fitIfAuto() {
    if (!viewport.userAdjusted()) {
      viewport.fitTo(model.positions().values(), this.width, this.height);
    }
  }

  @Override
  public void render(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    super.render(context, mouseX, mouseY, delta);

    context.enableScissor(0, 0, FamilyTreeViewport.panelX(this.width), this.height);
    canvas.render(
        context, this.textRenderer, model, viewport, this.width, this.height, mouseX, mouseY);
    context.disableScissor();

    renderPanel(context);
    renderHeader(context);
    renderNotices(context);
  }

  private void renderPanel(final DrawContext context) {
    final String selectedId = model.selectedId();
    final ConnectionDogSyncData dog = model.node(selectedId);
    final boolean showActions = dog != null && !selectedId.equals(model.focusId());
    toggleExpandButton.visible = showActions;
    focusButton.visible = showActions;
    if (dog != null) {
      toggleExpandButton.setMessage(
          model.isExpanded(selectedId)
              ? Text.translatable("screen.dogs-unleashed.family_tree.collapse")
              : Text.translatable("screen.dogs-unleashed.family_tree.expand"));
    }
    FamilyTreeDetailPanel.render(context, this.textRenderer, dog, this.width, this.height);
  }

  private void renderHeader(final DrawContext context) {
    final ConnectionDogSyncData focus = model.node(model.focusId());
    final String focusName = focus != null ? focus.pet().name() : "";
    context.drawCenteredTextWithShadow(
        this.textRenderer,
        Text.translatable("screen.dogs-unleashed.family_tree.header", focusName),
        FamilyTreeViewport.canvasCenterX(this.width),
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
    if (model.nodeLimitHit()) {
      context.drawCenteredTextWithShadow(
          this.textRenderer,
          Text.translatable("screen.dogs-unleashed.family_tree.node_limit"),
          FamilyTreeViewport.canvasCenterX(this.width),
          noticeY,
          NOTICE_COLOR);
      noticeY += 12;
    }
    if (model.anyListTruncated()) {
      context.drawCenteredTextWithShadow(
          this.textRenderer,
          Text.translatable("screen.dogs-unleashed.family_tree.truncated"),
          FamilyTreeViewport.canvasCenterX(this.width),
          noticeY,
          NOTICE_COLOR);
    }
  }

  @Override
  public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
    if (super.mouseClicked(mouseX, mouseY, button)) {
      return true;
    }
    if (button != 0 || FamilyTreeViewport.isOverPanel(mouseX, this.width)) {
      return false;
    }
    final String clickedId =
        viewport.nodeAt(mouseX, mouseY, model.positions(), this.width, this.height);
    if (clickedId != null) {
      model.selectAndExpand(clickedId);
      fitIfAuto();
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
      viewport.panBy(deltaX, deltaY);
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
    if (FamilyTreeViewport.isOverPanel(mouseX, this.width)) {
      return false;
    }
    viewport.zoomAt(mouseX, mouseY, this.width, this.height, verticalAmount > 0);
    return true;
  }

  @Override
  public void removed() {
    canvas.clear();
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
}
