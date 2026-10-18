package com.grahambartley.dogsunleashed.render.cosmetic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BannerPrintTest {

  private static final int OPAQUE_WHITE = 0xFFFFFFFF;

  private record Grid(int width, int height, int[] pixels) implements BannerPrint.Image {
    static Grid filled(final int width, final int height, final int colour) {
      final int[] pixels = new int[width * height];
      java.util.Arrays.fill(pixels, colour);
      return new Grid(width, height, pixels);
    }

    static Grid frontLeftHalf(final int size, final int colour) {
      final int[] pixels = new int[size * size];
      for (int y = 0; y < size; y++) {
        for (int x = 0; x < 11; x++) {
          pixels[y * size + x] = colour;
        }
      }
      return new Grid(size, size, pixels);
    }

    @Override
    public int get(final int x, final int y) {
      return this.pixels[y * this.width + x];
    }
  }

  @ParameterizedTest(name = "texel {0} of {1} reads banner column {2}")
  @CsvSource({"0, 40, 1", "39, 40, 20", "20, 40, 11"})
  @DisplayName("the face spans the banner's front, one texel in from its left edge")
  void horizontalSpan(final int u, final int width, final int expected) {
    assertEquals(expected, BannerPrint.sourceX(u, width, 64));
  }

  @Test
  @DisplayName("a high resolution pattern is read at its own scale")
  void scalesWithPatternSize() {
    assertEquals(6, BannerPrint.sourceX(10, 40, 64));
    assertEquals(12, BannerPrint.sourceX(10, 40, 128));
  }

  @Test
  @DisplayName("only cloth that is there takes the print")
  void transparentClothStaysClear() {
    final Grid cloth = Grid.filled(4, 4, 0);
    cloth.pixels()[5] = OPAQUE_WHITE;
    final Grid pattern = Grid.filled(64, 64, 0xFF112233);

    final int[] printed =
        BannerPrint.print(cloth, pattern, List.of(new BannerPrint.Face(0, 0, 4, 4, false, false)));

    for (int i = 0; i < printed.length; i++) {
      assertEquals(i == 5 ? 0xFF112233 : 0, printed[i], "texel " + i);
    }
  }

  @Test
  @DisplayName("a flipped face prints the banner's left side on its right")
  void flipMirrors() {
    final Grid cloth = Grid.filled(8, 1, OPAQUE_WHITE);
    final Grid pattern = Grid.frontLeftHalf(64, OPAQUE_WHITE);

    final int[] straight =
        BannerPrint.print(cloth, pattern, List.of(new BannerPrint.Face(0, 0, 8, 1, false, false)));
    final int[] flipped =
        BannerPrint.print(cloth, pattern, List.of(new BannerPrint.Face(0, 0, 8, 1, true, false)));

    assertEquals(OPAQUE_WHITE, straight[0]);
    assertEquals(0, straight[7]);
    assertEquals(0, flipped[0]);
    assertEquals(OPAQUE_WHITE, flipped[7]);
  }

  @Test
  @DisplayName("the shipped bandana prints on its cloth front and back, not its hem")
  void shippedBandanaFaces() {
    final List<BannerPrint.Face> faces = BannerPrint.facesOf(ShippedRigs.model("bandana"), 128, 64);

    assertEquals(2, faces.size());
    for (final BannerPrint.Face face : faces) {
      assertEquals(40, face.width());
      assertEquals(28, face.height());
    }
    assertEquals(
        faces.get(0).flipU(), faces.get(1).flipU(), "both read the right way from outside");
  }
}
