package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.screenhandler.DogEquipmentScreenHandler;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DogEquipmentScreen extends HandledScreen<DogEquipmentScreenHandler> {

  private static final Identifier BACKGROUND_TEXTURE =
      Identifier.ofVanilla("textures/gui/container/horse.png");
  private static final Identifier ARMOUR_SLOT_SPRITE =
      Identifier.ofVanilla("container/horse/armor_slot");
  private static final Identifier SLOT_SPRITE = Identifier.ofVanilla("container/slot");

  private static final int SLOT_SPRITE_SIZE = 18;
  private static final int SLOT_SPRITE_OFFSET = 1;
  private static final int PREVIEW_LEFT = 26;
  private static final int PREVIEW_TOP = 18;
  private static final int PREVIEW_SIZE = 52;

  public DogEquipmentScreen(
      final DogEquipmentScreenHandler handler, final PlayerInventory inventory, final Text title) {
    super(handler, inventory, title);
  }

  @Override
  protected void drawBackground(
      final DrawContext context, final float delta, final int mouseX, final int mouseY) {
    context.drawTexture(
        BACKGROUND_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    for (int index = 0; index < DogEquipmentScreenHandler.DOG_SLOT_COUNT; index++) {
      this.drawDogSlot(context, index);
    }
    this.drawDogPreview(context, mouseX, mouseY);
  }

  @Override
  public void render(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    super.render(context, mouseX, mouseY, delta);
    this.drawMouseoverTooltip(context, mouseX, mouseY);
    this.drawEmptyDogSlotTooltip(context, mouseX, mouseY);
  }

  private void drawDogSlot(final DrawContext context, final int index) {
    final Slot slot = this.handler.getSlot(index);
    final Identifier sprite =
        this.handler.dogSlotAt(index) == DogEquipmentSlot.ARMOUR ? ARMOUR_SLOT_SPRITE : SLOT_SPRITE;
    context.drawGuiTexture(
        sprite,
        this.x + slot.x - SLOT_SPRITE_OFFSET,
        this.y + slot.y - SLOT_SPRITE_OFFSET,
        SLOT_SPRITE_SIZE,
        SLOT_SPRITE_SIZE);
  }

  private void drawDogPreview(final DrawContext context, final int mouseX, final int mouseY) {
    final UnleashedDogEntity dog = this.handler.dog();
    if (dog == null) {
      return;
    }
    final UnleashedDogBreed.RenderTransforms transforms =
        dog.getRigSourceBreed().renderTransforms();
    final float relativeSize =
        DogPreviewFraming.sizeRelativeToAdult(
            dog.getGrowth().applies(),
            dog.getGrowth().stage().renderScaleMultiplier(),
            dog.isBaby(),
            transforms.adultScale(),
            transforms.babyScale());
    final float framedHeight = transforms.portraitHeight() * relativeSize;
    final int left = this.x + PREVIEW_LEFT;
    final int top = this.y + PREVIEW_TOP;
    InventoryScreen.drawEntity(
        context,
        left,
        top,
        left + PREVIEW_SIZE,
        top + PREVIEW_SIZE,
        DogPreviewFraming.renderSize(PREVIEW_SIZE, framedHeight),
        DogPreviewFraming.pivotOffset(framedHeight, dog.getHeight(), relativeSize),
        mouseX,
        mouseY,
        dog);
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
