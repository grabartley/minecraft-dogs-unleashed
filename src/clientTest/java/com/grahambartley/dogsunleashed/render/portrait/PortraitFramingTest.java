package com.grahambartley.dogsunleashed.render.portrait;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PortraitFramingTest {

  private static final int DOG = 0xFF3366AA;
  private static final int FRAME_BORDER = 4;

  private static PortraitComposer.Layer canvasWithDog(
      final int size, final int left, final int top, final int width, final int height) {
    final int[] pixels = new int[size * size];
    for (int y = top; y < top + height; y++) {
      for (int x = left; x < left + width; x++) {
        pixels[y * size + x] = DOG;
      }
    }
    return new PortraitComposer.Layer(pixels, size, size);
  }

  private static PortraitFraming.Bounds drawn(
      final int[] pixels, final int width, final int height) {
    return PortraitFraming.boundsOf(new PortraitComposer.Layer(pixels, width, height));
  }

  @Test
  @DisplayName("an empty render frames nothing")
  void emptyRenderIsClear() {
    final PortraitComposer.Layer empty = new PortraitComposer.Layer(new int[64 * 64], 64, 64);
    assertNull(PortraitFraming.boundsOf(empty));
    assertTrue(
        Arrays.stream(PortraitFraming.fit(empty, 32, 32, FRAME_BORDER))
            .allMatch(pixel -> pixel == 0));
  }

  @Test
  @DisplayName("the bounds wrap exactly the drawn pixels")
  void boundsWrapTheDrawnPixels() {
    assertEquals(
        new PortraitFraming.Bounds(10, 20, 40, 70),
        PortraitFraming.boundsOf(canvasWithDog(128, 10, 20, 30, 50)));
  }

  @ParameterizedTest(name = "a {2}x{3} dog at ({0},{1}) in a {4}x{5} frame")
  @CsvSource({
    "0, 0, 40, 100, 32, 32",
    "90, 10, 30, 60, 32, 32",
    "20, 20, 100, 40, 32, 32",
    "50, 30, 40, 80, 64, 32",
    "5, 100, 20, 20, 64, 64"
  })
  @DisplayName(
      "any dog, wherever it was drawn, is scaled to fit inside the frame and stands near the bottom, centred")
  void dogFitsInsideTheFrame(
      final int left,
      final int top,
      final int width,
      final int height,
      final int frameWidth,
      final int frameHeight) {
    final int[] out =
        PortraitFraming.fit(
            canvasWithDog(128, left, top, width, height), frameWidth, frameHeight, FRAME_BORDER);
    final PortraitFraming.Bounds bounds = drawn(out, frameWidth, frameHeight);
    final int border = FRAME_BORDER;
    assertTrue(bounds.left() >= border && bounds.right() <= frameWidth - border, "inside sideways");
    assertTrue(
        bounds.top() >= border && bounds.bottom() <= frameHeight - border, "inside vertically");
    assertTrue(Math.abs(bounds.left() - (frameWidth - bounds.right())) <= 1, "centred: " + bounds);
    assertTrue(frameHeight - border - bounds.bottom() <= frameHeight / 16 + 1, "near the bottom");
    final boolean fillsHeight = bounds.height() >= (frameHeight - 2 * border) * 0.78;
    final boolean fillsWidth = bounds.width() >= (frameWidth - 2 * border) * 0.8;
    assertTrue(fillsHeight || fillsWidth, "fills the frame: " + bounds);
  }

  @Test
  @DisplayName("solid pixels stay solid and keep their colour after scaling")
  void solidPixelsKeepTheirColour() {
    final int[] out = PortraitFraming.fit(canvasWithDog(128, 30, 30, 40, 40), 32, 32, FRAME_BORDER);
    assertEquals(DOG, out[16 * 32 + 16]);
  }

  @ParameterizedTest(name = "{0}px stripes framed at {1}x{2}")
  @CsvSource({"1, 64, 64", "3, 128, 64", "7, 128, 128", "16, 128, 128", "2, 64, 64"})
  @DisplayName(
      "the painted dog only uses colours from the render with hard edges, never a blurred mix")
  void scalingNeverBlendsColours(final int stripe, final int frameWidth, final int frameHeight) {
    final int size = 256;
    final int other = 0xFFE0C090;
    final int[] pixels = new int[size * size];
    for (int y = 40; y < 200; y++) {
      for (int x = 30; x < 220; x++) {
        pixels[y * size + x] = (x / stripe + y / stripe) % 2 == 0 ? DOG : other;
      }
    }
    final int[] out =
        PortraitFraming.fit(
            new PortraitComposer.Layer(pixels, size, size), frameWidth, frameHeight, FRAME_BORDER);
    assertTrue(
        Arrays.stream(out).allMatch(pixel -> pixel == 0 || pixel == DOG || pixel == other),
        "only render colours or clear");
    assertTrue(Arrays.stream(out).anyMatch(pixel -> pixel == DOG), "the dog is painted");
  }

  @ParameterizedTest(name = "an edge alpha of {0}")
  @ValueSource(ints = {0x80, 0xC0, 0xFE})
  @DisplayName("a half-see-through edge never paints over the dog's solid colour beside it")
  void translucentEdgesLoseToSolidColour(final int edgeAlpha) {
    final int size = 128;
    final int edge = (edgeAlpha << 24) | 0x101010;
    final int[] pixels = new int[size * size];
    for (int y = 20; y < 110; y++) {
      for (int x = 20; x < 110; x++) {
        pixels[y * size + x] = x % 3 == 0 ? DOG : edge;
      }
    }
    final int[] out =
        PortraitFraming.fit(new PortraitComposer.Layer(pixels, size, size), 64, 64, FRAME_BORDER);
    assertTrue(Arrays.stream(out).allMatch(pixel -> pixel == 0 || pixel == DOG), "solid wins");
  }
}
