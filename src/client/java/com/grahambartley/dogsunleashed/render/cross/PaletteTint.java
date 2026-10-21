package com.grahambartley.dogsunleashed.render.cross;

import com.grahambartley.dogsunleashed.render.rig.Texel;
import java.util.List;
import java.util.function.Predicate;

public final class PaletteTint {

  public record Pull(CoatPalette palette, float weight) {}

  private PaletteTint() {}

  public static int tinted(final int rgb, final CoatPalette own, final List<Pull> pulls) {
    final float[] closeness = own.closeness(rgb);
    float red = (rgb >> 16) & 0xFF;
    float green = (rgb >> 8) & 0xFF;
    float blue = rgb & 0xFF;
    for (int index = 0; index < CoatPalette.SIZE; index++) {
      final int base = own.colour(index);
      for (final Pull pull : pulls) {
        final int target = pull.palette().colour(index);
        final float weight = closeness[index] * pull.weight();
        red += weight * (((target >> 16) & 0xFF) - ((base >> 16) & 0xFF));
        green += weight * (((target >> 8) & 0xFF) - ((base >> 8) & 0xFF));
        blue += weight * ((target & 0xFF) - (base & 0xFF));
      }
    }
    return (channel(red) << 16) | (channel(green) << 8) | channel(blue);
  }

  public static int[] tintedAbgr(
      final int[] abgr,
      final int width,
      final Predicate<Texel> coat,
      final CoatPalette own,
      final List<Pull> pulls) {
    final int[] out = abgr.clone();
    for (int index = 0; index < out.length; index++) {
      final int colour = out[index];
      if ((colour >>> 24) == 0 || !coat.test(new Texel(index % width, index / width))) {
        continue;
      }
      final int rgb = ((colour & 0xFF) << 16) | (colour & 0xFF00) | ((colour >> 16) & 0xFF);
      final int tinted = tinted(rgb, own, pulls);
      out[index] =
          (colour & 0xFF000000)
              | ((tinted & 0xFF) << 16)
              | (tinted & 0xFF00)
              | ((tinted >> 16) & 0xFF);
    }
    return out;
  }

  private static int channel(final float value) {
    return Math.clamp(Math.round(value), 0, 255);
  }
}
