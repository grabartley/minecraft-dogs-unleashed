package com.grahambartley.dogsunleashed.screen;

import java.util.function.ToIntFunction;

final class TextEllipsis {

  static final String ELLIPSIS = "...";

  private TextEllipsis() {}

  static String fit(final String text, final int maxWidth, final ToIntFunction<String> width) {
    if (width.applyAsInt(text) <= maxWidth) {
      return text;
    }
    final int budget = maxWidth - width.applyAsInt(ELLIPSIS);
    if (budget < 0) {
      return "";
    }
    int end = text.length();
    while (end > 0 && width.applyAsInt(text.substring(0, end)) > budget) {
      end = text.offsetByCodePoints(end, -1);
    }
    return text.substring(0, end).stripTrailing() + ELLIPSIS;
  }
}
