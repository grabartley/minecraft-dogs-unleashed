package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogWheelAction;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.network.ModNetworking;
import com.grahambartley.dogsunleashed.network.ModNetworkingClient;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.UUID;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

/**
 * Radial command menu opened by right-clicking an owned dog. Sectors are drawn as annulus segments
 * with the position-color shader (no texture assets), tessellated at native window resolution so
 * the arcs stay smooth at any GUI scale. The world keeps running behind the wheel.
 */
public class DogCommandWheelScreen extends Screen {

  private static final DogWheelAction[] ACTIONS = DogWheelAction.values();

  private static final float OUTER_RADIUS_FRACTION = 0.32f;
  private static final int MIN_OUTER_RADIUS = 64;
  // High enough that large scaled resolutions (GUI scale 1 on big windows) still get a wheel
  // proportionate to the screen instead of a crosshair-sized ring.
  private static final int MAX_OUTER_RADIUS = 140;
  private static final float INNER_RADIUS_FRACTION = 0.45f;
  private static final int INNER_DETECT_MARGIN = 4;
  private static final int OUTER_DETECT_MARGIN = 14;
  private static final float SECTOR_GAP_DEGREES = 1.5f;
  private static final float TESSELLATION_STEP_DEGREES = 2.5f;
  private static final int ICON_HALF_SIZE = 8;

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
  private final ItemStack[] icons;
  private DogCommand currentCommand;

  public DogCommandWheelScreen(final ModNetworking.OpenCommandWheelPayload payload) {
    super(Text.translatable("screen.dogs-unleashed.command_wheel.title"));
    this.dogEntityId = payload.entityId();
    this.dogId = payload.dogId();
    this.hasBed = payload.hasBed();
    this.dogName = payload.dogName();
    this.currentCommand = DogCommand.fromId(payload.currentCommandId());
    this.icons =
        new ItemStack[] {
          new ItemStack(Items.LEAD),
          new ItemStack(Items.NAME_TAG),
          new ItemStack(Items.ARMOR_STAND),
          new ItemStack(Items.BONE),
          new ItemStack(Items.IRON_SWORD),
          new ItemStack(Items.SHIELD),
          new ItemStack(Items.GRASS_BLOCK),
          new ItemStack(Items.RED_BED)
        };
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

    final int outerRadius = this.outerRadius();
    final int innerRadius = this.innerRadius(outerRadius);
    final int hoveredIndex = this.hoveredSector(mouseX, mouseY);

    this.renderRing(context, innerRadius, outerRadius, hoveredIndex);
    this.renderIcons(context, innerRadius, outerRadius);
    this.renderHubText(context, hoveredIndex);
  }

