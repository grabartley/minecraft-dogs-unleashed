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

  @ParameterizedTest(name = "{0} is closest to {1}")
  @CsvSource({
    "0xC08040, 0",
    "0xB07838, 0",
    "0xF0F0F0, 1",
    "0x000000, 2",
    "0x303030, 2",
    "0xE8A8A8, 3"
  })
  @DisplayName(
      "a pixel leans most on the palette colour nearest to it, and the leanings sum to one")
  void nearestColourLeadsTheCloseness(final String rgb, final int expected) {
    final float[] closeness = TAN_WHITE_BLACK_PINK.closeness(Integer.decode(rgb));

    float total = 0.0f;
    for (int index = 0; index < CoatPalette.SIZE; index++) {
      total += closeness[index];
      if (index != expected) {
        assertTrue(closeness[expected] > closeness[index], rgb + " leans on " + index);
      }
    }
    assertEquals(1.0f, total, 1.0e-5f);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"0xC08040", "0xFFFFFF", "0x101010", "0xE0A0A0"})
  @DisplayName("a pixel that is exactly a palette colour leans almost wholly on it")
  void exactColourLeansWhollyOnIt(final String rgb) {
    final int colour = Integer.decode(rgb);
    final int index = TAN_WHITE_BLACK_PINK.colours().indexOf(colour);

    assertTrue(TAN_WHITE_BLACK_PINK.closeness(colour)[index] > 0.99f);
  }

  @ParameterizedTest(name = "{0} to {1}")
  @CsvSource({"0xC08040, 0xC28242", "0xC28242, 0xC48444", "0x808080, 0x828282"})
  @DisplayName("a small change in a pixel makes only a small change in its leanings")
  void leaningsChangeSmoothly(final String from, final String to) {
    final float[] before = TAN_WHITE_BLACK_PINK.closeness(Integer.decode(from));
    final float[] after = TAN_WHITE_BLACK_PINK.closeness(Integer.decode(to));

    for (int index = 0; index < CoatPalette.SIZE; index++) {
      assertEquals(before[index], after[index], 0.2f, "entry " + index);
    }
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
