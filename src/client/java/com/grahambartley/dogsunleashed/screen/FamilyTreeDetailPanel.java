package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.entity.genome.DogGeneLocus;
import com.grahambartley.dogsunleashed.network.payload.ConnectionDogSyncData;
import com.grahambartley.dogsunleashed.network.payload.GeneSyncData;
import com.grahambartley.dogsunleashed.network.payload.PetSyncData;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

final class FamilyTreeDetailPanel {

  static final int CONTENT_WIDTH = FamilyTreeViewport.PANEL_WIDTH - 12;
  private static final int PANEL_COLOR = 0xE0202020;
  private static final int DIVIDER_COLOR = 0xFF666666;
  private static final float LOW_HEALTH_COLOR_THRESHOLD = 0.5f;
  static final int CARRIER_COLOR = 0xE0A040;
  static final String GENES_KEY = "screen.dogs-unleashed.family_tree.genes";
  static final String GENE_KEY = "screen.dogs-unleashed.family_tree.gene";
  static final String CARRIES_KEY = "screen.dogs-unleashed.family_tree.carries";
  static final String HIDDEN_KEY = "screen.dogs-unleashed.family_tree.carried_hidden";

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
      renderGenes(context, textRenderer, dog, textX, textY + 18);
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
    renderGenes(context, textRenderer, dog, textX, textY + 18);
  }

  private static void renderGenes(
      final DrawContext context,
      final TextRenderer textRenderer,
      final ConnectionDogSyncData dog,
      final int textX,
      final int startY) {
    int textY = startY;
    for (final GeneLine line :
        geneLines(dog, (key, args) -> Text.translatable(key, args).getString())) {
      for (final OrderedText wrapped :
          textRenderer.wrapLines(StringVisitable.plain(line.text()), CONTENT_WIDTH)) {
        context.drawText(textRenderer, wrapped, textX, textY, line.color(), false);
        textY += 10;
      }
      textY += 2;
    }
  }

  static List<GeneLine> geneLines(final ConnectionDogSyncData dog, final Translator translator) {
    if (dog.genes().isEmpty()) {
      return List.of();
    }
    final List<GeneLine> lines = new ArrayList<>();
    lines.add(new GeneLine(translator.translate(GENES_KEY), 0xFFFFFF));
    for (final GeneSyncData gene : dog.genes()) {
      final DogGeneLocus locus = gene.locus();
      lines.add(
          new GeneLine(
              translator.translate(
                  GENE_KEY,
                  translator.translate(locus.trait().translationKey()),
                  translator.translate(locus.alleleTranslationKey(gene.shown()))),
              0xAAAAAA));
      if (gene.carrier()) {
        final String carried =
            gene.carried().stream()
                .map(allele -> translator.translate(locus.alleleTranslationKey(allele)))
                .collect(Collectors.joining(", "));
        lines.add(new GeneLine(translator.translate(CARRIES_KEY, carried), CARRIER_COLOR));
      }
    }
    if (!dog.carriedGenesRevealed()) {
      lines.add(new GeneLine(translator.translate(HIDDEN_KEY), 0x777777));
    }
    return lines;
  }

  static boolean isCarrier(final ConnectionDogSyncData dog) {
    return dog.genes().stream().anyMatch(GeneSyncData::carrier);
  }

  record GeneLine(String text, int color) {}

  interface Translator {
    String translate(String key, Object... args);
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
