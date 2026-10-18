package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FamilyTreeCanvasRendererTest {

  @ParameterizedTest(name = "selected {0}, hovered {1}, alive {2} -> {3}")
  @CsvSource({
    "true, true, true, 0xFFFFDD66",
    "true, false, false, 0xFFFFDD66",
    "false, true, false, 0xFF9C8E52",
    "false, false, true, 0xFF666666",
    "false, false, false, 0xFF555555"
  })
  @DisplayName("card borders show selection over hover over alive or deceased")
  void borderColorPrecedence(
      final boolean selected, final boolean hovered, final boolean alive, final String expected) {
    assertEquals(
        Integer.parseUnsignedInt(expected.substring(2), 16),
        FamilyTreeCanvasRenderer.borderColor(selected, hovered, alive));
  }
}
