package com.grahambartley.dogsunleashed.render.cross;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CoatPaletteTest {

  private static final CoatPalette TAN_WHITE_BLACK_PINK =
      new CoatPalette(List.of(0xC08040, 0xFFFFFF, 0x101010, 0xE0A0A0));

  @ParameterizedTest(name = "{0} is nearest {1}")
  @CsvSource({
    "0xC08040, 0",
    "0xB07838, 0",
    "0xF0F0F0, 1",
    "0x000000, 2",
    "0x303030, 2",
    "0xE8A8A8, 3"
  })
  @DisplayName("a pixel takes the palette colour nearest to it")
  void nearestColourWins(final String rgb, final int expected) {
    assertEquals(expected, TAN_WHITE_BLACK_PINK.nearestIndex(Integer.decode(rgb)));
  }

  @Test
  @DisplayName("a row of four colours reads in order, and the alpha byte is dropped")
  void rowReadsInOrder() {
    final Optional<CoatPalette> palette =
        CoatPalette.ofRow(new int[] {0xFF000001, 0x02, 0x03, 0x04, 0x05});

    assertEquals(List.of(1, 2, 3, 4), palette.orElseThrow().colours());
  }

  @ParameterizedTest(name = "{0} reads as {1}")
  @CsvSource({"0xFF4080C0, 0xC08040", "0x00FFFFFF, 0xFFFFFF", "0xFF000001, 0x010000"})
  @DisplayName("a palette read from a texture swaps its blue and red channels into place")
  void abgrRowReadsAsRgb(final String abgr, final String rgb) {
    final int colour = Integer.parseUnsignedInt(abgr.substring(2), 16);
    final CoatPalette palette =
        CoatPalette.ofAbgrRow(new int[] {colour, colour, colour, colour}).orElseThrow();

    assertEquals(Integer.decode(rgb), palette.colour(0));
  }

  @Test
  @DisplayName("a row shorter than four colours is not a palette")
  void shortRowIsNoPalette() {
    assertTrue(CoatPalette.ofRow(new int[] {1, 2, 3}).isEmpty());
    assertThrows(IllegalArgumentException.class, () -> new CoatPalette(List.of(1, 2)));
  }
}
