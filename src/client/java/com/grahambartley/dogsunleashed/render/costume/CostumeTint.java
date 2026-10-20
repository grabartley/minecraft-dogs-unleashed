package com.grahambartley.dogsunleashed.render.costume;

import java.util.OptionalInt;
import net.minecraft.util.math.ColorHelper;

public final class CostumeTint {

  static final int UNTINTED = 0xFFFFFFFF;

  private CostumeTint() {}

  public static int argb(final OptionalInt dyedRgb) {
    return dyedRgb.isPresent() ? ColorHelper.Argb.fullAlpha(dyedRgb.getAsInt()) : UNTINTED;
  }
}
