package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.HuskyEntity;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.network.ModNetworking;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;

/**
 * Renders a pet record as a live 3D dog portrait at any size, falling back to a breed-colored
 * placeholder when the entity cannot be created (no world, unknown entity type). Portrait entities
 * are client-side throwaways cached per pet id; the owning screen must call {@link #clear()} when
 * it stops rendering (typically from {@code removed()}) so they get discarded.
 */
public final class DogPortraitRenderer {

  private static final int BASE_SIZE = 48;
  private static final int BASE_SHADOW_SIZE = 22;
  private static final float BASE_SCALE = 0.35f;
  private static final float FULL_COLOR = 1.0f;
  private static final int COLLAR_COLOR_COUNT = DyeColor.values().length;
  private static final int DECEASED_OVERLAY_COLOR = 0x889A9A9A;

  private final Map<UUID, UnleashedDogEntity> portraitEntities = new HashMap<>();

  public void draw(
      final DrawContext context,
      final TextRenderer textRenderer,
      final ModNetworking.PetSyncData pet,
      final int x,
      final int y,
      final int size,
      final float mouseX,
      final float mouseY) {
    final UnleashedDogEntity entity = obtainPortraitEntity(pet);
    if (entity == null) {
      drawMissingPortraitPlaceholder(context, textRenderer, pet, x, y, size);
      return;
    }
    context.setShaderColor(FULL_COLOR, FULL_COLOR, FULL_COLOR, FULL_COLOR);
    InventoryScreen.drawEntity(
        context,
        x,
        y,
        x + size,
        y + size,
        Math.max(1, Math.round(BASE_SHADOW_SIZE * (size / (float) BASE_SIZE))),
        BASE_SCALE,
        mouseX,
        mouseY,
        entity);
    if (!pet.alive()) {
      context.fill(RenderLayer.getGuiOverlay(), x, y, x + size, y + size, DECEASED_OVERLAY_COLOR);
    }
    context.setShaderColor(FULL_COLOR, FULL_COLOR, FULL_COLOR, FULL_COLOR);
  }

  public void clear() {
    for (final UnleashedDogEntity entity : portraitEntities.values()) {
      entity.discard();
    }
    portraitEntities.clear();
  }

  private UnleashedDogEntity obtainPortraitEntity(final ModNetworking.PetSyncData pet) {
    final MinecraftClient client = MinecraftClient.getInstance();
    if (client.world == null) {
      return null;
    }
    final EntityType<? extends UnleashedDogEntity> type =
        pet.breed() == null ? null : ModEntities.getDogEntityType(pet.breed());
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

  private static void applyPetAppearance(
      final UnleashedDogEntity dog, final ModNetworking.PetSyncData pet) {
    dog.setBaby(pet.baby());
    dog.setCollarColor(DyeColor.byId(Math.floorMod(pet.collarColor(), COLLAR_COLOR_COUNT)));
    final NbtCompound nbt = new NbtCompound();
    if (pet.coatVariant() >= 0) {
      nbt.putInt(ModNbtKeys.COAT_VARIANT, pet.coatVariant());
    }
    if (dog instanceof HuskyEntity) {
      nbt.putInt(ModNbtKeys.EYE_COLOR_VARIANT, pet.huskyEyeVariant());
    }
    dog.readCustomDataFromNbt(nbt);
  }

  private static void drawMissingPortraitPlaceholder(
      final DrawContext context,
      final TextRenderer textRenderer,
      final ModNetworking.PetSyncData pet,
      final int x,
      final int y,
      final int size) {
    final UnleashedDogBreed.SpawnEggColors breedColors = pet.breed().spawnEggColors();
    final int backgroundColor = toOpaqueColor(breedColors.secondary());
    final int borderColor = toOpaqueColor(breedColors.primary());
    final int textColor = placeholderTextColor(backgroundColor);
    final String label = placeholderBreedLabel(pet.breed());

    context.fill(x, y, x + size, y + size, backgroundColor);
    context.drawBorder(x, y, size, size, borderColor);
    context.drawCenteredTextWithShadow(
        textRenderer, label, x + size / 2, y + (size - textRenderer.fontHeight) / 2, textColor);
    if (!pet.alive()) {
      context.fill(RenderLayer.getGuiOverlay(), x, y, x + size, y + size, DECEASED_OVERLAY_COLOR);
    }
  }

  static String placeholderBreedLabel(final UnleashedDogBreed breed) {
    final String compactId = breed.serializedId().replace("_", "");
    if (compactId.isEmpty()) {
      return "?";
    }
    return compactId.substring(0, Math.min(3, compactId.length())).toUpperCase(Locale.ROOT);
  }

  static int toOpaqueColor(final int rgbColor) {
    return 0xFF000000 | rgbColor;
  }

  static int placeholderTextColor(final int backgroundColor) {
    final int red = (backgroundColor >> 16) & 0xFF;
    final int green = (backgroundColor >> 8) & 0xFF;
    final int blue = backgroundColor & 0xFF;
    final int brightness = (red * 299 + green * 587 + blue * 114) / 1000;
    return brightness >= 140 ? 0xFF202020 : 0xFFF5F5F5;
  }
}
