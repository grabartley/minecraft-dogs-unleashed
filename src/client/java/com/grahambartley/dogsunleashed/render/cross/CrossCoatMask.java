package com.grahambartley.dogsunleashed.render.cross;

import com.grahambartley.dogsunleashed.render.rig.SlotRegion;
import com.grahambartley.dogsunleashed.render.rig.Texel;
import java.util.List;

public final class CrossCoatMask {

  private final int width;
  private final int height;
  private final boolean[] coat;

  private CrossCoatMask(final int width, final int height) {
    this.width = width;
    this.height = height;
    this.coat = new boolean[width * height];
  }

  public static CrossCoatMask of(
      final int width,
      final int height,
      final List<SlotRegion> regions,
      final List<Texel> keepColour) {
    final CrossCoatMask mask = new CrossCoatMask(width, height);
    for (final SlotRegion region : regions) {
      for (int y = Math.max(0, region.y());
          y < Math.min(height, region.y() + region.height());
          y++) {
        for (int x = Math.max(0, region.x());
            x < Math.min(width, region.x() + region.width());
            x++) {
          mask.coat[y * width + x] = true;
        }
      }
    }
    for (final Texel texel : keepColour) {
      if (mask.inside(texel)) {
        mask.coat[texel.y() * width + texel.x()] = false;
      }
    }
    return mask;
  }

  public boolean tints(final Texel texel) {
    return this.inside(texel) && this.coat[texel.y() * this.width + texel.x()];
  }

  private boolean inside(final Texel texel) {
    return texel.x() >= 0 && texel.y() >= 0 && texel.x() < this.width && texel.y() < this.height;
  }
}
