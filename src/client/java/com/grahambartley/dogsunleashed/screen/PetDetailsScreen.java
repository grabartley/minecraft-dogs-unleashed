package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.entity.variant.DogCoats;
import com.grahambartley.dogsunleashed.entity.variant.DogRarity;
import com.grahambartley.dogsunleashed.entity.variant.DogRarityClassifier;
import com.grahambartley.dogsunleashed.entity.variant.UnleashedDogCoat;
import com.grahambartley.dogsunleashed.network.DogConnectionsListener;
import com.grahambartley.dogsunleashed.network.ModNetworkingClient;
import com.grahambartley.dogsunleashed.network.payload.ConnectionDogSyncData;
import com.grahambartley.dogsunleashed.network.payload.PetSyncData;
import com.grahambartley.dogsunleashed.network.payload.RidingView;
import com.grahambartley.dogsunleashed.network.payload.SyncDogConnectionsPayload;
import com.grahambartley.dogsunleashed.pet.BreedComposition;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.util.DimensionLabelFormatter;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class PetDetailsScreen extends Screen implements DogConnectionsListener {

  static final int PORTRAIT_SIZE = 96;
  private static final int INFO_TOP = 44;
  private static final int INFO_LINE_HEIGHT = 14;
  private static final int STATS_SECTION_GAP = 6;
  private static final int LABEL_COLOR = 0xFFBBBBBB;
  private static final int VALUE_COLOR = 0xFFFFFFFF;
  private static final int PANEL_COLOR = 0x40333333;
  private static final int PANEL_BORDER = 0xFF555555;
  private static final float LOW_HEALTH_COLOR_THRESHOLD = 0.5f;

  private final Screen parent;
  private final PetSyncData pet;
  private final DogPortraitRenderer portraits = new DogPortraitRenderer();
  private @Nullable SyncDogConnectionsPayload connections;

  public PetDetailsScreen(final Screen parent, final PetSyncData pet) {
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
    summonButton.active = pet.summonable();

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

  private ConnectionDogSyncData focusConnectionData() {
    if (connections != null) {
      return connections.self();
    }
    final MinecraftClient client = MinecraftClient.getInstance();
    final String ownerId =
        client.player != null ? client.player.getUuid().toString() : new UUID(0, 0).toString();
    final String ownerName = client.player != null ? client.player.getGameProfile().getName() : "";
    return new ConnectionDogSyncData(pet, ownerId, ownerName, List.of(), false, List.of());
  }

  @Override
  public void onDogConnections(final SyncDogConnectionsPayload payload) {
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
        PetLifeStateDisplay.nameColor(pet.lifeState(), 0xFFFFFF, 0xAAAAAA));

    renderInfo(context);
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
            DogAppearanceFormat.formatComposition(
                composition(), breed -> Text.translatable(breed.translationKey()).getString()),
            VALUE_COLOR);
    final UnleashedDogCoat coat = DogCoats.coatOf(pet.displayBreed(), pet.look().coatVariant());
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
        DogRarityClassifier.chancePercent(pet.displayBreed(), pet.look().coatVariant());
    final DogRarity rarity = DogRarityClassifier.classify(rarityChance);
    y =
        drawInfoLine(
            context,
            x,
            y,
            "screen.dogs-unleashed.pet_details.rarity",
            DogAppearanceFormat.formatRarity(
                Text.translatable(rarity.translationKey()).getString(), rarityChance),
            rarity.colorArgb());
    y =
        drawInfoLine(
            context,
            x,
            y,
            "screen.dogs-unleashed.pet_details.fur",
            DogAppearanceFormat.furLine(pet),
            VALUE_COLOR);
    y =
        drawInfoLine(
            context,
            x,
            y,
            "screen.dogs-unleashed.pet_details.owner",
            resolveOwnerName(),
            VALUE_COLOR);
    final Text statusLabel = PetLifeStateDisplay.statusLabel(pet);
    if (statusLabel != null) {
      y =
          drawInfoLine(
              context,
              x,
              y,
              "screen.dogs-unleashed.pet_details.status",
              statusLabel.getString(),
              PetLifeStateDisplay.statusColor(pet));
    }
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
              Text.translatable(pet.age().translationKey()).getString(),
              VALUE_COLOR);
      y =
          drawInfoLine(
              context,
              x,
              y,
              "screen.dogs-unleashed.pet_details.bond",
              Text.translatable(pet.bond().translationKey()).getString(),
              VALUE_COLOR);
      if (!pet.personality().isEmpty()) {
        y =
            drawInfoLine(
                context,
                x,
                y,
                DogPersonalityFormat.LABEL_KEY,
                DogPersonalityFormat.format(pet.personality()),
                VALUE_COLOR);
      }
      if (pet.riding().kind() != RidingView.Kind.NONE) {
        y =
            drawInfoLine(
                context,
                x,
                y,
                "screen.dogs-unleashed.pet_details.riding",
                ridingText(pet.riding()).getString(),
                VALUE_COLOR);
      }
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

  static int portraitX(final int screenWidth) {
    return screenWidth / 4 - PORTRAIT_SIZE / 2;
  }

  static int infoColumnX(final int screenWidth) {
    return screenWidth / 2 - 20;
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

  static Text ridingText(final RidingView riding) {
    return riding.kind() == RidingView.Kind.READY
        ? Text.translatable(
            "screen.dogs-unleashed.pet_details.riding_ready", riding.staminaPercent())
        : Text.translatable("screen.dogs-unleashed.pet_details.riding_too_young");
  }
}
