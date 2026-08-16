package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.entity.variant.DogCoats;
import com.grahambartley.dogsunleashed.entity.variant.DogRarity;
import com.grahambartley.dogsunleashed.entity.variant.DogRarityClassifier;
import com.grahambartley.dogsunleashed.entity.variant.UnleashedDogCoat;
import com.grahambartley.dogsunleashed.network.DogConnectionsListener;
import com.grahambartley.dogsunleashed.network.ModNetworking;
import com.grahambartley.dogsunleashed.network.ModNetworkingClient;
import com.grahambartley.dogsunleashed.pet.BreedComposition;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.screen.FamilyTreeLayout.NodePosition;
import com.grahambartley.dogsunleashed.screen.FamilyTreeLayout.Relations;
import com.grahambartley.dogsunleashed.util.DimensionLabelFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

/**
 * The per-pet home screen, opened by clicking a dog in the Pet Manager: a large portrait on the
 * left, the record's details on the right, per-pet actions, and a small non-interactive preview of
 * the dog's direct family that links into the full {@link FamilyTreeScreen}. The preview reuses
 * {@link FamilyTreeLayout} with only this dog expanded, scaled to fit its box.
 */
public class PetDetailsScreen extends Screen implements DogConnectionsListener {

  static final int PORTRAIT_SIZE = 96;
  static final int PREVIEW_NODE_SIZE = 26;
  static final float PREVIEW_SCALE = 0.45f;
  private static final int INFO_TOP = 44;
  private static final int INFO_LINE_HEIGHT = 14;
  private static final int STATS_SECTION_GAP = 6;
  static final int FAMILY_PREVIEW_HEIGHT = 96;
  static final int MIN_FAMILY_PREVIEW_HEIGHT = 50;
  private static final int LABEL_COLOR = 0xFFBBBBBB;
  private static final int VALUE_COLOR = 0xFFFFFFFF;
  private static final int PANEL_COLOR = 0x40333333;
  private static final int PANEL_BORDER = 0xFF555555;
  private static final int EDGE_COLOR = 0xFF8A8A8A;
  private static final float LOW_HEALTH_COLOR_THRESHOLD = 0.5f;

  private final Screen parent;
  private final ModNetworking.PetSyncData pet;
  private final DogPortraitRenderer portraits = new DogPortraitRenderer();
  private @Nullable ModNetworking.SyncDogConnectionsPayload connections;

  public PetDetailsScreen(final Screen parent, final ModNetworking.PetSyncData pet) {
    super(Text.translatable("screen.dogs-unleashed.pet_details.title"));
    this.parent = parent;
    this.pet = pet;
  }

  @Override
  protected void init() {
    addDrawableChild(
        ButtonWidget.builder(
                Text.translatable("screen.dogs-unleashed.pet_details.back"),
                button -> MinecraftClient.getInstance().setScreen(parent))
            .dimensions(6, 6, 50, 20)
            .build());

    final int actionsY = this.height - 30;
    final ButtonWidget summonButton =
        addDrawableChild(
            ButtonWidget.builder(
                    Text.translatable("screen.dogs-unleashed.pet_manager.summon"),
                    button -> ModNetworkingClient.sendSummonPet(UUID.fromString(pet.petId())))
                .dimensions(this.width / 2 - 125, actionsY, 120, 20)
                .build());
    summonButton.active = pet.alive();

    addDrawableChild(
        ButtonWidget.builder(
                Text.translatable("screen.dogs-unleashed.pet_details.view_family_tree"),
                button ->
                    MinecraftClient.getInstance()
                        .setScreen(new FamilyTreeScreen(this, focusConnectionData())))
            .dimensions(this.width / 2 + 5, actionsY, 120, 20)
            .build());

    ModNetworkingClient.sendRequestDogConnections(UUID.fromString(pet.petId()));
  }

  private ModNetworking.ConnectionDogSyncData focusConnectionData() {
    if (connections != null) {
      return connections.self();
    }
    final MinecraftClient client = MinecraftClient.getInstance();
    final String ownerId =
        client.player != null ? client.player.getUuid().toString() : new UUID(0, 0).toString();
    final String ownerName = client.player != null ? client.player.getGameProfile().getName() : "";
    return new ModNetworking.ConnectionDogSyncData(pet, ownerId, ownerName);
  }

  @Override
  public void onDogConnections(final ModNetworking.SyncDogConnectionsPayload payload) {
    if (payload.self().pet().petId().equals(pet.petId())) {
      connections = payload;
    }
  }

  @Override
  public void render(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    super.render(context, mouseX, mouseY, delta);

    context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);

    final int portraitX = portraitX(this.width);
    final int portraitY = 40;
    context.fill(
        portraitX - 4,
        portraitY - 4,
        portraitX + PORTRAIT_SIZE + 4,
        portraitY + PORTRAIT_SIZE + 4,
        PANEL_COLOR);
    context.drawBorder(
        portraitX - 4, portraitY - 4, PORTRAIT_SIZE + 8, PORTRAIT_SIZE + 8, PANEL_BORDER);
    portraits.draw(
        context, this.textRenderer, pet, portraitX, portraitY, PORTRAIT_SIZE, mouseX, mouseY);
    context.drawCenteredTextWithShadow(
        this.textRenderer,
        pet.name(),
        portraitX + PORTRAIT_SIZE / 2,
        portraitY + PORTRAIT_SIZE + 10,
        pet.alive() ? 0xFFFFFF : 0xAAAAAA);

