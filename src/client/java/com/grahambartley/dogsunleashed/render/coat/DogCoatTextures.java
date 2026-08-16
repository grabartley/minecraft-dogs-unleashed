package com.grahambartley.dogsunleashed.render.coat;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.entity.rig.DogEarShape;
import com.grahambartley.dogsunleashed.render.coat.CoatPigments.Donor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Builds a dog's coat texture from its layered pieces and caches it. Composition happens once per
 * distinct appearance, never per frame: the cache key is the coat layout plus the blended pigments
 * plus the inherited ear shape, so every dog that looks the same shares one texture.
 */
public final class DogCoatTextures {

  private static final Map<String, Identifier> CACHE = new HashMap<>();
  private static final Map<String, CoatRecipe> RECIPES = new HashMap<>();
  private static final int SIZE = 128;

  private DogCoatTextures() {}

  /** Drops every generated texture, so a resource reload repaints from the new art. */
  public static void clear() {
    final MinecraftClient client = MinecraftClient.getInstance();
    if (client != null && client.getTextureManager() != null) {
      CACHE.values().forEach(client.getTextureManager()::destroyTexture);
    }
    CACHE.clear();
    RECIPES.clear();
  }

  public static @Nullable CoatRecipe recipe(final String coatId) {
    if (RECIPES.containsKey(coatId)) {
      return RECIPES.get(coatId);
    }
    CoatRecipe recipe = null;
    final Resource resource = resourceAt(coatId, "recipe.json");
    if (resource != null) {
      try (InputStream stream = resource.getInputStream()) {
        final JsonObject json =
            JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                .getAsJsonObject();
        recipe = CoatRecipe.fromJson(json);
      } catch (final IOException | RuntimeException exception) {
        recipe = null;
      }
    }
    RECIPES.put(coatId, recipe);
    return recipe;
  }

  /**
   * Returns the composited texture for a dog, or null when its coat has no layered art and the
   * caller should fall back to the flat texture.
   */
  public static @Nullable Identifier composited(
      final String layoutCoatId,
      final List<Donor> donors,
      final DogEarShape earShape,
      final String earDonorCoatId) {
    final CoatRecipe layout = recipe(layoutCoatId);
    if (layout == null) {
      return null;
    }
    final Map<String, Integer> pigments = CoatPigments.blend(layout, donors);
    final String key = cacheKey(layoutCoatId, earShape, earDonorCoatId, pigments);
    final Identifier cached = CACHE.get(key);
    if (cached != null) {
      return cached;
    }

    final NativeImage image =
        paint(layoutCoatId, layout, pigments, earShape, earDonorCoatId, donors);
    if (image == null) {
      return null;
    }
    final Identifier id =
        Identifier.of(
            MOD_ID,
            "coat/"
                + Integer.toHexString(key.hashCode())
                + "_"
                + Integer.toHexString(key.length()));
    MinecraftClient.getInstance()
        .getTextureManager()
        .registerTexture(id, new NativeImageBackedTexture(image));
    CACHE.put(key, id);
    return id;
  }

  private static @Nullable NativeImage paint(
      final String layoutCoatId,
      final CoatRecipe layout,
      final Map<String, Integer> pigments,
      final DogEarShape earShape,
      final String earDonorCoatId,
      final List<Donor> donors) {
    final NativeImage base = read(layoutCoatId, "base.png");
    if (base == null) {
      return null;
    }
    final NativeImage image = new NativeImage(NativeImage.Format.RGBA, SIZE, SIZE, false);
    image.fillRect(0, 0, SIZE, SIZE, 0);

    try {
      for (final String slot : layout.slots()) {
        final NativeImage mask = read(layoutCoatId, "mask_" + slot + ".png");
        if (mask == null) {
          continue;
        }
        try {
          tint(
              image,
              base,
              mask,
              pigments.getOrDefault(slot, 0xFFFFFF),
              layout.baseScaleOf(slot),
              null);
        } finally {
          mask.close();
        }
      }

      if (!earDonorCoatId.equals(layoutCoatId)) {
        paintInheritedEars(image, earShape, earDonorCoatId, donors);
      }

      stampFeatures(image, layoutCoatId);
      final List<CoatEarIslands.Island> inherited = CoatEarIslands.of(earShape);
      if (!earDonorCoatId.equals(layoutCoatId)) {
        stampFeaturesIn(image, earDonorCoatId, inherited);
      }
    } finally {
      base.close();
    }
    return image;
  }

