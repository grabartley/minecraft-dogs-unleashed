package com.grahambartley.dogsunleashed.render.portrait;

import java.util.List;

public final class PortraitComposer {

  public record Layer(int[] pixels, int width, int height) {}

  private PortraitComposer() {}

  public static int[] compose(final int width, final int height, final List<Layer> layers) {
    final int[] out = new int[width * height];
    for (final Layer layer : layers) {
      for (int y = 0; y < height; y++) {
        final int sourceY = y * layer.height() / height;
        for (int x = 0; x < width; x++) {
          final int sourceX = x * layer.width() / width;
          final int index = y * width + x;
          out[index] = over(layer.pixels()[sourceY * layer.width() + sourceX], out[index]);
        }
      }
    }
    return out;
  }

  public static int straighten(final int premultiplied) {
    final int alpha = premultiplied >>> 24;
    if (alpha == 0 || alpha == 0xFF) {
      return alpha == 0 ? 0 : premultiplied;
    }
    int out = alpha << 24;
    for (int shift = 0; shift < 24; shift += 8) {
      out |=
          Math.min(0xFF, (((premultiplied >>> shift) & 0xFF) * 0xFF + alpha / 2) / alpha) << shift;
    }
    return out;
  }

  static int over(final int top, final int bottom) {
    final int topAlpha = top >>> 24;
    if (topAlpha == 0xFF) {
      return top;
    }
    if (topAlpha == 0) {
      return bottom;
    }
    final int bottomAlpha = bottom >>> 24;
    final int bottomWeight = bottomAlpha * (0xFF - topAlpha) / 0xFF;
    final int alpha = topAlpha + bottomWeight;
    int out = alpha << 24;
    for (int shift = 0; shift < 24; shift += 8) {
      final int channel =
          (((top >>> shift) & 0xFF) * topAlpha + ((bottom >>> shift) & 0xFF) * bottomWeight)
              / alpha;
      out |= channel << shift;
    }
    return out;
  }
}
