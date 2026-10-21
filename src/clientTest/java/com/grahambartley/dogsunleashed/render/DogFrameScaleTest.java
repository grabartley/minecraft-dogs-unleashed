package com.grahambartley.dogsunleashed.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed.RenderTransforms;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DogFrameScaleTest {

  private static final RenderTransforms TRANSFORMS = new RenderTransforms(1.2f, 0.6f, 0.0f, 0.9f);

  static Stream<Arguments> frames() {
    return Stream.of(
        Arguments.of("an adult draws at its adult scale", false, null, 1.0f, 1.2f),
        Arguments.of("a puppy draws at its baby scale", true, null, 1.0f, 0.6f),
        Arguments.of("a reshaped cross keeps its height", false, null, 0.9f, 1.08f),
        Arguments.of("a reshaped puppy cross keeps its height", true, null, 0.9f, 0.54f),
        Arguments.of("a growth stage scales the adult size", false, 2.0f, 0.8f, 1.92f),
        Arguments.of("a growth stage ignores the baby scale", true, 0.5f, 1.0f, 0.6f));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("frames")
  @DisplayName("the whole dog scales by age or growth stage, then by its reshaped height")
  void scalesByAgeOrStage(
      final String name,
      final boolean baby,
      final Float stage,
      final float heightScale,
      final float expected) {
    assertEquals(expected, DogFrameScale.of(TRANSFORMS, baby, stage, heightScale), 1.0e-5f);
  }
}
