package com.grahambartley.dogsunleashed.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

final class ConfigSectionHeader extends ClickableWidget {
  private static final int COLOR = 0xFFD27F;

  private final TextRenderer textRenderer;

  ConfigSectionHeader(
      final int width, final int height, final Text message, final TextRenderer textRenderer) {
    super(0, 0, width, height, message);
    this.textRenderer = textRenderer;
    this.active = false;
  }

  @Override
  protected void renderWidget(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    context.drawCenteredTextWithShadow(
        textRenderer, this.getMessage(), this.getX() + this.getWidth() / 2, this.getY() + 2, COLOR);
  }

  @Override
  protected void appendClickableNarrations(final NarrationMessageBuilder builder) {}
}
