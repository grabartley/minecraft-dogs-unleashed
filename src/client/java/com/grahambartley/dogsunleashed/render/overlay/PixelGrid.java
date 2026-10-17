package com.grahambartley.dogsunleashed.render.overlay;

public final class PixelGrid {

  private final int width;
  private final int height;
  private final int[] colours;

  private PixelGrid(final int width, final int height) {
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException("A pixel grid needs a size, got " + width + "x" + height);
    }
    this.width = width;
    this.height = height;
    this.colours = new int[width * height];
  }

  public static PixelGrid blank(final int width, final int height) {
    return new PixelGrid(width, height);
  }

  public int width() {
    return this.width;
  }

  public int height() {
    return this.height;
  }

  public boolean hasSize(final int width, final int height) {
    return this.width == width && this.height == height;
  }

  public int colourAt(final int x, final int y) {
    return this.colours[this.indexOf(x, y)];
  }

  public void set(final int x, final int y, final int colour) {
    this.colours[this.indexOf(x, y)] = colour;
  }

  private int indexOf(final int x, final int y) {
    if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
      throw new IndexOutOfBoundsException(
          "(" + x + ", " + y + ") is outside " + this.width + "x" + this.height);
    }
    return y * this.width + x;
  }
}
