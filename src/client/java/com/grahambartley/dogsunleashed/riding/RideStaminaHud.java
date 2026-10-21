package com.grahambartley.dogsunleashed.riding;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.sled.SledEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

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

  public static float shownFraction(final float jumpCharge, final float stamina) {
    return jumpCharge > 0.0f ? jumpCharge : stamina;
  }

  public static int fillWidth(final float fraction) {
    return Math.round(Math.max(0.0f, Math.min(1.0f, fraction)) * BAR_WIDTH);
  }

  public static boolean isShowing(final @Nullable PlayerEntity player) {
    final UnleashedDogEntity dog = RiddenDog.of(player);
    return (dog != null && dog.getRiding().isRideableStage()) || sledOf(player) != null;
  }

  private static @Nullable SledEntity sledOf(final @Nullable PlayerEntity player) {
    return player != null
            && player.getVehicle() instanceof SledEntity sled
            && sled.getTeam().size() > 0
        ? sled
        : null;
  }

  private static void render(final DrawContext context, final RenderTickCounter tickCounter) {
    final MinecraftClient client = MinecraftClient.getInstance();
    if (client.options.hudHidden || client.player == null) {
      return;
    }
    final UnleashedDogEntity dog = RiddenDog.of(client.player);
    if (dog != null && dog.getRiding().isRideableStage()) {
      final float charge = dog.getRiding().jumpCharge();
      draw(
          context,
          shownFraction(charge, dog.getRiding().staminaFraction()),
          charge <= 0.0f && dog.getRiding().isLockedOut());
    } else {
      final SledEntity sled = sledOf(client.player);
      if (sled != null) {
        draw(context, sled.staminaFraction(), sled.isStaminaLockedOut());
      }
    }
  }

  private static void draw(
      final DrawContext context, final float fraction, final boolean lockedOut) {
    final int x = context.getScaledWindowWidth() / 2 - BAR_WIDTH / 2;
    final int y = context.getScaledWindowHeight() - BOTTOM_OFFSET;
    final int fill = fillWidth(fraction);
    RenderSystem.enableBlend();
    context.drawGuiTexture(BACKGROUND, x, y, BAR_WIDTH, BAR_HEIGHT);
    if (fill > 0) {
      context.drawGuiTexture(
          lockedOut ? LOCKED_OUT : PROGRESS, BAR_WIDTH, BAR_HEIGHT, 0, 0, x, y, fill, BAR_HEIGHT);
    }
    RenderSystem.disableBlend();
  }
}
