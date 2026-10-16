package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DogPortraitRendererTest {

  static Stream<Arguments> sizes() {
    return Stream.of(
        Arguments.of("a tall dog in the inspect box", 56, 1.31f, 36),
        Arguments.of("a short dog is drawn larger", 56, 0.57f, 83),
        Arguments.of("a larger box grows the dog", 112, 1.31f, 72),
        Arguments.of("a tiny box never renders at zero", 1, 2.0f, 1));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("sizes")
  @DisplayName("the dog's drawn height fills the same share of the box for every breed")
  void sizesTheDogToFillTheBox(
      final String label, final int boxSize, final float portraitHeight, final int expected) {
    assertEquals(expected, DogPortraitRenderer.portraitRenderSize(boxSize, portraitHeight));
  }

  static Stream<Arguments> pivots() {
    return Stream.of(
        Arguments.of("model as tall as its hitbox", 1.1f, 1.1f, -0.12f),
        Arguments.of("short model lowers the pivot", 0.57f, 1.1f, -0.385f),
        Arguments.of("tall model raises the pivot", 1.31f, 1.1f, -0.015f));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("pivots")
  @DisplayName("the pivot sits at the middle of the drawn dog rather than its hitbox")
  void centresOnTheDrawnDog(
      final String label, final float portraitHeight, final float hitbox, final float expected) {
    assertEquals(expected, DogPortraitRenderer.pivotOffset(portraitHeight, hitbox), 0.0001f);
  }
}
