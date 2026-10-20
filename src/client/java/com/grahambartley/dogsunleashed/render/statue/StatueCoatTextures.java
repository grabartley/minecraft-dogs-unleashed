package com.grahambartley.dogsunleashed.render.statue;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.block.StatueMaterial;
import com.grahambartley.dogsunleashed.render.coat.CoatImages;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class StatueCoatTextures {

  public static final Identifier RELOADER_ID = Identifier.of(MOD_ID, "statue_coat_textures");

  private static final StatueCoatTextureCache CACHE =
      new StatueCoatTextureCache(StatueCoatTextures::carve);

  private StatueCoatTextures() {}

  public static StatueCoatTextureCache cache() {
    return CACHE;
  }

  public static Identifier statueOf(final Identifier coat, final StatueMaterial material) {
    return CACHE.statueOf(coat, material);
  }

  private static @Nullable Identifier carve(final Identifier coat, final StatueMaterial material) {
    final int[] ramp = rampOf(material);
    if (ramp == null) {
      return null;
    }
    try (CoatImages.Source source = CoatImages.read(coat)) {
      if (source == null) {
        return null;
      }
      return CoatImages.register(
          Identifier.of(
              MOD_ID, "statue/" + material.id() + "/" + coat.getNamespace() + "/" + coat.getPath()),
          carvedImage(source.image(), ramp));
    }
  }

  private static int @Nullable [] rampOf(final StatueMaterial material) {
    try (CoatImages.Source source =
        CoatImages.read(Identifier.of(MOD_ID, "textures/statue/" + material.id() + ".png"))) {
      if (source == null || source.image().getWidth() < StatueRamp.STEPS) {
        return null;
      }
      final int[] ramp = new int[StatueRamp.STEPS];
      for (int step = 0; step < StatueRamp.STEPS; step++) {
        ramp[step] = source.image().getColor(step, 0);
      }
      return ramp;
    }
  }

  private static NativeImage carvedImage(final NativeImage source, final int[] ramp) {
    final int width = source.getWidth();
    final int height = source.getHeight();
    final int[] pixels = new int[width * height];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        pixels[y * width + x] = source.getColor(x, y);
      }
    }
    final int[] carved = StatueRamp.carve(pixels, ramp);
    final NativeImage image = new NativeImage(NativeImage.Format.RGBA, width, height, false);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        image.setColor(x, y, carved[y * width + x]);
      }
    }
    return image;
  }
}
