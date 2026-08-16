package com.grahambartley.dogsunleashed.render.coat;

import java.util.ArrayList;
import java.util.List;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;

/**
 * Works out which patch of the atlas a variant bone paints from, by reading the bone's own baked
 * UVs rather than keeping a copy of the art's coordinates.
 *
 * <p>Variant bones exist so a dog can wear one ancestor's part on another ancestor's coat, which
 * means the compositor has to repaint exactly that part's texels from the donor's layers. Deriving
 * the region from the model keeps the rig the only place those coordinates are written down: move a
 * cube in Blockbench and the compositor follows, and a new variant needs no code at all.
 */
public final class VariantIslands {

  public record Island(int x, int y, int width, int height) {

    public boolean contains(final int px, final int py) {
      return px >= this.x && px < this.x + this.width && py >= this.y && py < this.y + this.height;
    }

    boolean touches(final Island other) {
      return this.x <= other.x + other.width
          && other.x <= this.x + this.width
          && this.y <= other.y + other.height
          && other.y <= this.y + this.height;
    }

    Island merge(final Island other) {
      final int x0 = Math.min(this.x, other.x);
      final int y0 = Math.min(this.y, other.y);
      final int x1 = Math.max(this.x + this.width, other.x + other.width);
      final int y1 = Math.max(this.y + this.height, other.y + other.height);
      return new Island(x0, y0, x1 - x0, y1 - y0);
    }
  }

  private VariantIslands() {}

  public static List<Island> of(final GeoBone bone, final int atlasWidth, final int atlasHeight) {
    return bone == null ? List.of() : ofCubes(bone.getCubes(), atlasWidth, atlasHeight);
  }

  /**
   * One island per quad, then anything overlapping or touching is merged. Box UV lays a cube's six
   * faces out side by side, so they collapse back into the single island the artist sees; faces
   * scattered individually stay separate rather than dragging in the texels between them.
   */
  public static List<Island> ofCubes(
      final List<GeoCube> cubes, final int atlasWidth, final int atlasHeight) {
    final List<Island> islands = new ArrayList<>();
    for (final GeoCube cube : cubes) {
      for (final GeoQuad quad : cube.quads()) {
        final Island island = islandOf(quad, atlasWidth, atlasHeight);
        if (island != null) {
          islands.add(island);
        }
      }
    }
    return merged(islands);
  }

  private static Island islandOf(final GeoQuad quad, final int atlasWidth, final int atlasHeight) {
    if (quad == null || quad.vertices().length == 0) {
      return null;
    }
    float minU = Float.MAX_VALUE;
    float maxU = -Float.MAX_VALUE;
    float minV = Float.MAX_VALUE;
    float maxV = -Float.MAX_VALUE;
    for (final GeoVertex vertex : quad.vertices()) {
      minU = Math.min(minU, vertex.texU());
      maxU = Math.max(maxU, vertex.texU());
      minV = Math.min(minV, vertex.texV());
      maxV = Math.max(maxV, vertex.texV());
    }
    // baked UVs are normalised, so scale back into texels and round outward to cover partials
    final int x = (int) Math.floor(minU * atlasWidth);
    final int y = (int) Math.floor(minV * atlasHeight);
    final int width = (int) Math.ceil(maxU * atlasWidth) - x;
    final int height = (int) Math.ceil(maxV * atlasHeight) - y;
    return width <= 0 || height <= 0 ? null : new Island(x, y, width, height);
  }

  private static List<Island> merged(final List<Island> islands) {
    final List<Island> result = new ArrayList<>(islands);
    boolean mergedAny = true;
    while (mergedAny) {
      mergedAny = false;
      outer:
      for (int i = 0; i < result.size(); i++) {
        for (int j = i + 1; j < result.size(); j++) {
          if (result.get(i).touches(result.get(j))) {
            final Island combined = result.get(i).merge(result.get(j));
            result.remove(j);
            result.set(i, combined);
            mergedAny = true;
            break outer;
          }
        }
      }
    }
    return List.copyOf(result);
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
