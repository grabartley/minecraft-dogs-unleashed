package com.grahambartley.dogsunleashed.render.coat;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;
import static com.grahambartley.dogsunleashed.render.coat.CoatImages.abgr;
import static com.grahambartley.dogsunleashed.render.coat.CoatImages.alpha;
import static com.grahambartley.dogsunleashed.render.coat.CoatImages.blue;
import static com.grahambartley.dogsunleashed.render.coat.CoatImages.green;
import static com.grahambartley.dogsunleashed.render.coat.CoatImages.red;

import com.grahambartley.dogsunleashed.render.rig.Texel;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class UndeadCoatTextures {

  public static final Identifier RELOADER_ID = Identifier.of(MOD_ID, "undead_coat_textures");

  private static final UndeadCoatTextureCache CACHE =
      new UndeadCoatTextureCache(UndeadCoatTextures::decay);

  private UndeadCoatTextures() {}

  public static UndeadCoatTextureCache cache() {
    return CACHE;
  }

  public static Identifier undeadOf(final Identifier living, final UndeadDogEyes eyes) {
    return CACHE.undeadOf(living, eyes);
  }

  private static @Nullable Identifier decay(final Identifier living, final UndeadDogEyes eyes) {
    try (CoatImages.Source source = CoatImages.read(living)) {
      if (source == null) {
        return null;
      }
      return CoatImages.register(
          Identifier.of(MOD_ID, "undead/" + living.getNamespace() + "/" + living.getPath()),
          decayedImage(source.image(), eyes));
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
    if (eyes.fits(width, height)) {
      for (final Texel pixel : eyes.pupils()) {
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
}
