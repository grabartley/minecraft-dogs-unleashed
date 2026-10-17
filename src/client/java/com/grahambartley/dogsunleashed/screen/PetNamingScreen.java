package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.network.ModNetworkingClient;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class PetNamingScreen extends Screen {

  private static final int FIELD_WIDTH = 200;
  private static final int FIELD_HEIGHT = 20;
  private static final int BUTTON_WIDTH = 120;
  private static final int BUTTON_HEIGHT = 20;
  static final int NAME_MAX_LENGTH = 32;

  private final UUID petId;
  private final Text breedName;
  private final boolean bornPuppy;
  private final String suggestedName;
  private TextFieldWidget nameField;

  public PetNamingScreen(UUID petId, Text breedName, boolean bornPuppy, String suggestedName) {
    super(Text.translatable("screen.dogs-unleashed.pet_naming.title"));
    this.petId = petId;
    this.breedName = breedName;
    this.bornPuppy = bornPuppy;
    this.suggestedName = suggestedName;
  }

  @Override
  protected void init() {
    final int centerX = this.width / 2;
    final int centerY = this.height / 2;

    final TextFieldWidget previousField = nameField;
    nameField =
        new TextFieldWidget(
            this.textRenderer,
            centerX - FIELD_WIDTH / 2,
            centerY - 30,
            FIELD_WIDTH,
            FIELD_HEIGHT,
            previousField,
            Text.translatable("screen.dogs-unleashed.pet_naming.name_field"));
    nameField.setMaxLength(NAME_MAX_LENGTH);
    if (previousField == null) {
      nameField.setText(suggestedName);
    }
    addDrawableChild(nameField);

    addDrawableChild(
        ButtonWidget.builder(
                Text.translatable("screen.dogs-unleashed.pet_naming.confirm"), this::onConfirm)
            .dimensions(centerX - BUTTON_WIDTH / 2, centerY + 10, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build());
  }

  private void onConfirm(ButtonWidget button) {
    resolveSubmittableName(nameField.getText())
        .ifPresent(
            name -> {
              ModNetworkingClient.sendSetPetName(petId, name);
              close();
            });
  }

  static Optional<String> resolveSubmittableName(String rawText) {
    if (rawText == null) {
      return Optional.empty();
    }
    final String trimmed = rawText.trim();
    return trimmed.isEmpty() ? Optional.empty() : Optional.of(trimmed);
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    super.render(context, mouseX, mouseY, delta);

    final int centerX = this.width / 2;
    final int centerY = this.height / 2;

    context.drawCenteredTextWithShadow(
        this.textRenderer,
        Text.translatable(headerKey(this.bornPuppy), this.breedName),
        centerX,
        centerY - 70,
        0xFFFFFF);

    context.drawCenteredTextWithShadow(
        this.textRenderer,
        Text.translatable("screen.dogs-unleashed.pet_naming.prompt"),
        centerX,
        centerY - 50,
        0xAAAAAA);
  }

  static String headerKey(final boolean bornPuppy) {
    return bornPuppy
        ? "screen.dogs-unleashed.pet_naming.header_newborn"
        : "screen.dogs-unleashed.pet_naming.header";
  }

  @Override
  public boolean shouldPause() {
    return false;
  }

  @Override
  public boolean shouldCloseOnEsc() {
    return false;
  }
}
