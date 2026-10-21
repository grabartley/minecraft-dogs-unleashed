package com.grahambartley.dogsunleashed.tug;

import com.grahambartley.dogsunleashed.entity.DogTug;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.goal.TugRules;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class TugMeterHud {

  static final int BAR_WIDTH = 182;
  static final int BAR_HEIGHT = 5;
  static final int BOTTOM_OFFSET = 29;
  private static final double SEARCH_RANGE = TugRules.MAX_DISTANCE + 2.0;
  private static final Identifier DOG_SIDE = Identifier.ofVanilla("boss_bar/red_progress");
  private static final Identifier PLAYER_SIDE = Identifier.ofVanilla("boss_bar/green_progress");

  private TugMeterHud() {}

  public static void register() {
    HudRenderCallback.EVENT.register(TugMeterHud::render);
  }

  public static int playerWidth(final float balance) {
    return Math.round(TugRules.meterFill(balance) * BAR_WIDTH);
  }

  public static boolean shows(final int tuggingPlayerId, final int playerId) {
    return tuggingPlayerId != DogTug.NO_PLAYER && tuggingPlayerId == playerId;
  }

  public static boolean isShowing(final @Nullable PlayerEntity player) {
    return dogTuggedBy(player) != null;
  }

  private static @Nullable UnleashedDogEntity dogTuggedBy(final @Nullable PlayerEntity player) {
    if (player == null) {
      return null;
    }
    return player
        .getWorld()
        .getEntitiesByClass(
            UnleashedDogEntity.class,
            player.getBoundingBox().expand(SEARCH_RANGE),
            dog -> shows(dog.getComponents().get(DogTug.class).tuggingPlayerId(), player.getId()))
        .stream()
        .findFirst()
        .orElse(null);
  }

  private static void render(final DrawContext context, final RenderTickCounter tickCounter) {
    final MinecraftClient client = MinecraftClient.getInstance();
    if (client.options.hudHidden) {
      return;
    }
    final UnleashedDogEntity dog = dogTuggedBy(client.player);
    if (dog == null) {
      return;
    }
    final int x = context.getScaledWindowWidth() / 2 - BAR_WIDTH / 2;
    final int y = context.getScaledWindowHeight() - BOTTOM_OFFSET;
    final int yours = playerWidth(dog.getComponents().get(DogTug.class).balance());
    RenderSystem.enableBlend();
    context.drawGuiTexture(DOG_SIDE, x, y, BAR_WIDTH, BAR_HEIGHT);
    if (yours > 0) {
      context.drawGuiTexture(PLAYER_SIDE, BAR_WIDTH, BAR_HEIGHT, 0, 0, x, y, yours, BAR_HEIGHT);
    }
    RenderSystem.disableBlend();
  }
}
