package com.grahambartley.screen;

import com.grahambartley.DogsUnleashed;
import com.grahambartley.ModEntities;
import com.grahambartley.entity.HuskyEntity;
import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.entity.variant.HuskyCoat;
import com.grahambartley.entity.variant.HuskyEyeColor;
import com.grahambartley.network.ModNetworking;
import com.grahambartley.network.ModNetworkingClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class PetManagerScreen extends Screen {

  private enum AliveFilter {
    ALL,
    ALIVE,
    DECEASED
  }

  private static final int ENTRY_HEIGHT = 60;
  private static final int ENTRY_WIDTH = 280;
  private static final int ENTRIES_PER_PAGE = 4;
  private static final int THUMBNAIL_SIZE = 48;

  private static final List<String> BREED_OPTIONS =
      List.of("", "husky", "dachshund", "beagle", "goldenretriever", "shibainu");

  private List<ModNetworking.PetSyncData> pets = new ArrayList<>();
  private TextFieldWidget searchField;
  private String currentBreedFilter = "";
  private AliveFilter currentAliveFilter = AliveFilter.ALL;
  private int scrollOffset = 0;
  private ModNetworking.PetSyncData selectedPet = null;
  private final Map<UUID, UnleashedDogEntity> portraitEntities = new HashMap<>();

  public PetManagerScreen() {
    super(Text.translatable("screen.dogs-unleashed.pet_manager.title"));
  }

  @Override
  protected void init() {
    final int centerX = this.width / 2;

    searchField =
        new TextFieldWidget(
            this.textRenderer,
            centerX - 140,
            40,
            180,
            20,
            Text.translatable("screen.dogs-unleashed.pet_manager.search"));
    searchField.setMaxLength(32);
    searchField.setChangedListener(this::onSearchChanged);
    addDrawableChild(searchField);

    addDrawableChild(
        ButtonWidget.builder(
                Text.translatable("screen.dogs-unleashed.pet_manager.search_button"),
                button -> refreshPetsList())
            .dimensions(centerX + 45, 40, 60, 20)
            .build());

    addDrawableChild(
        CyclingButtonWidget.<String>builder(
                breed ->
                    breed.isEmpty()
                        ? Text.translatable("screen.dogs-unleashed.pet_manager.all_breeds")
                        : Text.translatable("entity.dogs-unleashed." + breed))
            .values(BREED_OPTIONS)
            .initially(currentBreedFilter)
            .build(
                centerX - 140,
                65,
                120,
                20,
                Text.translatable("screen.dogs-unleashed.pet_manager.breed_filter"),
                (button, value) -> {
                  currentBreedFilter = value;
                  refreshPetsList();
                }));

    addDrawableChild(
        CyclingButtonWidget.<AliveFilter>builder(
                filter ->
                    switch (filter) {
                      case ALL -> Text.translatable("screen.dogs-unleashed.pet_manager.all_status");
                      case ALIVE ->
                          Text.translatable("screen.dogs-unleashed.pet_manager.alive_only");
                      case DECEASED ->
                          Text.translatable("screen.dogs-unleashed.pet_manager.deceased_only");
                    })
            .values(AliveFilter.values())
            .initially(currentAliveFilter)
            .build(
                centerX + 5,
                65,
                100,
                20,
                Text.translatable("screen.dogs-unleashed.pet_manager.status_filter"),
                (button, value) -> {
                  currentAliveFilter = value;
                  refreshPetsList();
                }));

    addDrawableChild(
        ButtonWidget.builder(
                Text.translatable("screen.dogs-unleashed.pet_manager.summon"), this::onSummon)
            .dimensions(centerX - 60, this.height - 50, 120, 20)
            .build());

    addDrawableChild(
        ButtonWidget.builder(Text.literal("▲"), button -> scroll(-1))
            .dimensions(centerX + ENTRY_WIDTH / 2 + 10, 95, 20, 20)
            .build());

    addDrawableChild(
        ButtonWidget.builder(Text.literal("▼"), button -> scroll(1))
            .dimensions(centerX + ENTRY_WIDTH / 2 + 10, this.height - 80, 20, 20)
            .build());

    refreshPetsList();
  }

  private void onSearchChanged(String query) {}

  private void refreshPetsList() {
    final String searchQuery = searchField != null ? searchField.getText() : "";
    final boolean filterAlive = currentAliveFilter != AliveFilter.ALL;
    final boolean aliveValue = currentAliveFilter == AliveFilter.ALIVE;
    ModNetworkingClient.sendRequestPets(currentBreedFilter, filterAlive, aliveValue, searchQuery);
  }

  public void updatePetsList(List<ModNetworking.PetSyncData> pets) {
    clearPortraitEntities();
    this.pets = new ArrayList<>(pets);
    this.scrollOffset = 0;
    if (selectedPet != null) {
      selectedPet =
          this.pets.stream()
              .filter(p -> p.petId().equals(selectedPet.petId()))
              .findFirst()
              .orElse(null);
    }
  }

  @Override
  public void close() {
    clearPortraitEntities();
    super.close();
  }

  private void clearPortraitEntities() {
    for (final UnleashedDogEntity entity : portraitEntities.values()) {
      entity.discard();
    }
    portraitEntities.clear();
  }

  private void scroll(int direction) {
    final int maxOffset = Math.max(0, pets.size() - ENTRIES_PER_PAGE);
    scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset + direction));
  }

  private void onSummon(ButtonWidget button) {
    if (selectedPet != null && selectedPet.alive()) {
      ModNetworkingClient.sendSummonPet(UUID.fromString(selectedPet.petId()));
    }
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    super.render(context, mouseX, mouseY, delta);

    final int centerX = this.width / 2;

    context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, 15, 0xFFFFFF);

    final int listStartY = 95;
    for (int i = 0; i < ENTRIES_PER_PAGE && i + scrollOffset < pets.size(); i++) {
      final ModNetworking.PetSyncData pet = pets.get(i + scrollOffset);
      final int entryY = listStartY + i * ENTRY_HEIGHT;
      renderPetEntry(
          context, pet, centerX - ENTRY_WIDTH / 2, entryY, (float) mouseX, (float) mouseY);
    }

    if (pets.isEmpty()) {
      context.drawCenteredTextWithShadow(
          this.textRenderer,
          Text.translatable("screen.dogs-unleashed.pet_manager.no_pets"),
          centerX,
          listStartY + 50,
          0x888888);
    }
  }

  private void renderPetEntry(
      DrawContext context,
      ModNetworking.PetSyncData pet,
      int x,
      int y,
      float mouseX,
      float mouseY) {
    final boolean isSelected = selectedPet != null && selectedPet.petId().equals(pet.petId());
    final boolean isHovered =
        mouseX >= x && mouseX < x + ENTRY_WIDTH && mouseY >= y && mouseY < y + ENTRY_HEIGHT;

    int bgColor;
    if (!pet.alive()) {
      bgColor = isSelected ? 0x80404040 : 0x60303030;
    } else if (isSelected) {
      bgColor = 0x80446644;
    } else if (isHovered) {
      bgColor = 0x60555555;
    } else {
      bgColor = 0x40333333;
    }

    context.fill(x, y, x + ENTRY_WIDTH, y + ENTRY_HEIGHT - 5, bgColor);

    final int imgX = x + 5;
    final int imgY = y + 3;

    drawPetPortrait(context, pet, imgX, imgY, mouseX, mouseY);

    final int textX = x + THUMBNAIL_SIZE + 15;
    final int nameColor = pet.alive() ? 0xFFFFFF : 0x888888;
    context.drawText(this.textRenderer, pet.name(), textX, y + 5, nameColor, true);

    final String breedName =
        Text.translatable("entity.dogs-unleashed." + pet.breedType()).getString();
    context.drawText(this.textRenderer, breedName, textX, y + 18, 0xAAAAAA, false);

    if (pet.alive()) {
      final String healthText = String.format("%.1f / %.1f ❤", pet.health(), pet.maxHealth());
      final int healthColor = pet.health() > pet.maxHealth() * 0.5 ? 0x55FF55 : 0xFF5555;
      context.drawText(this.textRenderer, healthText, textX, y + 31, healthColor, false);

      final String locationText = String.format("(%d, %d, %d)", pet.posX(), pet.posY(), pet.posZ());
      context.drawText(this.textRenderer, locationText, textX, y + 44, 0x888888, false);
    } else {
      context.drawText(
          this.textRenderer,
          Text.translatable("screen.dogs-unleashed.pet_manager.deceased"),
          textX,
          y + 31,
          0xFF5555,
          false);
    }
  }

  private static EntityType<? extends UnleashedDogEntity> breedToEntityType(
      final String breedType) {
    return switch (breedType) {
      case "husky" -> ModEntities.HUSKY;
      case "dachshund" -> ModEntities.DACHSHUND;
      case "beagle" -> ModEntities.BEAGLE;
      case "goldenretriever" -> ModEntities.GOLDEN_RETRIEVER;
      case "shibainu" -> ModEntities.SHIBA_INU;
      default -> null;
    };
  }

  private static void applyPetAppearance(
      final UnleashedDogEntity dog, final ModNetworking.PetSyncData pet) {
    dog.setBaby(pet.baby());
    dog.setCollarColor(DyeColor.byId(Math.floorMod(pet.collarColor(), 16)));
    if (dog instanceof HuskyEntity && pet.huskyCoatVariant() >= 0) {
      final NbtCompound nbt = new NbtCompound();
      nbt.putInt("CoatVariant", pet.huskyCoatVariant());
      nbt.putInt("EyeColorVariant", pet.huskyEyeVariant());
      dog.readCustomDataFromNbt(nbt);
    }
  }

  private UnleashedDogEntity obtainPortraitEntity(final ModNetworking.PetSyncData pet) {
    final MinecraftClient client = MinecraftClient.getInstance();
    if (client.world == null) {
      return null;
    }
    final EntityType<? extends UnleashedDogEntity> type = breedToEntityType(pet.breedType());
    if (type == null) {
      return null;
    }
    final UUID id = UUID.fromString(pet.petId());
    UnleashedDogEntity entity = portraitEntities.get(id);
    if (entity == null || entity.getType() != type) {
      if (entity != null) {
        entity.discard();
      }
      final UnleashedDogEntity created = (UnleashedDogEntity) type.create(client.world);
      if (created == null) {
        return null;
      }
      portraitEntities.put(id, created);
      entity = created;
    }
    applyPetAppearance(entity, pet);
    if (client.player != null) {
      entity.setPosition(client.player.getX(), client.player.getY(), client.player.getZ());
    }
    entity.setSitting(true);
    return entity;
  }

  private static Identifier resolveFallbackTexture(final ModNetworking.PetSyncData pet) {
    if ("husky".equals(pet.breedType()) && pet.huskyCoatVariant() >= 0) {
      final HuskyCoat coat = HuskyCoat.fromOrdinal(pet.huskyCoatVariant());
      final HuskyEyeColor eyes = HuskyEyeColor.fromOrdinal(pet.huskyEyeVariant());
      final String fileName =
          "husky_" + coat.textureCoatPrefix() + "_" + eyes.textureSuffix() + ".png";
      return Identifier.of(DogsUnleashed.MOD_ID, "textures/entity/" + fileName);
    }
    return Identifier.of(DogsUnleashed.MOD_ID, "textures/entity/" + pet.breedType() + ".png");
  }

  private void drawTexturePortrait(
      final DrawContext context,
      final ModNetworking.PetSyncData pet,
      final int imgX,
      final int imgY,
      final boolean greyed) {
    final Identifier textureId = resolveFallbackTexture(pet);
    if (greyed) {
      context.setShaderColor(0.4f, 0.4f, 0.4f, 1.0f);
    }
    context.drawTexture(
        textureId,
        imgX,
        imgY,
        0,
        0,
        THUMBNAIL_SIZE,
        THUMBNAIL_SIZE,
        THUMBNAIL_SIZE,
        THUMBNAIL_SIZE);
    if (greyed) {
      context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
  }

  private void drawPetPortrait(
      final DrawContext context,
      final ModNetworking.PetSyncData pet,
      final int imgX,
      final int imgY,
      final float mouseX,
      final float mouseY) {
    final UnleashedDogEntity entity = obtainPortraitEntity(pet);
    if (entity != null) {
      context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
      InventoryScreen.drawEntity(
          context,
          imgX,
          imgY,
          imgX + THUMBNAIL_SIZE,
          imgY + THUMBNAIL_SIZE,
          22,
          0.35f,
          mouseX,
          mouseY,
          entity);
      if (!pet.alive()) {
        context.fill(
            RenderLayer.getGuiOverlay(),
            imgX,
            imgY,
            imgX + THUMBNAIL_SIZE,
            imgY + THUMBNAIL_SIZE,
            0x889A9A9A);
      }
      context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
      return;
    }
    drawTexturePortrait(context, pet, imgX, imgY, !pet.alive());
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (button == 0) {
      final int centerX = this.width / 2;
      final int listStartY = 95;
      final int listX = centerX - ENTRY_WIDTH / 2;

      for (int i = 0; i < ENTRIES_PER_PAGE && i + scrollOffset < pets.size(); i++) {
        final int entryY = listStartY + i * ENTRY_HEIGHT;
        if (mouseX >= listX
            && mouseX < listX + ENTRY_WIDTH
            && mouseY >= entryY
            && mouseY < entryY + ENTRY_HEIGHT - 5) {
          selectedPet = pets.get(i + scrollOffset);
          return true;
        }
      }
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseScrolled(
      double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
    scroll(verticalAmount > 0 ? -1 : 1);
    return true;
  }

  @Override
  public boolean shouldPause() {
    return false;
  }
}
