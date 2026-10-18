package com.grahambartley.dogsunleashed.render.layer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Matrix4f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CarriedToySizeTest {

  private static final float TOLERANCE = 1e-5f;

  @Test
  @DisplayName("the frame scale is the matrix scale, whatever the rotation")
  void frameScaleReadsTheMatrix() {
    final Matrix4f frame = new Matrix4f().rotateY(1.1f).scale(2.5f);

    assertEquals(2.5f, CarriedToySize.frameScale(frame), TOLERANCE);
  }

  @Test
  @DisplayName("an adult carries its toy at the breed's own toy scale")
  void adultKeepsItsToyScale() {
    assertEquals(0.3f, CarriedToySize.scaleInFrame(0.3f, 1.3f, 1.3f), TOLERANCE);
  }

  @ParameterizedTest(name = "dog at {0}x its adult size")
  @CsvSource({"0.25", "0.5", "2.0", "4.0"})
  @DisplayName("the toy's world size moves with the square root of the dog's size")
  void worldSizeFollowsTheSquareRootOfDogSize(final float relative) {
    final float adultModel = 1.3f;
    final float frame = adultModel * relative;
    final float world = CarriedToySize.scaleInFrame(0.3f, adultModel, frame) * frame;

    assertEquals(0.3f * adultModel * (float) Math.sqrt(relative), world, TOLERANCE);
  }
}
