package com.grahambartley.dogsunleashed.render.coat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UndeadTextureTransformTest {

  private static final int WHITE_FUR = 0xFFFFFF;
  private static final int GOLDEN_FUR = 0xFAC590;
  private static final int GREY_FUR = 0x9A9A9A;
  private static final int ATLAS = 128;

  static Stream<Arguments> furColours() {
    return Stream.of(
        Arguments.of("white fur", WHITE_FUR),
        Arguments.of("golden fur", GOLDEN_FUR),
        Arguments.of("grey fur", GREY_FUR));
  }

  @ParameterizedTest(name = "{0} decays the same way every time")
  @MethodSource("furColours")
  @DisplayName("the treatment is a pure function of colour and position")
  void decayIsDeterministic(final String label, final int rgb) {
    for (int y = 0; y < ATLAS; y += 17) {
      for (int x = 0; x < ATLAS; x += 13) {
        assertEquals(
            UndeadTextureTransform.decayed(rgb, x, y),
            UndeadTextureTransform.decayed(rgb, x, y),
            label + " at " + x + "," + y);
      }
    }
  }

  @ParameterizedTest(name = "{0} turns sickly green")
  @MethodSource("furColours")
  @DisplayName("decayed fur is green-dominant and darker than it was in life")
  void decayedFurIsGreenishAndDarker(final String label, final int rgb) {
    final int out = UndeadTextureTransform.decayed(rgb, 5, 5);
    final int r = (out >> 16) & 0xFF;
    final int g = (out >> 8) & 0xFF;
    final int b = out & 0xFF;
    assertTrue(g > r, label + " should lean green over red, got " + Integer.toHexString(out));
    assertTrue(g > b, label + " should lean green over blue, got " + Integer.toHexString(out));
    assertTrue(luma(out) < luma(rgb), label + " should darken");
  }

  @Test
  @DisplayName("black fur stays black rather than turning green")
  void blackFurStaysBlack() {
    assertEquals(0x000000, UndeadTextureTransform.decayed(0x000000, 5, 5));
  }

  @Test
  @DisplayName("mange patches rot a shade darker than healthy decayed fur")
  void mangePatchesAreDarker() {
    Integer mange = null;
    Integer healthy = null;
    for (int y = 0; y < ATLAS && (mange == null || healthy == null); y++) {
      for (int x = 0; x < ATLAS && (mange == null || healthy == null); x++) {
        final int out = UndeadTextureTransform.decayed(WHITE_FUR, x, y);
        if (UndeadTextureTransform.isMangePatch(x, y)) {
          mange = out;
        } else {
          healthy = out;
        }
      }
    }
    assertTrue(mange != null && healthy != null, "the atlas should hold both kinds of pixel");
    assertTrue(luma(mange) < luma(healthy), "mange should read darker than healthy decay");
  }

  @Test
  @DisplayName("tattering tears some of the silhouette but never all of it")
  void tatteringIsPartial() {
    int torn = 0;
    final int samples = ATLAS * ATLAS;
    for (int y = 0; y < ATLAS; y++) {
      for (int x = 0; x < ATLAS; x++) {
        if (UndeadTextureTransform.isTattered(x, y)) {
          torn++;
        }
      }
    }
    assertTrue(torn > samples / 5, "too little tatter: " + torn + "/" + samples);
    assertTrue(torn < samples * 3 / 5, "too much tatter: " + torn + "/" + samples);
  }

  @ParameterizedTest(name = "{0} stays within channel range")
  @MethodSource("furColours")
  @DisplayName("every decayed channel stays a valid colour byte")
  void decayedChannelsStayInRange(final String label, final int rgb) {
    for (int y = 0; y < ATLAS; y += 7) {
      for (int x = 0; x < ATLAS; x += 7) {
        final int out = UndeadTextureTransform.decayed(rgb, x, y);
        assertEquals(0, out >>> 24, label + " must pack to 24 bits");
      }
    }
  }

  private static int luma(final int rgb) {
    return (((rgb >> 16) & 0xFF) * 299 + ((rgb >> 8) & 0xFF) * 587 + (rgb & 0xFF) * 114) / 1000;
  }
}
