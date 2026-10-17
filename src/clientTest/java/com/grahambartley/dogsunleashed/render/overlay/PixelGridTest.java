package com.grahambartley.dogsunleashed.render.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PixelGridTest {

  @Test
  @DisplayName("a blank grid is fully transparent")
  void blankIsTransparent() {
    final PixelGrid grid = PixelGrid.blank(3, 2);

    for (int y = 0; y < 2; y++) {
      for (int x = 0; x < 3; x++) {
        assertEquals(0, grid.colourAt(x, y));
      }
    }
  }

  @Test
  @DisplayName("a colour set at a texel reads back from that texel only")
  void setReadsBack() {
    final PixelGrid grid = PixelGrid.blank(3, 2);

    grid.set(2, 1, 0xFF112233);

    assertEquals(0xFF112233, grid.colourAt(2, 1));
    assertEquals(0, grid.colourAt(1, 1));
  }

  @ParameterizedTest(name = "({0}, {1})")
  @CsvSource({"-1, 0", "0, -1", "3, 0", "0, 2"})
  @DisplayName("a texel outside the grid is rejected")
  void outsideIsRejected(final int x, final int y) {
    final PixelGrid grid = PixelGrid.blank(3, 2);

    assertThrows(IndexOutOfBoundsException.class, () -> grid.colourAt(x, y));
    assertThrows(IndexOutOfBoundsException.class, () -> grid.set(x, y, 1));
  }

  @ParameterizedTest(name = "{0}x{1}")
  @CsvSource({"0, 1", "1, 0", "-2, 4"})
  @DisplayName("a grid without area cannot be made")
  void emptyGridIsRejected(final int width, final int height) {
    assertThrows(IllegalArgumentException.class, () -> PixelGrid.blank(width, height));
  }

  @Test
  @DisplayName("hasSize matches only the exact width and height")
  void hasSizeIsExact() {
    final PixelGrid grid = PixelGrid.blank(4, 5);

    assertTrue(grid.hasSize(4, 5));
    assertFalse(grid.hasSize(5, 4));
    assertEquals(4, grid.width());
    assertEquals(5, grid.height());
  }
}
