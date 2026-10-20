package com.grahambartley.dogsunleashed.render.statue;

import com.grahambartley.dogsunleashed.block.StatueMaterial;
import java.util.List;
import java.util.function.ToIntFunction;

public final class StatueEngraving {

  public static final float FACE_WIDTH = 11.0f / 16.0f;
  public static final float FULL_SCALE = 2.5f / 16.0f / 7.0f;
  public static final float TWO_LINE_SCALE = 3.2f / 16.0f / 16.0f;
  public static final float MIN_SCALE = FULL_SCALE * 0.4f;
  public static final float LINE_SPACING = 9.0f;

  private static final float WRAP_BELOW = FULL_SCALE * 0.6f;
  private static final String ELLIPSIS = "...";

  public record Layout(List<String> lines, float scale) {}

  private StatueEngraving() {}

  public static Layout layout(final String name, final ToIntFunction<String> widthOf) {
    final int width = widthOf.applyAsInt(name);
    final int split = middleSpace(name);
    if (width <= 0 || FACE_WIDTH / width >= WRAP_BELOW || split < 0) {
      final String line = fit(name, widthOf);
      return new Layout(List.of(line), scaleFor(widthOf.applyAsInt(line), FULL_SCALE));
    }
    final String top = fit(name.substring(0, split), widthOf);
    final String bottom = fit(name.substring(split + 1), widthOf);
    final int widest = Math.max(widthOf.applyAsInt(top), widthOf.applyAsInt(bottom));
    return new Layout(List.of(top, bottom), scaleFor(widest, TWO_LINE_SCALE));
  }

  static float scaleFor(final int textWidth, final float largest) {
    if (textWidth <= 0) {
      return largest;
    }
    return Math.clamp(FACE_WIDTH / textWidth, Math.min(MIN_SCALE, largest), largest);
  }

  private static String fit(final String text, final ToIntFunction<String> widthOf) {
    if (widthOf.applyAsInt(text) * MIN_SCALE <= FACE_WIDTH) {
      return text;
    }
    String kept = text;
    while (!kept.isEmpty() && widthOf.applyAsInt(kept + ELLIPSIS) * MIN_SCALE > FACE_WIDTH) {
      kept = kept.substring(0, kept.length() - 1);
    }
    return kept.stripTrailing() + ELLIPSIS;
  }

  private static int middleSpace(final String name) {
    final int middle = name.length() / 2;
    int best = -1;
    for (int i = 0; i < name.length(); i++) {
      if (name.charAt(i) == ' ' && (best < 0 || Math.abs(i - middle) < Math.abs(best - middle))) {
        best = i;
      }
    }
    return best;
  }

  public static int colourOf(final StatueMaterial material) {
    return switch (material) {
      case STONE -> 0xFF2B2B2B;
      case GOLD -> 0xFF5C3D05;
      case COPPER -> 0xFF4A1F12;
      case EXPOSED_COPPER -> 0xFF3F2A20;
      case WEATHERED_COPPER -> 0xFF1F3A2C;
      case OXIDIZED_COPPER -> 0xFF143A33;
    };
  }
}
