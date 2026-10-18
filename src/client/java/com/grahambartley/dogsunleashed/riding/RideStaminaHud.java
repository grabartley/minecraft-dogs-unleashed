package com.grahambartley.dogsunleashed.riding;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

public final class RideStaminaHud {

  static final int BAR_WIDTH = 182;
  static final int BAR_HEIGHT = 5;
  static final int BOTTOM_OFFSET = 29;
  private static final Identifier BACKGROUND = Identifier.ofVanilla("hud/jump_bar_background");
  private static final Identifier PROGRESS = Identifier.ofVanilla("hud/jump_bar_progress");
  private static final Identifier LOCKED_OUT = Identifier.ofVanilla("hud/jump_bar_cooldown");

  private RideStaminaHud() {}

  public static void register() {
    HudRenderCallback.EVENT.register(RideStaminaHud::render);
  }

  public static int fillWidth(final float fraction) {
    return Math.round(Math.max(0.0f, Math.min(1.0f, fraction)) * BAR_WIDTH);
  }

  private static void render(final DrawContext context, final RenderTickCounter tickCounter) {
    final MinecraftClient client = MinecraftClient.getInstance();
    final UnleashedDogEntity dog = RiddenDog.of(client.player);
    if (dog == null || !dog.getRiding().isRideableStage() || client.options.hudHidden) {
      return;
    }
    final int x = context.getScaledWindowWidth() / 2 - BAR_WIDTH / 2;
    final int y = context.getScaledWindowHeight() - BOTTOM_OFFSET;
    final int fill = fillWidth(dog.getRiding().staminaFraction());
    RenderSystem.enableBlend();
    context.drawGuiTexture(BACKGROUND, x, y, BAR_WIDTH, BAR_HEIGHT);
    if (fill > 0) {
      context.drawGuiTexture(
          dog.getRiding().isLockedOut() ? LOCKED_OUT : PROGRESS,
          BAR_WIDTH,
          BAR_HEIGHT,
          0,
          0,
          x,
          y,
          fill,
          BAR_HEIGHT);
    }
    RenderSystem.disableBlend();
  }
}
