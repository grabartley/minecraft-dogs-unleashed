package com.grahambartley.dogsunleashed.render.cross;

import java.util.List;
import java.util.Optional;

public record CoatPalette(List<Integer> colours) {

  public static final int SIZE = 4;

  private static final float SOFTNESS = 64.0f;

  public CoatPalette {
    if (colours.size() != SIZE) {
      throw new IllegalArgumentException("A coat palette has " + SIZE + " colours");
    }
    colours = colours.stream().map(rgb -> rgb & 0xFFFFFF).toList();
  }

  public static Optional<CoatPalette> ofRow(final int[] rgb) {
    if (rgb.length < SIZE) {
      return Optional.empty();
    }
    return Optional.of(new CoatPalette(List.of(rgb[0], rgb[1], rgb[2], rgb[3])));
  }

  public static Optional<CoatPalette> ofAbgrRow(final int[] abgr) {
    final int[] rgb = new int[abgr.length];
    for (int index = 0; index < abgr.length; index++) {
      final int colour = abgr[index];
      rgb[index] = ((colour & 0xFF) << 16) | (colour & 0xFF00) | ((colour >> 16) & 0xFF);
    }
    return ofRow(rgb);
  }

  public int colour(final int index) {
    return this.colours.get(index);
  }

  public float[] closeness(final int rgb) {
    final float[] weights = new float[SIZE];
    float total = 0.0f;
    for (int index = 0; index < SIZE; index++) {
      final float spread = distanceSquared(rgb, this.colour(index)) + SOFTNESS;
      weights[index] = 1.0f / (spread * spread);
      total += weights[index];
    }
    for (int index = 0; index < SIZE; index++) {
      weights[index] /= total;
    }
    return weights;
  }

  private static int distanceSquared(final int a, final int b) {
    final int red = ((a >> 16) & 0xFF) - ((b >> 16) & 0xFF);
    final int green = ((a >> 8) & 0xFF) - ((b >> 8) & 0xFF);
    final int blue = (a & 0xFF) - (b & 0xFF);
    return red * red + green * green + blue * blue;
  }
}
