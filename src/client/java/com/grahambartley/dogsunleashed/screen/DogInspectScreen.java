package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.entity.variant.DogCoats;
import com.grahambartley.dogsunleashed.entity.variant.DogRarity;
import com.grahambartley.dogsunleashed.entity.variant.DogRarityClassifier;
import com.grahambartley.dogsunleashed.entity.variant.UnleashedDogCoat;
import com.grahambartley.dogsunleashed.network.ModNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class DogInspectScreen extends Screen {

  static final int CARD_WIDTH = 236;
  static final int CARD_HEIGHT = 186;
  static final int CARD_PADDING = 10;
  static final int PORTRAIT_SIZE = 56;
  private static final int HEADER_LINE_HEIGHT = 12;
  private static final int INFO_LINE_HEIGHT = 14;
  private static final int INFO_LABEL_WIDTH = 60;
  private static final int CARD_BACKGROUND = 0xE8141414;
  private static final int CARD_BORDER = 0xFF555555;
  private static final int LABEL_COLOR = 0xFFBBBBBB;
  private static final int VALUE_COLOR = 0xFFFFFFFF;
  private static final int WILD_COLOR = 0xFFFFCC55;
  private static final int HINT_COLOR = 0xFF888888;
  private static final float LOW_HEALTH_COLOR_THRESHOLD = 0.5f;

  private final ModNetworking.OpenDogInspectPayload data;
  private final DogPortraitRenderer portraits = new DogPortraitRenderer();

  public DogInspectScreen(final ModNetworking.OpenDogInspectPayload data) {
    super(Text.translatable("screen.dogs-unleashed.dog_inspect.title"));
    this.data = data;
  }

  @Override
  public void render(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    super.render(context, mouseX, mouseY, delta);

    final int cardX = cardLeft(this.width);
    final int cardY = cardTop(this.height);
    context.fill(cardX, cardY, cardX + CARD_WIDTH, cardY + CARD_HEIGHT, CARD_BACKGROUND);
    context.drawBorder(cardX, cardY, CARD_WIDTH, CARD_HEIGHT, CARD_BORDER);

    final ModNetworking.PetSyncData pet = data.pet();
    final int portraitX = cardX + CARD_PADDING;
    final int portraitY = cardY + CARD_PADDING;
    portraits.draw(
        context, this.textRenderer, pet, portraitX, portraitY, PORTRAIT_SIZE, mouseX, mouseY);

    final int headerX = portraitX + PORTRAIT_SIZE + 8;
    final int headerMaxWidth = cardX + CARD_WIDTH - CARD_PADDING - headerX;
    int headerY = portraitY + 2;
    context.drawText(
        this.textRenderer,
        this.textRenderer.trimToWidth(displayName(), headerMaxWidth),
        headerX,
        headerY,
        VALUE_COLOR,
        true);
    headerY += HEADER_LINE_HEIGHT;
    context.drawText(
        this.textRenderer,
        this.textRenderer.trimToWidth(statusText().getString(), headerMaxWidth),
        headerX,
        headerY,
        data.tamed() ? LABEL_COLOR : WILD_COLOR,
        true);
    headerY += HEADER_LINE_HEIGHT;
    final int healthColor =
        pet.health() > pet.maxHealth() * LOW_HEALTH_COLOR_THRESHOLD ? 0xFF55FF55 : 0xFFFF5555;
    context.drawText(
        this.textRenderer,
        String.format("%.1f / %.1f ❤", pet.health(), pet.maxHealth()),
        headerX,
        headerY,
        healthColor,
        true);
    headerY += HEADER_LINE_HEIGHT;
    context.drawText(
        this.textRenderer,
        Text.translatable(
            pet.baby()
                ? "screen.dogs-unleashed.pet_details.age_baby"
                : "screen.dogs-unleashed.pet_details.age_adult"),
        headerX,
        headerY,
        LABEL_COLOR,
        true);

    final int infoX = cardX + CARD_PADDING;
    int infoY = portraitY + PORTRAIT_SIZE + 8;
    infoY =
        drawInfoLine(
            context,
            infoX,
            infoY,
            "screen.dogs-unleashed.pet_details.breed",
            DogTraitFormat.formatComposition(
                data.composition(), breed -> Text.translatable(breed.translationKey()).getString()),
            VALUE_COLOR);
    final UnleashedDogCoat coat = DogCoats.coatOf(pet.displayBreed(), pet.coatVariant());
    if (coat != null) {
      infoY =
          drawInfoLine(
              context,
              infoX,
              infoY,
              "screen.dogs-unleashed.pet_details.coat",
              Text.translatable(coat.translationKey()).getString(),
              VALUE_COLOR);
    }
    final int rarityChance =
        DogRarityClassifier.chancePercent(pet.displayBreed(), pet.coatVariant());
    final DogRarity rarity = DogRarityClassifier.classify(rarityChance);
    infoY =
        drawInfoLine(
            context,
            infoX,
            infoY,
            "screen.dogs-unleashed.pet_details.rarity",
            DogTraitFormat.formatRarity(
                Text.translatable(rarity.translationKey()).getString(), rarityChance),
            rarity.colorArgb());

    DogStatsPanel.drawForPet(context, this.textRenderer, pet, infoX, infoY, INFO_LABEL_WIDTH);

    context.drawCenteredTextWithShadow(
        this.textRenderer,
        Text.translatable("screen.dogs-unleashed.dog_inspect.close_hint"),
        cardX + CARD_WIDTH / 2,
        cardY + CARD_HEIGHT - CARD_PADDING - this.textRenderer.fontHeight + 2,
        HINT_COLOR);
  }

  private String displayName() {
    final ModNetworking.PetSyncData pet = data.pet();
    return pet.name().isEmpty()
        ? DogBreedNames.displayName(pet.breed(), pet.composition()).getString()
        : pet.name();
  }

  private Text statusText() {
    if (!data.tamed()) {
      return Text.translatable("screen.dogs-unleashed.dog_inspect.wild");
    }
    final String ownerName =
        data.ownerName().isEmpty()
            ? Text.translatable("screen.dogs-unleashed.family_tree.owner_unknown").getString()
            : data.ownerName();
    return Text.translatable("screen.dogs-unleashed.dog_inspect.owner", ownerName);
  }

  private int drawInfoLine(
      final DrawContext context,
      final int x,
      final int y,
      final String labelKey,
      final String value,
      final int valueColor) {
    context.drawText(this.textRenderer, Text.translatable(labelKey), x, y, LABEL_COLOR, true);
    context.drawText(
        this.textRenderer,
        this.textRenderer.trimToWidth(value, CARD_WIDTH - CARD_PADDING * 2 - INFO_LABEL_WIDTH),
        x + INFO_LABEL_WIDTH,
        y,
        valueColor,
        true);
    return y + INFO_LINE_HEIGHT;
  }

  @Override
  public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
    if (super.mouseClicked(mouseX, mouseY, button)) {
      return true;
    }
    close();
    return true;
  }

  static int cardLeft(final int screenWidth) {
    return (screenWidth - CARD_WIDTH) / 2;
  }

  static int cardTop(final int screenHeight) {
    return (screenHeight - CARD_HEIGHT) / 2 - 10;
  }

  @Override
  public void removed() {
    portraits.clear();
    super.removed();
  }

  @Override
  public void close() {
    MinecraftClient.getInstance().setScreen(null);
  }

  @Override
  public boolean shouldPause() {
    return false;
  }
}
