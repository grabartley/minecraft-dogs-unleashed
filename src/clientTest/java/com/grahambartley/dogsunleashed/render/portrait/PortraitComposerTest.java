package com.grahambartley.dogsunleashed.render.portrait;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PortraitComposerTest {

  private static final int SKY = 0xFFF0C080;
  private static final int GRASS = 0xFF30A050;
  private static final int WOOD = 0xFF204070;
  private static final int CLEAR = 0x00000000;

  @ParameterizedTest(name = "{0} over {1} is {2}")
  @CsvSource({
    "0xFF0000FF, 0xFF00FF00, 0xFF0000FF",
    "0x00FFFFFF, 0xFF00FF00, 0xFF00FF00",
    "0x800000FF, 0xFF00FF00, 0xFF007F80",
    "0x800000FF, 0x00000000, 0x800000FF",
    "0x00000000, 0x00000000, 0x00000000"
  })
  @DisplayName("a layer covers what is under it by its own alpha")
  void overBlendsByAlpha(final String top, final String bottom, final String expected) {
    assertEquals(hex(expected), PortraitComposer.over(hex(top), hex(bottom)));
  }

  @ParameterizedTest(name = "{0} straightens to {1}")
  @CsvSource({
    "0xFF102030, 0xFF102030",
    "0x00102030, 0x00000000",
    "0x80404040, 0x80808080",
    "0x40101010, 0x40404040",
    "0x10FFFFFF, 0x10FFFFFF"
  })
  @DisplayName(
      "a premultiplied render pixel gets its full colour back, so soft edges do not darken")
  void straightenUndoesPremultipliedAlpha(final String premultiplied, final String straight) {
    assertEquals(hex(straight), PortraitComposer.straighten(hex(premultiplied)));
  }

  @Test
  @DisplayName("a smaller layer is scaled up pixel for pixel, never smoothed")
  void smallerLayerScalesNearest() {
    final int[] out =
        PortraitComposer.compose(
            4, 2, List.of(new PortraitComposer.Layer(new int[] {SKY, GRASS}, 2, 1)));
    assertArrayEquals(new int[] {SKY, SKY, GRASS, GRASS, SKY, SKY, GRASS, GRASS}, out);
  }

  @Test
  @DisplayName(
      "background, dog and frame stack in order, with the dog's clear pixels showing the background")
  void layersStackInOrder() {
    final PortraitComposer.Layer background =
        new PortraitComposer.Layer(new int[] {SKY, SKY, GRASS, GRASS}, 2, 2);
    final PortraitComposer.Layer dog =
        new PortraitComposer.Layer(
            new int[] {
              CLEAR,
              CLEAR,
              CLEAR,
              CLEAR,
              CLEAR,
              0xFF0000FF,
              0xFF0000FF,
              CLEAR,
              CLEAR,
              0xFF0000FF,
              0xFF0000FF,
              CLEAR,
              CLEAR,
              CLEAR,
              CLEAR,
              CLEAR
            },
            4,
            4);
    final PortraitComposer.Layer frame =
        new PortraitComposer.Layer(
            new int[] {
              WOOD, WOOD, WOOD, WOOD,
              WOOD, CLEAR, CLEAR, WOOD,
              WOOD, CLEAR, CLEAR, WOOD,
              WOOD, WOOD, WOOD, WOOD
            },
            4,
            4);
    assertArrayEquals(
        new int[] {
          WOOD,
          WOOD,
          WOOD,
          WOOD,
          WOOD,
          0xFF0000FF,
          0xFF0000FF,
          WOOD,
          WOOD,
          0xFF0000FF,
          0xFF0000FF,
          WOOD,
          WOOD,
          WOOD,
          WOOD,
          WOOD
        },
        PortraitComposer.compose(4, 4, List.of(background, dog, frame)));
    assertArrayEquals(
        new int[] {SKY, SKY, SKY, SKY, GRASS, GRASS, GRASS, GRASS},
        PortraitComposer.compose(
            4,
            2,
            List.of(
                new PortraitComposer.Layer(new int[] {SKY, GRASS}, 1, 2),
                new PortraitComposer.Layer(new int[] {CLEAR}, 1, 1))));
  }

  private static int hex(final String value) {
    return Integer.parseUnsignedInt(value.substring(2), 16);
  }
}
