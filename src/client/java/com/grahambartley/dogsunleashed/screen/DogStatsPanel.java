package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.Arrays;
import java.util.function.ToDoubleFunction;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

final class DogStatsPanel {

  static final int ROW_HEIGHT = 14;
  static final int ROW_COUNT = 3;
  static final int BAR_WIDTH = 70;
  static final int BAR_HEIGHT = 7;
  private static final int VALUE_TEXT_GAP = 6;
  private static final int BAR_BACKGROUND = 0xFF2A2A2A;
  private static final int BAR_BORDER = 0xFF555555;
  private static final int HEALTH_FILL = 0xFFE05B5B;
  private static final int SPEED_FILL = 0xFF5BA8E0;
  private static final int ATTACK_FILL = 0xFFE0A85B;
  private static final int LABEL_COLOR = 0xFFBBBBBB;
  private static final int VALUE_COLOR = 0xFFFFFFFF;

  private DogStatsPanel() {}

  static int totalHeight() {
    return ROW_COUNT * ROW_HEIGHT;
  }

  static double maxHealthAcrossBreeds() {
    return maxAcrossBreeds(breed -> breed.attributes().maxHealth());
  }

  static double maxSpeedAcrossBreeds() {
    return maxAcrossBreeds(breed -> breed.attributes().movementSpeed());
  }

  static double maxAttackAcrossBreeds() {
    return maxAcrossBreeds(breed -> breed.attributes().attackDamage());
  }

  static float fraction(final double value, final double max) {
    if (max <= 0.0) {
      return 0.0f;
    }
    return (float) Math.clamp(value / max, 0.0, 1.0);
  }

  static int fillWidth(final float fraction, final int barWidth) {
    return Math.round(fraction * barWidth);
  }

  static int draw(
      final DrawContext context,
      final TextRenderer textRenderer,
      final UnleashedDogBreed breed,
      final int x,
      final int y,
      final int labelWidth) {
    int rowY = y;
    rowY =
        drawRow(
            context,
            textRenderer,
            "screen.dogs-unleashed.pet_details.stat_health",
            String.format("%.0f", breed.attributes().maxHealth()),
            fraction(breed.attributes().maxHealth(), maxHealthAcrossBreeds()),
            HEALTH_FILL,
            x,
            rowY,
            labelWidth);
    rowY =
        drawRow(
            context,
            textRenderer,
            "screen.dogs-unleashed.pet_details.stat_speed",
            String.format("%.2f", breed.attributes().movementSpeed()),
            fraction(breed.attributes().movementSpeed(), maxSpeedAcrossBreeds()),
            SPEED_FILL,
            x,
            rowY,
            labelWidth);
    rowY =
        drawRow(
            context,
            textRenderer,
            "screen.dogs-unleashed.pet_details.stat_attack",
            String.format("%.1f", breed.attributes().attackDamage()),
            fraction(breed.attributes().attackDamage(), maxAttackAcrossBreeds()),
            ATTACK_FILL,
            x,
            rowY,
            labelWidth);
    return rowY;
  }

  private static int drawRow(
      final DrawContext context,
      final TextRenderer textRenderer,
      final String labelKey,
      final String value,
      final float fraction,
      final int fillColor,
      final int x,
      final int y,
      final int labelWidth) {
    context.drawText(textRenderer, Text.translatable(labelKey), x, y, LABEL_COLOR, true);

    final int barX = x + labelWidth;
    final int barY = y + (textRenderer.fontHeight - BAR_HEIGHT) / 2;
    context.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, BAR_BACKGROUND);
    final int fill = fillWidth(fraction, BAR_WIDTH);
    if (fill > 0) {
      context.fill(barX, barY, barX + fill, barY + BAR_HEIGHT, fillColor);
    }
    context.drawBorder(barX, barY, BAR_WIDTH, BAR_HEIGHT, BAR_BORDER);

    context.drawText(textRenderer, value, barX + BAR_WIDTH + VALUE_TEXT_GAP, y, VALUE_COLOR, true);
    return y + ROW_HEIGHT;
  }

  private static double maxAcrossBreeds(final ToDoubleFunction<UnleashedDogBreed> stat) {
    return Arrays.stream(UnleashedDogBreed.values()).mapToDouble(stat).max().orElse(0.0);
  }
}
