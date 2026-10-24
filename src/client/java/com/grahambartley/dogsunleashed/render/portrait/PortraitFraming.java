package com.grahambartley.dogsunleashed.render.portrait;

public final class PortraitFraming {

  private static final int SOLID_ALPHA = 0x80;
  private static final int OPAQUE_ALPHA = 0xFF;
  private static final double FILL_HEIGHT = 0.82;
  private static final double FILL_WIDTH = 0.9;
  private static final double FOOT_ROOM = 0.06;

  record Bounds(int left, int top, int right, int bottom) {
    int width() {
      return this.right - this.left;
    }

    int height() {
      return this.bottom - this.top;
    }
  }

  private PortraitFraming() {}

  public static int[] fit(
      final PortraitComposer.Layer dog, final int width, final int height, final int frame) {
    final int[] out = new int[width * height];
    final Bounds bounds = boundsOf(dog);
    if (bounds == null) {
      return out;
    }
    final double innerWidth = width - 2.0 * frame;
    final double innerHeight = height - 2.0 * frame;
    final double scale =
        Math.min(
            innerWidth * FILL_WIDTH / bounds.width(), innerHeight * FILL_HEIGHT / bounds.height());
    final double drawnWidth = bounds.width() * scale;
    final double drawnHeight = bounds.height() * scale;
    final double left = (width - drawnWidth) / 2.0;
    final double top = height - frame - innerHeight * FOOT_ROOM - drawnHeight;
    final int cellSide = (int) Math.ceil(1.0 / scale) + 2;
    final int[] colours = new int[cellSide * cellSide];
    final int[] votes = new int[colours.length];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        out[y * width + x] =
            dominant(
                dog,
                bounds.left() + (x - left) / scale,
                bounds.top() + (y - top) / scale,
                bounds.left() + (x + 1 - left) / scale,
                bounds.top() + (y + 1 - top) / scale,
                colours,
                votes);
      }
    }
    return out;
  }

  static Bounds boundsOf(final PortraitComposer.Layer layer) {
    int left = layer.width();
    int top = layer.height();
    int right = 0;
    int bottom = 0;
    for (int y = 0; y < layer.height(); y++) {
      for (int x = 0; x < layer.width(); x++) {
        if ((layer.pixels()[y * layer.width() + x] >>> 24) != 0) {
          left = Math.min(left, x);
          top = Math.min(top, y);
          right = Math.max(right, x + 1);
          bottom = Math.max(bottom, y + 1);
        }
      }
    }
    return right == 0 ? null : new Bounds(left, top, right, bottom);
  }

  private static int dominant(
      final PortraitComposer.Layer layer,
      final double fromX,
      final double fromY,
      final double toX,
      final double toY,
      final int[] colours,
      final int[] votes) {
    final int startX = Math.max(0, (int) Math.floor(fromX));
    final int startY = Math.max(0, (int) Math.floor(fromY));
    final int endX = Math.min(layer.width(), (int) Math.ceil(toX));
    final int endY = Math.min(layer.height(), (int) Math.ceil(toY));
    if (startX >= endX || startY >= endY) {
      return 0;
    }
    int distinct = 0;
    int samples = 0;
    int solid = 0;
    int best = 0;
    int bestVotes = 0;
    for (int y = startY; y < endY; y++) {
      for (int x = startX; x < endX; x++) {
        samples++;
        final int pixel = layer.pixels()[y * layer.width() + x];
        if ((pixel >>> 24) < SOLID_ALPHA) {
          continue;
        }
        solid++;
        if ((pixel >>> 24) < OPAQUE_ALPHA) {
          continue;
        }
        int slot = 0;
        while (slot < distinct && colours[slot] != pixel) {
          slot++;
        }
        if (slot == distinct) {
          colours[distinct] = pixel;
          votes[distinct++] = 0;
        }
        if (++votes[slot] > bestVotes) {
          best = pixel;
          bestVotes = votes[slot];
        }
      }
    }
    return solid * 2 < samples ? 0 : best;
  }
}
