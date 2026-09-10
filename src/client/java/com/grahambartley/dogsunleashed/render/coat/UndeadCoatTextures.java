package com.grahambartley.dogsunleashed.render.coat;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.render.coat.UndeadDogEyes.EyePixel;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class UndeadCoatTextures {

  private static final Map<Identifier, Identifier> CACHE = new HashMap<>();

  private UndeadCoatTextures() {}

  public static void clear() {
    final MinecraftClient client = MinecraftClient.getInstance();
    if (client != null && client.getTextureManager() != null) {
      CACHE.values().forEach(client.getTextureManager()::destroyTexture);
    }
    CACHE.clear();
  }

  public static Identifier undeadOf(final Identifier living, final UndeadDogEyes eyes) {
    final Identifier cached = CACHE.get(living);
    if (cached != null) {
      return cached;
    }
    final Identifier undead = decay(living, eyes);
    CACHE.put(living, undead != null ? undead : living);
    return undead != null ? undead : living;
  }

  private static @Nullable Identifier decay(final Identifier living, final UndeadDogEyes eyes) {
    final SourcePixels source = sourceOf(living);
    if (source == null) {
      return null;
    }
    try {
      final NativeImage image = decayedImage(source.image(), eyes);
      final Identifier id =
          Identifier.of(MOD_ID, "undead/" + Integer.toHexString(living.toString().hashCode()));
      MinecraftClient.getInstance()
          .getTextureManager()
          .registerTexture(id, new NativeImageBackedTexture(image));
      return id;
    } finally {
      if (source.owned()) {
        source.image().close();
      }
    }
  }

  private static NativeImage decayedImage(final NativeImage source, final UndeadDogEyes eyes) {
    final int width = source.getWidth();
    final int height = source.getHeight();
    final NativeImage image = new NativeImage(NativeImage.Format.RGBA, width, height, false);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        final int colour = source.getColor(x, y);
        if (alpha(colour) == 0
            || (isIslandEdge(source, x, y) && UndeadTextureTransform.isTattered(x, y))) {
          image.setColor(x, y, 0);
          continue;
        }
        final int rgb = (red(colour) << 16) | (green(colour) << 8) | blue(colour);
        final int decayed = UndeadTextureTransform.decayed(rgb, x, y);
        image.setColor(
            x, y, abgr(255, (decayed >> 16) & 0xFF, (decayed >> 8) & 0xFF, decayed & 0xFF));
      }
    }
    if (width == eyes.atlasSize() && height == eyes.atlasSize()) {
      for (final EyePixel pixel : eyes.pixels()) {
        final int rgb = UndeadTextureTransform.EYE_RGB;
        image.setColor(
            pixel.x(), pixel.y(), abgr(255, (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
      }
    }
    return image;
  }

  private static boolean isIslandEdge(final NativeImage source, final int x, final int y) {
    return isTransparentAt(source, x - 1, y)
        || isTransparentAt(source, x + 1, y)
        || isTransparentAt(source, x, y - 1)
        || isTransparentAt(source, x, y + 1);
  }

  private static boolean isTransparentAt(final NativeImage source, final int x, final int y) {
    if (x < 0 || y < 0 || x >= source.getWidth() || y >= source.getHeight()) {
      return false;
    }
    return alpha(source.getColor(x, y)) == 0;
  }

  private record SourcePixels(NativeImage image, boolean owned) {}

  private static @Nullable SourcePixels sourceOf(final Identifier living) {
    final MinecraftClient client = MinecraftClient.getInstance();
    if (client == null) {
      return null;
    }
    final Resource resource = client.getResourceManager().getResource(living).orElse(null);
    if (resource != null) {
      try (InputStream stream = resource.getInputStream()) {
        return new SourcePixels(NativeImage.read(stream), true);
      } catch (final IOException exception) {
        return null;
      }
    }
    if (client.getTextureManager().getTexture(living)
            instanceof NativeImageBackedTexture dynamicTexture
        && dynamicTexture.getImage() != null) {
      return new SourcePixels(dynamicTexture.getImage(), false);
    }
    return null;
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
