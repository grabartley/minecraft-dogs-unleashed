package com.grahambartley.dogsunleashed.render.cross;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.render.cross.PaletteTint.Pull;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PaletteTintTest {

  private static final CoatPalette GOLD =
      new CoatPalette(List.of(0xC08040, 0xFFFFFF, 0x604020, 0x101010));
  private static final CoatPalette BLACK =
      new CoatPalette(List.of(0x202020, 0xF0F0F0, 0x000000, 0x101010));
  private static final CoatPalette WHITE =
      new CoatPalette(List.of(0xF0F0F0, 0xFFFFFF, 0xC0C0C0, 0x101010));

  static Stream<Arguments> pulls() {
    return Stream.of(
        Arguments.of("no pull leaves the pixel alone", 0xB07030, List.of(), 0xB07030),
        Arguments.of(
            "a full pull lands on the other palette's colour",
            0xC08040,
            List.of(new Pull(BLACK, 1.0f)),
            0x202020),
        Arguments.of(
            "a half pull lands halfway", 0xC08040, List.of(new Pull(BLACK, 0.5f)), 0x705030),
        Arguments.of(
            "shading darker than the base stays darker after the shift",
            0xA06020,
            List.of(new Pull(BLACK, 0.5f)),
            0x503010),
        Arguments.of(
            "two pulls add their shifts",
            0xC08040,
            List.of(new Pull(BLACK, 0.25f), new Pull(WHITE, 0.25f)),
            0xA48464),
        Arguments.of(
            "a shift past black clamps", 0x504030, List.of(new Pull(BLACK, 1.0f)), 0x000010));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("pulls")
  @DisplayName("each pixel moves by the weighted gap between its palette colour and the others'")
  void pixelMovesByWeightedGap(
      final String name, final int rgb, final List<Pull> pulls, final int expected) {
    assertEquals(expected, PaletteTint.tinted(rgb, GOLD, pulls), Integer.toHexString(expected));
  }

  @Test
  @DisplayName("only opaque pixels inside the coat change, and alpha is kept")
  void onlyOpaqueCoatPixelsChange() {
    final int goldAbgr = 0xFF4080C0;
    final int halfGoldAbgr = 0x804080C0;
    final int[] image = {goldAbgr, goldAbgr, 0x004080C0, halfGoldAbgr};

    final int[] tinted =
        PaletteTint.tintedAbgr(
            image,
            2,
            texel -> texel.x() == 0 || texel.y() == 1,
            GOLD,
            List.of(new Pull(BLACK, 1.0f)));

    assertArrayEquals(new int[] {0xFF202020, goldAbgr, 0x004080C0, 0x80202020}, tinted);
  }
}
