package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;

public final class LeashCollarTint {

  public static final int RED_SHIFT = 16;
  public static final int GREEN_SHIFT = 8;
  public static final int BLUE_SHIFT = 0;

  @Nullable private static DyeColor activeCollarColor;

  private LeashCollarTint() {}

  public static void capture(final Entity leashedEntity) {
    activeCollarColor =
        leashedEntity instanceof UnleashedDogEntity dog ? dog.getCollarColor() : null;
  }

  public static void clear() {
    activeCollarColor = null;
  }

  public static float channel(final float vanillaValue, final int shift) {
    final DyeColor color = activeCollarColor;
    if (color == null) {
      return vanillaValue;
    }
    return ((color.getEntityColor() >> shift) & 0xFF) / 255.0f;
  }
}
