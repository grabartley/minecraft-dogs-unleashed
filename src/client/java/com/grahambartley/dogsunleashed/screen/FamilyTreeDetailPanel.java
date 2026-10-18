package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.network.payload.ConnectionDogSyncData;
import com.grahambartley.dogsunleashed.network.payload.PetSyncData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

final class FamilyTreeDetailPanel {

  static final int CONTENT_WIDTH = FamilyTreeViewport.PANEL_WIDTH - 12;
  private static final int PANEL_COLOR = 0xE0202020;
  private static final int DIVIDER_COLOR = 0xFF666666;
  private static final float LOW_HEALTH_COLOR_THRESHOLD = 0.5f;

  private FamilyTreeDetailPanel() {}

  static void render(
      final DrawContext context,
      final TextRenderer textRenderer,
      final @Nullable ConnectionDogSyncData dog,
      final int screenWidth,
      final int screenHeight) {
    final int x = FamilyTreeViewport.panelX(screenWidth);
    renderBackground(context, x, screenWidth, screenHeight);
    if (dog == null) {
      return;
    }
    final PetSyncData pet = dog.pet();
    final int textX = x + 6;
    int textY = 10;
    context.drawText(textRenderer, fit(textRenderer, pet.name()), textX, textY, 0xFFFFFF, true);
    textY += 14;
    context.drawText(
        textRenderer,
        fit(textRenderer, DogBreedNames.displayName(pet.breed(), pet.composition()).getString()),
        textX,
        textY,
        0xAAAAAA,
        false);
    textY += 12;
    context.drawText(
        textRenderer,
        fit(
            textRenderer,
            Text.translatable("screen.dogs-unleashed.family_tree.owner", ownerName(dog))
                .getString()),
        textX,
        textY,
        0xAAAAAA,
        false);
    textY += 12;
    if (!pet.alive()) {
      context.drawText(
          textRenderer,
          Text.translatable("screen.dogs-unleashed.pet_manager.deceased"),
          textX,
          textY,
          0xFF5555,
          false);
      return;
    }
    context.drawText(
        textRenderer,
        String.format("%.1f / %.1f ❤", pet.health(), pet.maxHealth()),
        textX,
        textY,
        healthColor(pet.health(), pet.maxHealth()),
        false);
    textY += 12;
    context.drawText(
        textRenderer,
        Text.translatable(
            pet.baby()
                ? "screen.dogs-unleashed.pet_details.age_baby"
                : "screen.dogs-unleashed.pet_details.age_adult"),
        textX,
        textY,
        0xAAAAAA,
        false);
  }

  private static void renderBackground(
      final DrawContext context, final int x, final int screenWidth, final int screenHeight) {
    context.fill(x, 0, screenWidth, screenHeight, PANEL_COLOR);
    context.fill(x, 0, x + 1, screenHeight, DIVIDER_COLOR);
  }

  static int healthColor(final float health, final float maxHealth) {
    return health > maxHealth * LOW_HEALTH_COLOR_THRESHOLD ? 0x55FF55 : 0xFF5555;
  }

  private static String ownerName(final ConnectionDogSyncData dog) {
    return dog.ownerName().isEmpty()
        ? Text.translatable("screen.dogs-unleashed.family_tree.owner_unknown").getString()
        : dog.ownerName();
  }

  private static String fit(final TextRenderer textRenderer, final String text) {
    return TextEllipsis.fit(text, CONTENT_WIDTH, textRenderer::getWidth);
  }
}
