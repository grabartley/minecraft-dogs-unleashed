package com.grahambartley.dogsunleashed.render.coat;

/**
 * The per-pixel maths of the placeholder undead coat treatment: desaturate the living fur, pull it
 * toward a sickly green, rot mottled patches a shade darker, and eat ragged holes into the
 * silhouette. Everything is a pure function of colour and atlas position, so the same dog always
 * decays the same way and the result can sit in a texture cache.
 *
 * <p>Placeholder for the authored undead layers of #60, which replace this whole class.
 */
public final class UndeadTextureTransform {

  public static final int EYE_RGB = 0xE81E14;

  private static final int DESATURATION_PERCENT = 55;
  private static final int[] FUR_TINT_PERCENT = {68, 80, 62};
  private static final int[] MANGE_TINT_PERCENT = {76, 80, 70};
  private static final int MANGE_CELL_SHIFT = 2;
  private static final int MANGE_PERCENT = 30;
  private static final int TATTER_PERCENT = 40;

  private UndeadTextureTransform() {}

  /** Decays one opaque fur pixel, mange included; {@code rgb} is packed {@code 0xRRGGBB}. */
  public static int decayed(final int rgb, final int x, final int y) {
    final int r = (rgb >> 16) & 0xFF;
    final int g = (rgb >> 8) & 0xFF;
    final int b = rgb & 0xFF;
    final int luma = (r * 299 + g * 587 + b * 114) / 1000;

    final boolean mange = isMangePatch(x, y);
    final int outR = tint(desaturate(r, luma), 0, mange);
    final int outG = tint(desaturate(g, luma), 1, mange);
    final int outB = tint(desaturate(b, luma), 2, mange);
    return (outR << 16) | (outG << 8) | outB;
  }

  /**
   * Whether an island-edge pixel is torn away entirely, giving the silhouette its ragged fringe.
   */
  public static boolean isTattered(final int x, final int y) {
    return chance(hash(x, y), TATTER_PERCENT);
  }

  static boolean isMangePatch(final int x, final int y) {
    return chance(hash(x >> MANGE_CELL_SHIFT, y >> MANGE_CELL_SHIFT), MANGE_PERCENT);
  }

  private static int desaturate(final int channel, final int luma) {
    return channel + (luma - channel) * DESATURATION_PERCENT / 100;
  }

  private static int tint(final int channel, final int index, final boolean mange) {
    int out = channel * FUR_TINT_PERCENT[index] / 100;
    if (mange) {
      out = out * MANGE_TINT_PERCENT[index] / 100;
    }
    return Math.clamp(out, 0, 255);
  }

  private static boolean chance(final int hash, final int percent) {
    return Math.floorMod(hash, 100) < percent;
  }

  private static int hash(final int x, final int y) {
    int h = x * 374761393 + y * 668265263;
    h = (h ^ (h >>> 13)) * 1274126177;
    return h ^ (h >>> 16);
  }
}
