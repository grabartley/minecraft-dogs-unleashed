package com.grahambartley.dogsunleashed.render.coat;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
