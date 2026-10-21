package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.network.ModNetworkingClient;
import com.grahambartley.dogsunleashed.network.payload.OpenDeliveryPickerPayload;
import com.grahambartley.dogsunleashed.network.payload.OpenDeliveryPickerPayload.Recipient;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class DeliveryPickerScreen extends Screen {

  private static final int PAGE_BUTTON_WIDTH = 20;
  private static final int CANCEL_WIDTH = 112;
  private static final int GAP = 4;

  private final OpenDeliveryPickerPayload payload;
  private int page;

  public DeliveryPickerScreen(final OpenDeliveryPickerPayload payload) {
    super(Text.translatable("screen.dogs-unleashed.deliver.title", payload.dogName()));
    this.payload = payload;
  }

  @Override
  protected void init() {
    final List<Recipient> recipients = this.payload.recipients();
    this.page = DeliveryPickerLayout.clampPage(this.page, recipients.size());
    final int left = (this.width - DeliveryPickerLayout.BUTTON_WIDTH) / 2;
    final int first = DeliveryPickerLayout.firstOnPage(this.page);
    for (int row = 0; row < DeliveryPickerLayout.shownOnPage(this.page, recipients.size()); row++) {
      final Recipient recipient = recipients.get(first + row);
      this.addDrawableChild(
          ButtonWidget.builder(Text.literal(recipient.name()), button -> this.pick(recipient))
              .dimensions(
                  left,
                  DeliveryPickerLayout.rowY(this.height, row),
                  DeliveryPickerLayout.BUTTON_WIDTH,
                  DeliveryPickerLayout.BUTTON_HEIGHT)
              .build());
    }
    final int footerY = DeliveryPickerLayout.footerY(this.height);
    final int pages = DeliveryPickerLayout.pageCount(recipients.size());
    final ButtonWidget previous =
        ButtonWidget.builder(Text.literal("<"), button -> this.turn(-1))
            .dimensions(left, footerY, PAGE_BUTTON_WIDTH, DeliveryPickerLayout.BUTTON_HEIGHT)
            .build();
    previous.active = this.page > 0;
    final ButtonWidget next =
        ButtonWidget.builder(Text.literal(">"), button -> this.turn(1))
            .dimensions(
                left + DeliveryPickerLayout.BUTTON_WIDTH - PAGE_BUTTON_WIDTH,
                footerY,
                PAGE_BUTTON_WIDTH,
                DeliveryPickerLayout.BUTTON_HEIGHT)
            .build();
    next.active = this.page < pages - 1;
    if (pages > 1) {
      this.addDrawableChild(previous);
      this.addDrawableChild(next);
    }
    this.addDrawableChild(
        ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.close())
            .dimensions(
                left + PAGE_BUTTON_WIDTH + GAP,
                footerY,
                CANCEL_WIDTH,
                DeliveryPickerLayout.BUTTON_HEIGHT)
            .build());
  }

  private void turn(final int by) {
    this.page += by;
    this.clearAndInit();
  }

  private void pick(final Recipient recipient) {
    ModNetworkingClient.sendDeliverDog(this.payload.dogId(), recipient.id());
    this.close();
  }

  @Override
  public void render(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    super.render(context, mouseX, mouseY, delta);
    context.drawCenteredTextWithShadow(
        this.textRenderer,
        this.title,
        this.width / 2,
        DeliveryPickerLayout.listTop(this.height) - DeliveryPickerLayout.TITLE_GAP,
        0xFFFFFF);
  }

  @Override
  public boolean shouldPause() {
    return false;
  }
}
