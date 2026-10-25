package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogWheelAction;
import com.grahambartley.dogsunleashed.entity.DogWheelIcons;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.network.ModNetworkingClient;
import com.grahambartley.dogsunleashed.network.payload.OpenCommandWheelPayload;
import com.grahambartley.dogsunleashed.network.payload.WheelSubActionState;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class DogCommandWheelScreen extends Screen {

  private static final DogWheelAction[] ACTIONS = DogWheelAction.values();
  private static final String ACTIONS_SECTOR_KEY = "command.dogs-unleashed.actions";

  static final int OUTER_RADIUS = 60;
  static final int INNER_RADIUS = 26;
  static final int CAPTION_GAP = 6;
  private static final int INNER_DETECT_MARGIN = 4;
  private static final int OUTER_DETECT_MARGIN = 14;
  private static final float SECTOR_GAP_DEGREES = 1.5f;
  private static final float TESSELLATION_STEP_DEGREES = 2.5f;
  static final int ICON_SIZE = 16;

  private static final int BACKGROUND_DIM_COLOR = 0x40000000;
  private static final int SECTOR_COLOR = 0xA8101014;
  private static final int SECTOR_HOVERED_COLOR = 0xE0343440;
  private static final int SECTOR_ACTIVE_COLOR = 0xC81E3A2A;
  private static final int SECTOR_ACTIVE_HOVERED_COLOR = 0xE02C543C;
  private static final int SECTOR_DISABLED_COLOR = 0x50101014;
  private static final int LABEL_COLOR = 0xFFFFFF;
  private static final int SUBTITLE_COLOR = 0xAAAAAA;
  private static final int DISABLED_HINT_COLOR = 0xFF8888;

  private final int dogEntityId;
  private final UUID dogId;
  private final boolean hasBed;
  private final String dogName;
  private final String rideRefusalKey;
  private final List<WheelSubActionState> subEntries;
  private final CommandWheelRing ring;
  private DogCommand currentCommand;
  private int rejectedIndex = -1;

  public DogCommandWheelScreen(final OpenCommandWheelPayload payload) {
    super(Text.translatable("screen.dogs-unleashed.command_wheel.title"));
    this.dogEntityId = payload.entityId();
    this.dogId = payload.dogId();
    this.hasBed = payload.hasBed();
    this.dogName = payload.dogName();
    this.rideRefusalKey = payload.rideRefusalKey();
    this.currentCommand = DogCommand.fromId(payload.currentCommandId());
    this.subEntries = payload.subActions();
    this.ring = new CommandWheelRing(ACTIONS.length, this.subEntries.size());
  }

  @Override
  public boolean shouldPause() {
    return false;
  }

  @Override
  public void renderBackground(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    context.fill(0, 0, this.width, this.height, BACKGROUND_DIM_COLOR);
  }

  @Override
  public void render(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    super.render(context, mouseX, mouseY, delta);

    if (this.client == null || this.client.world == null) {
      return;
    }
    if (this.client.world.getEntityById(this.dogEntityId) instanceof UnleashedDogEntity dog) {
      this.currentCommand = dog.getCommand();
    } else {
      this.close();
      return;
    }

    final int hoveredIndex = this.hoveredSector(mouseX, mouseY);

    this.renderRing(context, hoveredIndex);
    this.renderIcons(context);
    if (hoveredIndex >= 0) {
      this.rejectedIndex = -1;
    }
    this.renderCaption(context, hubIndex(hoveredIndex, this.rejectedIndex));
  }

  private void renderRing(final DrawContext context, final int hoveredIndex) {
    final float windowScale = (float) this.client.getWindow().getScaleFactor();
    context.getMatrices().push();
    context.getMatrices().scale(1f / windowScale, 1f / windowScale, 1f);
    final Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
    final float centerX = this.width / 2f * windowScale;
    final float centerY = this.height / 2f * windowScale;

    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    RenderSystem.disableCull();
    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
    final BufferBuilder buffer =
        Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

    final int sectorCount = this.ring.sectorCount();
    final float sectorSpan = 360f / sectorCount;
    for (int i = 0; i < sectorCount; i++) {
      final float startDeg = -90f + (i - 0.5f) * sectorSpan + SECTOR_GAP_DEGREES / 2f;
      final float endDeg = startDeg + sectorSpan - SECTOR_GAP_DEGREES;
      appendSector(
          buffer,
          matrix,
          centerX,
          centerY,
          INNER_RADIUS * windowScale,
          OUTER_RADIUS * windowScale,
          startDeg,
          endDeg,
          this.sectorColor(i, hoveredIndex));
    }

    BufferRenderer.drawWithGlobalProgram(buffer.end());
    RenderSystem.enableCull();
    RenderSystem.disableBlend();
    context.getMatrices().pop();
  }

  private static void appendSector(
      final BufferBuilder buffer,
      final Matrix4f matrix,
      final float centerX,
      final float centerY,
      final float innerRadius,
      final float outerRadius,
      final float startDeg,
      final float endDeg,
      final int argb) {
    final int steps = Math.max(1, MathHelper.ceil((endDeg - startDeg) / TESSELLATION_STEP_DEGREES));
    for (int i = 0; i < steps; i++) {
      final float angle1 = (float) Math.toRadians(startDeg + (endDeg - startDeg) * i / steps);
      final float angle2 = (float) Math.toRadians(startDeg + (endDeg - startDeg) * (i + 1) / steps);
      final float cos1 = MathHelper.cos(angle1);
      final float sin1 = MathHelper.sin(angle1);
      final float cos2 = MathHelper.cos(angle2);
      final float sin2 = MathHelper.sin(angle2);
      buffer
          .vertex(matrix, centerX + outerRadius * cos1, centerY + outerRadius * sin1, 0)
          .color(argb);
      buffer
          .vertex(matrix, centerX + innerRadius * cos1, centerY + innerRadius * sin1, 0)
          .color(argb);
      buffer
          .vertex(matrix, centerX + innerRadius * cos2, centerY + innerRadius * sin2, 0)
          .color(argb);
      buffer
          .vertex(matrix, centerX + outerRadius * cos2, centerY + outerRadius * sin2, 0)
          .color(argb);
    }
  }

  private int sectorColor(final int index, final int hoveredIndex) {
    if (this.isDisabledAt(index)) {
      return SECTOR_DISABLED_COLOR;
    }
    final boolean active = this.isActiveAt(index);
    final boolean hovered = index == hoveredIndex;
    if (active && hovered) {
      return SECTOR_ACTIVE_HOVERED_COLOR;
    }
    if (active) {
      return SECTOR_ACTIVE_COLOR;
    }
    return hovered ? SECTOR_HOVERED_COLOR : SECTOR_COLOR;
  }

  private void renderIcons(final DrawContext context) {
    final float iconRadius = (INNER_RADIUS + OUTER_RADIUS) / 2f;
    final int sectorCount = this.ring.sectorCount();
    final float sectorSpan = (float) (2 * Math.PI / sectorCount);
    for (int i = 0; i < sectorCount; i++) {
      final float angle = (float) (-Math.PI / 2 + i * sectorSpan);
      final int iconX =
          Math.round(this.width / 2f + iconRadius * MathHelper.cos(angle)) - ICON_SIZE / 2;
      final int iconY =
          Math.round(this.height / 2f + iconRadius * MathHelper.sin(angle)) - ICON_SIZE / 2;
      context.drawGuiTexture(this.iconAt(i), iconX, iconY, ICON_SIZE, ICON_SIZE);
    }
  }

  static int hubIndex(final int hoveredIndex, final int rejectedIndex) {
    return hoveredIndex >= 0 ? hoveredIndex : rejectedIndex;
  }

  public static boolean isOpenFor(final int entityId) {
    return MinecraftClient.getInstance().currentScreen instanceof DogCommandWheelScreen wheel
        && wheel.dogEntityId == entityId;
  }

  private void renderCaption(final DrawContext context, final int actionIndex) {
    final int centerX = this.width / 2;
    final int top = this.height / 2 + OUTER_RADIUS + CAPTION_GAP;
    if (actionIndex >= 0) {
      final boolean disabled = this.isDisabledAt(actionIndex);
      context.drawCenteredTextWithShadow(
          this.textRenderer,
          Text.translatable(
              disabled ? this.disabledHintKeyAt(actionIndex) : this.labelKeyAt(actionIndex)),
          centerX,
          top,
          disabled ? DISABLED_HINT_COLOR : LABEL_COLOR);
      return;
    }
    context.drawCenteredTextWithShadow(
        this.textRenderer, Text.literal(this.dogName), centerX, top, LABEL_COLOR);
    context.drawCenteredTextWithShadow(
        this.textRenderer,
        Text.translatable(
            this.ring.isSubRingOpen() ? ACTIONS_SECTOR_KEY : this.currentCommand.translationKey()),
        centerX,
        top + this.textRenderer.fontHeight + 2,
        SUBTITLE_COLOR);
  }

  @Override
  public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
    if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
      return super.mouseClicked(mouseX, mouseY, button);
    }
    final int index = this.hoveredSector(mouseX, mouseY);
    if (index >= 0) {
      this.selectAction(index);
    } else {
      this.close();
    }
    return true;
  }

  @Override
  public void mouseMoved(final double mouseX, final double mouseY) {
    final double hubRadius = INNER_RADIUS - INNER_DETECT_MARGIN;
    if (this.ring.returnToMainRingIfInHub(
        mouseX - this.width / 2.0, mouseY - this.height / 2.0, hubRadius)) {
      this.rejectedIndex = -1;
    }
  }

  @Override
  public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.ring.returnToMainRing()) {
      this.rejectedIndex = -1;
      return true;
    }
    final int index = keyCode == GLFW.GLFW_KEY_0 ? ACTIONS.length - 1 : keyCode - GLFW.GLFW_KEY_1;
    if (!this.ring.isSubRingOpen() && index >= 0 && index < ACTIONS.length) {
      this.selectAction(index);
      return true;
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  private void selectAction(final int index) {
    if (this.isDisabledAt(index)) {
      this.rejectedIndex = index;
      return;
    }
    if (this.ring.isActionsSector(index)) {
      this.ring.openSubRing();
      this.rejectedIndex = -1;
      this.playClick();
      return;
    }
    if (this.ring.isSubRingOpen()) {
      ModNetworkingClient.sendSelectWheelSubAction(this.dogId, this.subEntries.get(index).id());
      this.playClick();
      this.close();
      return;
    }
    final DogWheelAction action = ACTIONS[index];
    if (action.command() != null && action.command() == this.currentCommand) {
      this.close();
      return;
    }
    ModNetworkingClient.sendSelectWheelAction(this.dogId, action);
    this.playClick();
    this.close();
  }

  private void playClick() {
    if (this.client != null) {
      this.client
          .getSoundManager()
          .play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }
  }

  private boolean isActiveAt(final int index) {
    return !this.ring.isSubRingOpen()
        && !this.ring.isActionsSector(index)
        && ACTIONS[index].command() == this.currentCommand;
  }

  private boolean isDisabledAt(final int index) {
    if (this.ring.isSubRingOpen()) {
      return !this.subEntries.get(index).available();
    }
    return !this.ring.isActionsSector(index) && this.isDisabled(ACTIONS[index]);
  }

  private Identifier iconAt(final int index) {
    if (this.ring.isSubRingOpen()) {
      return this.subEntries.get(index).icon();
    }
    return this.ring.isActionsSector(index) ? DogWheelIcons.ACTIONS : ACTIONS[index].icon();
  }

  private String labelKeyAt(final int index) {
    if (this.ring.isSubRingOpen()) {
      return this.subEntries.get(index).translationKey();
    }
    return this.ring.isActionsSector(index) ? ACTIONS_SECTOR_KEY : ACTIONS[index].translationKey();
  }

  private String disabledHintKeyAt(final int index) {
    if (this.ring.isSubRingOpen()) {
      return this.subEntries.get(index).refusalKey();
    }
    return this.disabledHintKey(ACTIONS[index]);
  }

  private boolean isDisabled(final DogWheelAction action) {
    return switch (action) {
      case GO_TO_BED -> !this.hasBed;
      case RIDE -> !this.rideRefusalKey.isEmpty();
      default -> false;
    };
  }

  private String disabledHintKey(final DogWheelAction action) {
    return action == DogWheelAction.RIDE
        ? this.rideRefusalKey
        : "screen.dogs-unleashed.command_wheel.no_bed";
  }

  private int hoveredSector(final double mouseX, final double mouseY) {
    return sectorIndexAt(
        mouseX - this.width / 2.0,
        mouseY - this.height / 2.0,
        this.ring.sectorCount(),
        INNER_RADIUS - INNER_DETECT_MARGIN,
        OUTER_RADIUS + OUTER_DETECT_MARGIN);
  }

  static int sectorIndexAt(
      final double dx,
      final double dy,
      final int sectorCount,
      final double innerRadius,
      final double outerRadius) {
    final double distanceSquared = dx * dx + dy * dy;
    if (distanceSquared < innerRadius * innerRadius
        || distanceSquared > outerRadius * outerRadius) {
      return -1;
    }
    double angle = Math.atan2(dy, dx);
    if (angle < 0) {
      angle += 2 * Math.PI;
    }
    final double sectorSpan = 2 * Math.PI / sectorCount;
    final double shifted = (angle + Math.PI / 2 + sectorSpan / 2) % (2 * Math.PI);
    return (int) (shifted / sectorSpan) % sectorCount;
  }
}
