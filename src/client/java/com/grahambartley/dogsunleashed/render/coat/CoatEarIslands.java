package com.grahambartley.dogsunleashed.render.coat;

import com.grahambartley.dogsunleashed.entity.rig.DogEarShape;
import java.util.List;
import java.util.Map;

/**
 * Where each ear shape lives on the shared 128x128 atlas. Every shape has its own reserved patch so
 * the variants can coexist in one model, which is what lets a dog wear one ancestor's ears on
 * another ancestor's coat.
 */
public final class CoatEarIslands {

  public record Island(int x, int y, int width, int height) {

    public boolean contains(final int px, final int py) {
      return px >= this.x && px < this.x + this.width && py >= this.y && py < this.y + this.height;
    }
  }

  private static final Map<DogEarShape, List<Island>> ISLANDS =
      Map.of(
          DogEarShape.GOLDEN_RETRIEVER,
          List.of(new Island(65, 0, 6, 4), new Island(72, 0, 4, 2)),
          DogEarShape.BEAGLE,
          List.of(new Island(77, 0, 10, 7)));

  private CoatEarIslands() {}

  public static List<Island> of(final DogEarShape shape) {
    return ISLANDS.getOrDefault(shape, List.of());
  }

  public static boolean contains(final List<Island> islands, final int x, final int y) {
    for (final Island island : islands) {
      if (island.contains(x, y)) {
        return true;
      }
    }
    return false;
  }
}
