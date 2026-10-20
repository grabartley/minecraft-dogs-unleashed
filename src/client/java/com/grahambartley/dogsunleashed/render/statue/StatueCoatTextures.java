package com.grahambartley.dogsunleashed.render.statue;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;
import static com.grahambartley.dogsunleashed.render.coat.CoatImages.abgr;
import static com.grahambartley.dogsunleashed.render.coat.CoatImages.alpha;
import static com.grahambartley.dogsunleashed.render.coat.CoatImages.blue;
import static com.grahambartley.dogsunleashed.render.coat.CoatImages.green;
import static com.grahambartley.dogsunleashed.render.coat.CoatImages.red;

import com.grahambartley.dogsunleashed.block.StatueMaterial;
import com.grahambartley.dogsunleashed.render.coat.CoatImages;
import java.util.Arrays;
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
    final double[] lumas = new double[width * height];
    int opaque = 0;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        final int colour = source.getColor(x, y);
        if (alpha(colour) > 0) {
          lumas[opaque++] = StatueRamp.luma(red(colour), green(colour), blue(colour));
        }
      }
    }
    final int[] steps = StatueRamp.steps(Arrays.copyOf(lumas, opaque));
    final NativeImage image = new NativeImage(NativeImage.Format.RGBA, width, height, false);
    int next = 0;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        final int colour = source.getColor(x, y);
        if (alpha(colour) == 0) {
          image.setColor(x, y, 0);
          continue;
        }
        final int shade = ramp[steps[next++]];
        image.setColor(x, y, abgr(alpha(colour), red(shade), green(shade), blue(shade)));
      }
    }
    return image;
  }
}
