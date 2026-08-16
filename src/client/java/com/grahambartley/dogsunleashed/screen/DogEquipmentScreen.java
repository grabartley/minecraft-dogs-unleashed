package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.screenhandler.DogEquipmentScreenHandler;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

public class DogEquipmentScreen extends HandledScreen<DogEquipmentScreenHandler> {

  private static final int PANEL_BACKGROUND = 0xE8141414;
  private static final int PANEL_BORDER = 0xFF555555;
  private static final int SLOT_WELL_BACKGROUND = 0xFF2A2A2A;
  private static final int SLOT_WELL_BORDER = 0xFF6A6A6A;
  private static final int SLOT_GLYPH_COLOR = 0xFF6A6A6A;
  private static final int LABEL_COLOR = 0xFFBBBBBB;
  private static final int TITLE_COLOR = 0xFFFFFFFF;

  private static final int SLOT_WELL_SIZE = 18;
  private static final int SLOT_WELL_OFFSET = 1;
  private static final int SLOT_LABEL_X = 32;
  private static final int SLOT_TEXT_Y_OFFSET = 5;
  private static final int SLOT_CENTER_OFFSET = 8;

  public DogEquipmentScreen(
      final DogEquipmentScreenHandler handler, final PlayerInventory inventory, final Text title) {
    super(handler, inventory, title);
  }

  @Override
  protected void drawBackground(
      final DrawContext context, final float delta, final int mouseX, final int mouseY) {
    context.fill(
        this.x,
        this.y,
        this.x + this.backgroundWidth,
        this.y + this.backgroundHeight,
        PANEL_BACKGROUND);
    context.drawBorder(this.x, this.y, this.backgroundWidth, this.backgroundHeight, PANEL_BORDER);

    for (final Slot slot : this.handler.slots) {
      this.drawSlotWell(context, slot);
    }
    for (int index = 0; index < DogEquipmentScreenHandler.DOG_SLOT_COUNT; index++) {
      this.drawDogSlotDecoration(context, index);
    }
  }

  @Override
  protected void drawForeground(final DrawContext context, final int mouseX, final int mouseY) {
    context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, TITLE_COLOR, false);
    context.drawText(
        this.textRenderer,
        this.playerInventoryTitle,
        this.playerInventoryTitleX,
        this.playerInventoryTitleY,
        LABEL_COLOR,
        false);
  }

  @Override
  public void render(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    super.render(context, mouseX, mouseY, delta);
    this.drawMouseoverTooltip(context, mouseX, mouseY);
    this.drawEmptyDogSlotTooltip(context, mouseX, mouseY);
  }

  private void drawSlotWell(final DrawContext context, final Slot slot) {
    final int wellX = this.x + slot.x - SLOT_WELL_OFFSET;
    final int wellY = this.y + slot.y - SLOT_WELL_OFFSET;
    context.fill(
        wellX, wellY, wellX + SLOT_WELL_SIZE, wellY + SLOT_WELL_SIZE, SLOT_WELL_BACKGROUND);
    context.drawBorder(wellX, wellY, SLOT_WELL_SIZE, SLOT_WELL_SIZE, SLOT_WELL_BORDER);
  }

  private void drawDogSlotDecoration(final DrawContext context, final int index) {
    final Slot slot = this.handler.getSlot(index);
    final DogEquipmentSlot dogSlot = this.handler.dogSlotAt(index);
    context.drawText(
        this.textRenderer,
        Text.translatable(dogSlot.translationKey()),
        this.x + SLOT_LABEL_X,
        this.y + slot.y + SLOT_TEXT_Y_OFFSET,
        LABEL_COLOR,
        false);
    if (slot.hasStack()) {
      return;
    }
    context.drawCenteredTextWithShadow(
        this.textRenderer,
        Text.translatable(dogSlot.glyphTranslationKey()),
        this.x + slot.x + SLOT_CENTER_OFFSET,
        this.y + slot.y + SLOT_TEXT_Y_OFFSET,
        SLOT_GLYPH_COLOR);
  }

  private void drawEmptyDogSlotTooltip(
      final DrawContext context, final int mouseX, final int mouseY) {
    if (!this.handler.getCursorStack().isEmpty()
        || this.focusedSlot == null
        || this.focusedSlot.hasStack()
        || this.focusedSlot.id >= DogEquipmentScreenHandler.DOG_SLOT_COUNT) {
      return;
    }
    final DogEquipmentSlot dogSlot = this.handler.dogSlotAt(this.focusedSlot.id);
    context.drawTooltip(
        this.textRenderer,
        List.of(
            Text.translatable(dogSlot.translationKey()),
            Text.translatable(dogSlot.emptyHintTranslationKey())),
        mouseX,
        mouseY);
  }
}
