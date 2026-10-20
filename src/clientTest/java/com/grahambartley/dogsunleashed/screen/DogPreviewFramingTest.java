package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DogPreviewFramingTest {

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
    assertEquals(expected, DogPreviewFraming.renderSize(boxSize, portraitHeight));
  }

  static Stream<Arguments> relativeSizes() {
    return Stream.of(
        Arguments.of("a grown dog", false, 1.0f, false, 1.3f, 0.75f, 1.0f),
        Arguments.of(
            "a puppy is drawn at its share of the adult", false, 1.0f, true, 1.5f, 0.75f, 0.5f),
        Arguments.of("a staged dog follows its stage", true, 2.91f, false, 1.7f, 0.85f, 2.91f),
        Arguments.of("a staged pup ignores the baby scale", true, 0.5f, true, 1.7f, 0.85f, 0.5f));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("relativeSizes")
  @DisplayName("the framed height follows how big the dog is drawn against a grown dog")
  void scalesTheFrameWithTheDrawnDog(
      final String label,
      final boolean growsThroughStages,
      final float stageScale,
      final boolean baby,
      final float adultScale,
      final float babyScale,
      final float expected) {
    assertEquals(
        expected,
        DogPreviewFraming.sizeRelativeToAdult(
            growsThroughStages, stageScale, baby, adultScale, babyScale),
        0.0001f);
  }

  static Stream<Arguments> pivots() {
    return Stream.of(
        Arguments.of("model as tall as its hitbox", 1.1f, 1.1f, 1.0f, -0.12f),
        Arguments.of("short model lowers the pivot", 0.57f, 1.1f, 1.0f, -0.385f),
        Arguments.of("tall model raises the pivot", 1.31f, 1.1f, 1.0f, -0.015f),
        Arguments.of(
            "a half-size dog scales its depth below the origin", 0.5f, 0.5f, 0.5f, -0.06f));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("pivots")
  @DisplayName("the pivot sits at the middle of the drawn dog rather than its hitbox")
  void centresOnTheDrawnDog(
      final String label,
      final float portraitHeight,
      final float hitbox,
      final float relativeSize,
      final float expected) {
    assertEquals(
        expected, DogPreviewFraming.pivotOffset(portraitHeight, hitbox, relativeSize), 0.0001f);
  }
}
