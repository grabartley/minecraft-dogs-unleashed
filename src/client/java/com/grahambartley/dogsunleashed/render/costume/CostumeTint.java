package com.grahambartley.dogsunleashed.render.costume;

import net.minecraft.util.math.ColorHelper;

public final class CostumeTint {

  static final int UNTINTED = 0xFFFFFFFF;

  private CostumeTint() {}

  public static int argb(final boolean dyeable, final int rgb) {
    return dyeable ? ColorHelper.Argb.fullAlpha(rgb) : UNTINTED;
  }
}
