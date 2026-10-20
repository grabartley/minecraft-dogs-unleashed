package com.grahambartley.dogsunleashed.render.coat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.client.texture.NativeImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CoatImagesTest {

  @ParameterizedTest(name = "argb({0}, {1}, {2}, {3}) packs to {4}")
  @CsvSource({
    "255, 255, 0, 0, 0xFF0000FF",
    "255, 0, 255, 0, 0xFF00FF00",
    "255, 0, 0, 255, 0xFFFF0000",
    "128, 18, 52, 86, 0x80563412",
    "0, 0, 0, 0, 0x00000000"
  })
  @DisplayName("colours pack as NativeImage's ABGR and unpack to the same channels")
  void packsAndUnpacksAbgr(
      final int alpha, final int red, final int green, final int blue, final String packed) {
    final int abgr = Integer.parseUnsignedInt(packed.substring(2), 16);
    assertEquals(abgr, CoatImages.abgr(alpha, red, green, blue));
    assertEquals(alpha, CoatImages.alpha(abgr));
    assertEquals(red, CoatImages.red(abgr));
    assertEquals(green, CoatImages.green(abgr));
    assertEquals(blue, CoatImages.blue(abgr));
  }

  @ParameterizedTest(name = "{0}x{1}")
  @CsvSource({"1, 1", "3, 2", "2, 5"})
  @DisplayName("pixels copied out of an image build the same image back, row by row")
  void pixelsRoundTripThroughAnImage(final int width, final int height) {
    final int[] pixels = new int[width * height];
    for (int i = 0; i < pixels.length; i++) {
      pixels[i] = CoatImages.abgr(255 - i, i * 10, i * 20, i * 30);
    }
    try (NativeImage image = CoatImages.imageOf(pixels, width, height)) {
      assertEquals(width, image.getWidth());
      assertEquals(height, image.getHeight());
      assertEquals(pixels[width * height - 1], image.getColor(width - 1, height - 1));
      assertArrayEquals(pixels, CoatImages.pixelsOf(image));
    }
  }
}