  private void renderRing(
      final DrawContext context,
      final int innerRadius,
      final int outerRadius,
      final int hoveredIndex) {
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

    final float sectorSpan = 360f / ACTIONS.length;
    for (int i = 0; i < ACTIONS.length; i++) {
      final float startDeg = -90f + (i - 0.5f) * sectorSpan + SECTOR_GAP_DEGREES / 2f;
      final float endDeg = startDeg + sectorSpan - SECTOR_GAP_DEGREES;
      appendSector(
          buffer,
          matrix,
          centerX,
          centerY,
          innerRadius * windowScale,
          outerRadius * windowScale,
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
    final DogWheelAction action = ACTIONS[index];
    if (this.isDisabled(action)) {
      return SECTOR_DISABLED_COLOR;
    }
    final boolean active = action.command() == this.currentCommand;
    final boolean hovered = index == hoveredIndex;
    if (active && hovered) {
      return SECTOR_ACTIVE_HOVERED_COLOR;
    }
    if (active) {
      return SECTOR_ACTIVE_COLOR;
    }
    return hovered ? SECTOR_HOVERED_COLOR : SECTOR_COLOR;
  }

  private void renderIcons(
      final DrawContext context, final int innerRadius, final int outerRadius) {
    final float iconRadius = (innerRadius + outerRadius) / 2f;
    final float sectorSpan = (float) (2 * Math.PI / ACTIONS.length);
    for (int i = 0; i < ACTIONS.length; i++) {
      final float angle = (float) (-Math.PI / 2 + i * sectorSpan);
      final int iconX =
          Math.round(this.width / 2f + iconRadius * MathHelper.cos(angle)) - ICON_HALF_SIZE;
      final int iconY =
          Math.round(this.height / 2f + iconRadius * MathHelper.sin(angle)) - ICON_HALF_SIZE;
      context.drawItem(this.icons[i], iconX, iconY);
    }
  }

  private void renderHubText(final DrawContext context, final int hoveredIndex) {
    final int centerX = this.width / 2;
    final int centerY = this.height / 2;
    if (hoveredIndex >= 0) {
      final DogWheelAction action = ACTIONS[hoveredIndex];
      if (this.isDisabled(action)) {
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.translatable("screen.dogs-unleashed.command_wheel.no_bed"),
            centerX,
            centerY - this.textRenderer.fontHeight / 2,
            DISABLED_HINT_COLOR);
        return;
      }
      context.drawCenteredTextWithShadow(
          this.textRenderer,
          Text.translatable(action.translationKey()),
          centerX,
          centerY - this.textRenderer.fontHeight / 2,
          LABEL_COLOR);
      return;
    }
    context.drawCenteredTextWithShadow(
        this.textRenderer,
        Text.literal(this.dogName),
        centerX,
        centerY - this.textRenderer.fontHeight - 1,
        LABEL_COLOR);
    context.drawCenteredTextWithShadow(
        this.textRenderer,
        Text.translatable(this.currentCommand.translationKey()),
        centerX,
        centerY + 1,
        SUBTITLE_COLOR);
  }

  @Override
  public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
    if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
      return super.mouseClicked(mouseX, mouseY, button);
    }
    final int index = this.hoveredSector(mouseX, mouseY);
    if (index >= 0) {
      this.selectAction(ACTIONS[index]);
    } else {
      this.close();
    }
    return true;
  }

  @Override
  public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
    final int index = keyCode - GLFW.GLFW_KEY_1;
    if (index >= 0 && index < ACTIONS.length) {
      this.selectAction(ACTIONS[index]);
      return true;
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  private void selectAction(final DogWheelAction action) {
    if (this.isDisabled(action)) {
      return;
    }
    if (action.command() != null && action.command() == this.currentCommand) {
      this.close();
      return;
    }
    ModNetworkingClient.sendSelectWheelAction(this.dogId, action);
    if (this.client != null) {
      this.client
          .getSoundManager()
          .play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }
    this.close();
  }

  private boolean isDisabled(final DogWheelAction action) {
    return action == DogWheelAction.GO_TO_BED && !this.hasBed;
  }

  private int hoveredSector(final double mouseX, final double mouseY) {
    final int outerRadius = this.outerRadius();
    final int innerRadius = this.innerRadius(outerRadius);
    return sectorIndexAt(
        mouseX - this.width / 2.0,
        mouseY - this.height / 2.0,
        ACTIONS.length,
        innerRadius - INNER_DETECT_MARGIN,
        outerRadius + OUTER_DETECT_MARGIN);
  }

  private int outerRadius() {
    return MathHelper.clamp(
        (int) (Math.min(this.width, this.height) * OUTER_RADIUS_FRACTION),
        MIN_OUTER_RADIUS,
        MAX_OUTER_RADIUS);
  }

  private int innerRadius(final int outerRadius) {
    return (int) (outerRadius * INNER_RADIUS_FRACTION);
  }

  /**
   * Maps a mouse offset from the wheel center to a sector index, or -1 in the hub deadzone or
   * beyond the outer detection radius. Sector 0 is centered at 12 o'clock and indices proceed
   * clockwise (screen y grows downward).
   */
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