  /**
   * A dog can wear one ancestor's ears on another ancestor's coat, and only the donor's art covers
   * that ear's corner of the atlas, so those islands are painted from the donor's layers.
   */
  private static void paintInheritedEars(
      final NativeImage image,
      final DogEarShape earShape,
      final String earDonorCoatId,
      final List<Donor> donors) {
    final CoatRecipe donorRecipe = recipe(earDonorCoatId);
    if (donorRecipe == null) {
      return;
    }
    final List<CoatEarIslands.Island> islands = CoatEarIslands.of(earShape);
    if (islands.isEmpty()) {
      return;
    }
    final NativeImage donorBase = read(earDonorCoatId, "base.png");
    if (donorBase == null) {
      return;
    }
    try {
      final Map<String, Integer> donorPigments = CoatPigments.blend(donorRecipe, donors);
      for (final String slot : donorRecipe.slots()) {
        final NativeImage mask = read(earDonorCoatId, "mask_" + slot + ".png");
        if (mask == null) {
          continue;
        }
        try {
          tint(
              image,
              donorBase,
              mask,
              donorPigments.getOrDefault(slot, 0xFFFFFF),
              donorRecipe.baseScaleOf(slot),
              islands);
        } finally {
          mask.close();
        }
      }
    } finally {
      donorBase.close();
    }
  }

  private static void tint(
      final NativeImage target,
      final NativeImage base,
      final NativeImage mask,
      final int pigment,
      final int baseScale,
      final @Nullable List<CoatEarIslands.Island> limitTo) {
    final int pigmentRed = (pigment >> 16) & 0xFF;
    final int pigmentGreen = (pigment >> 8) & 0xFF;
    final int pigmentBlue = pigment & 0xFF;
    for (int y = 0; y < SIZE; y++) {
      for (int x = 0; x < SIZE; x++) {
        if (limitTo != null && !CoatEarIslands.contains(limitTo, x, y)) {
          continue;
        }
        if (alpha(mask.getColor(x, y)) == 0) {
          continue;
        }
        final int baseColour = base.getColor(x, y);
        if (alpha(baseColour) == 0) {
          continue;
        }
        target.setColor(
            x,
            y,
            abgr(
                255,
                scale(pigmentRed, red(baseColour), baseScale),
                scale(pigmentGreen, green(baseColour), baseScale),
                scale(pigmentBlue, blue(baseColour), baseScale)));
      }
    }
  }

  private static void stampFeatures(final NativeImage image, final String coatId) {
    stampFeaturesIn(image, coatId, null);
  }

  private static void stampFeaturesIn(
      final NativeImage image,
      final String coatId,
      final @Nullable List<CoatEarIslands.Island> limitTo) {
    final NativeImage features = read(coatId, "features.png");
    if (features == null) {
      return;
    }
    try {
      for (int y = 0; y < SIZE; y++) {
        for (int x = 0; x < SIZE; x++) {
          if (limitTo != null && !CoatEarIslands.contains(limitTo, x, y)) {
            continue;
          }
          final int colour = features.getColor(x, y);
          if (alpha(colour) != 0) {
            image.setColor(x, y, colour);
          }
        }
      }
    } finally {
      features.close();
    }
  }

  private static String cacheKey(
      final String layoutCoatId,
      final DogEarShape earShape,
      final String earDonorCoatId,
      final Map<String, Integer> pigments) {
    final StringBuilder key = new StringBuilder(layoutCoatId).append('|').append(earShape.name());
    key.append('|').append(earDonorCoatId);
    pigments.forEach((slot, colour) -> key.append('|').append(slot).append('=').append(colour));
    return key.toString();
  }

  private static @Nullable NativeImage read(final String coatId, final String fileName) {
    final Resource resource = resourceAt(coatId, fileName);
    if (resource == null) {
      return null;
    }
    try (InputStream stream = resource.getInputStream()) {
      return NativeImage.read(stream);
    } catch (final IOException exception) {
      return null;
    }
  }

  private static @Nullable Resource resourceAt(final String coatId, final String fileName) {
    final MinecraftClient client = MinecraftClient.getInstance();
    if (client == null || client.getResourceManager() == null) {
      return null;
    }
    return client
        .getResourceManager()
        .getResource(Identifier.of(MOD_ID, "textures/entity/coat/" + coatId + "/" + fileName))
        .orElse(null);
  }

  private static int scale(final int pigment, final int baseChannel, final int baseScale) {
    return Math.clamp(Math.round(pigment * baseChannel / (float) baseScale), 0, 255);
  }

  private static int abgr(final int a, final int r, final int g, final int b) {
    return (a << 24) | (b << 16) | (g << 8) | r;
  }

  private static int alpha(final int abgr) {
    return (abgr >>> 24) & 0xFF;
  }

  private static int red(final int abgr) {
    return abgr & 0xFF;
  }

  private static int green(final int abgr) {
    return (abgr >>> 8) & 0xFF;
  }

  private static int blue(final int abgr) {
    return (abgr >>> 16) & 0xFF;
  }
}
