package com.grahambartley.dogsunleashed.render.rig;

import net.minecraft.util.math.Direction;

public record SlotRegion(String boneName, Direction face, int x, int y, int width, int height) {

  public boolean contains(final Texel texel) {
    return texel.x() >= this.x
        && texel.x() < this.x + this.width
        && texel.y() >= this.y
        && texel.y() < this.y + this.height;
  }
}
