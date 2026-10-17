package com.grahambartley.dogsunleashed.render.overlay;

final class PixelGrids {

  private PixelGrids() {}

  static PixelGrid filled(final int width, final int height, final int colour) {
    final PixelGrid grid = PixelGrid.blank(width, height);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        grid.set(x, y, colour);
      }
    }
    return grid;
  }
}