    renderInfo(context);
    renderFamilyPreview(context, mouseX, mouseY);
  }

  private void renderInfo(final DrawContext context) {
    final int x = infoColumnX(this.width);
    int y = INFO_TOP;

    y =
        drawInfoLine(
            context,
            x,
            y,
            "screen.dogs-unleashed.pet_details.breed",
            DogTraitFormat.formatComposition(
                composition(), breed -> Text.translatable(breed.translationKey()).getString()),
            VALUE_COLOR);
    final UnleashedDogCoat coat = DogCoats.coatOf(pet.displayBreed(), pet.coatVariant());
    if (coat != null) {
      y =
          drawInfoLine(
              context,
              x,
              y,
              "screen.dogs-unleashed.pet_details.coat",
              Text.translatable(coat.translationKey()).getString(),
              VALUE_COLOR);
    }
    final int rarityChance =
        DogRarityClassifier.chancePercent(pet.displayBreed(), pet.coatVariant());
    final DogRarity rarity = DogRarityClassifier.classify(rarityChance);
    y =
        drawInfoLine(
            context,
            x,
            y,
            "screen.dogs-unleashed.pet_details.rarity",
            DogTraitFormat.formatRarity(
                Text.translatable(rarity.translationKey()).getString(), rarityChance),
            rarity.colorArgb());
    y =
        drawInfoLine(
            context,
            x,
            y,
            "screen.dogs-unleashed.pet_details.owner",
            resolveOwnerName(),
            VALUE_COLOR);
    if (pet.alive()) {
      final int healthColor =
          pet.health() > pet.maxHealth() * LOW_HEALTH_COLOR_THRESHOLD ? 0xFF55FF55 : 0xFFFF5555;
      y =
          drawInfoLine(
              context,
              x,
              y,
              "screen.dogs-unleashed.pet_details.health",
              String.format("%.1f / %.1f ❤", pet.health(), pet.maxHealth()),
              healthColor);
      y =
          drawInfoLine(
              context,
              x,
              y,
              "screen.dogs-unleashed.pet_details.age",
              Text.translatable(
                      pet.baby()
                          ? "screen.dogs-unleashed.pet_details.age_baby"
                          : "screen.dogs-unleashed.pet_details.age_adult")
                  .getString(),
              VALUE_COLOR);
      y =
          drawInfoLine(
              context,
              x,
              y,
              "screen.dogs-unleashed.pet_details.location",
              String.format(
                  "%s (%d, %d, %d)",
                  DimensionLabelFormatter.format(pet.dimension()),
                  pet.posX(),
                  pet.posY(),
                  pet.posZ()),
              VALUE_COLOR);
    } else {
      y =
          drawInfoLine(
              context,
              x,
              y,
              "screen.dogs-unleashed.pet_details.status",
              Text.translatable("screen.dogs-unleashed.pet_manager.deceased").getString(),
              0xFFFF5555);
    }

    y += STATS_SECTION_GAP;
    context.drawText(
        this.textRenderer,
        Text.translatable("screen.dogs-unleashed.pet_details.stats"),
        x,
        y,
        0xFFFFFFFF,
        true);
    y += INFO_LINE_HEIGHT;
    DogStatsPanel.drawForPet(context, this.textRenderer, pet, x, y, 90);
  }

  private List<BreedShare> composition() {
    if (connections != null && !connections.focusComposition().isEmpty()) {
      return connections.focusComposition();
    }
    if (!pet.composition().isEmpty()) {
      return pet.composition();
    }
    return BreedComposition.pureComposition(pet.breed());
  }

  private String resolveOwnerName() {
    if (connections != null && !connections.self().ownerName().isEmpty()) {
      return connections.self().ownerName();
    }
    final MinecraftClient client = MinecraftClient.getInstance();
    return client.player != null ? client.player.getGameProfile().getName() : "";
  }

  private int drawInfoLine(
      final DrawContext context,
      final int x,
      final int y,
      final String labelKey,
      final String value,
      final int valueColor) {
    final Text label = Text.translatable(labelKey);
    context.drawText(this.textRenderer, label, x, y, LABEL_COLOR, true);
    context.drawText(
        this.textRenderer,
        this.textRenderer.trimToWidth(value, this.width - x - 90 - 10),
        x + 90,
        y,
        valueColor,
        true);
    return y + INFO_LINE_HEIGHT;
  }

  private void renderFamilyPreview(final DrawContext context, final int mouseX, final int mouseY) {
    if (!shouldShowFamilyPreview(this.height, pet.alive())) {
      return;
    }
    final int boxTop = previewBoxTop(this.height, pet.alive());
    final int boxLeft = 20;
    final int boxRight = this.width - 20;
    context.fill(boxLeft, boxTop, boxRight, this.height - 40, PANEL_COLOR);
    context.drawBorder(
        boxLeft, boxTop, boxRight - boxLeft, this.height - 40 - boxTop, PANEL_BORDER);
    context.drawText(
        this.textRenderer,
        Text.translatable("screen.dogs-unleashed.pet_details.family_preview"),
        boxLeft + 6,
        boxTop + 5,
        0xFFFFFFFF,
        true);

    if (connections == null || hasNoRelatives(connections)) {
      context.drawCenteredTextWithShadow(
          this.textRenderer,
          Text.translatable("screen.dogs-unleashed.pet_details.no_family"),
          (boxLeft + boxRight) / 2,
          (boxTop + this.height - 40) / 2,
          0x888888);
      return;
    }

    final Map<String, ModNetworking.ConnectionDogSyncData> dogs = new HashMap<>();
    dogs.put(connections.self().pet().petId(), connections.self());
    connections.parents().forEach(dog -> dogs.put(dog.pet().petId(), dog));
    connections.mates().forEach(dog -> dogs.put(dog.pet().petId(), dog));
    connections.siblings().forEach(dog -> dogs.put(dog.pet().petId(), dog));
    connections.children().forEach(dog -> dogs.put(dog.pet().petId(), dog));

    final String focusId = connections.self().pet().petId();
    final Map<String, Relations> relations =
        Map.of(
            focusId,
            new Relations(
                connections.parents().stream().map(d -> d.pet().petId()).toList(),
                connections.mates().stream().map(d -> d.pet().petId()).toList(),
                connections.siblings().stream().map(d -> d.pet().petId()).toList(),
                connections.children().stream().map(d -> d.pet().petId()).toList()));
    final Map<String, NodePosition> positions =
        FamilyTreeLayout.layout(focusId, Set.of(focusId), relations);

    final int previewCenterX = (boxLeft + boxRight) / 2;
    final int previewCenterY = (boxTop + this.height - 40) / 2 + 4;
    for (final Map.Entry<String, NodePosition> entry : positions.entrySet()) {
      final ModNetworking.ConnectionDogSyncData dog = dogs.get(entry.getKey());
      if (dog == null) {
        continue;
      }
      final int x =
          previewCenterX
              + Math.round(
                  FamilyTreeScreen.nodeWorldCenterX(entry.getValue()) * PREVIEW_SCALE
                      - PREVIEW_NODE_SIZE / 2.0f);
      final int y =
          previewCenterY
              + Math.round(
                  FamilyTreeScreen.nodeWorldCenterY(entry.getValue()) * PREVIEW_SCALE
                      - PREVIEW_NODE_SIZE / 2.0f);
      final boolean isFocus = entry.getKey().equals(focusId);
      if (isFocus) {
        context.drawBorder(x - 2, y - 2, PREVIEW_NODE_SIZE + 4, PREVIEW_NODE_SIZE + 4, 0xFFFFDD66);
      }
      if (!isFocus) {
        final int lineY = y + PREVIEW_NODE_SIZE / 2;
        final int focusScreenX = previewCenterX;
        context.fill(
            Math.min(x + PREVIEW_NODE_SIZE / 2, focusScreenX),
            lineY,
            Math.max(x + PREVIEW_NODE_SIZE / 2, focusScreenX),
            lineY + 1,
            EDGE_COLOR);
      }
      portraits.draw(
          context, this.textRenderer, dog.pet(), x, y, PREVIEW_NODE_SIZE, mouseX, mouseY);
    }
  }

  static boolean hasNoRelatives(final ModNetworking.SyncDogConnectionsPayload payload) {
    return payload.parents().isEmpty()
        && payload.mates().isEmpty()
        && payload.siblings().isEmpty()
        && payload.children().isEmpty();
  }

  static int portraitX(final int screenWidth) {
    return screenWidth / 4 - PORTRAIT_SIZE / 2;
  }

  static int infoColumnX(final int screenWidth) {
    return screenWidth / 2 - 20;
  }

  static int infoBottom(final boolean alive) {
    final int infoLines = alive ? 7 : 5;
    return INFO_TOP
        + infoLines * INFO_LINE_HEIGHT
        + STATS_SECTION_GAP
        + INFO_LINE_HEIGHT
        + DogStatsPanel.totalHeight();
  }

  static int previewBoxTop(final int screenHeight, final boolean alive) {
    return Math.max(
        screenHeight - 40 - FAMILY_PREVIEW_HEIGHT, infoBottom(alive) + STATS_SECTION_GAP);
  }

  static boolean shouldShowFamilyPreview(final int screenHeight, final boolean alive) {
    return screenHeight - 40 - previewBoxTop(screenHeight, alive) >= MIN_FAMILY_PREVIEW_HEIGHT;
  }

  @Override
  public void removed() {
    portraits.clear();
    super.removed();
  }

  @Override
  public void close() {
    MinecraftClient.getInstance().setScreen(parent);
  }

  @Override
  public boolean shouldPause() {
    return false;
  }
}
