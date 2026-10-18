package com.grahambartley.dogsunleashed.render.layer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.render.UnrotatedGeoBounds;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import java.util.stream.Stream;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CarriedToyTest {

  private static final float PIXELS_PER_BLOCK = 16f;

  static Stream<Arguments> toysInDogsOfEverySize() {
    return Stream.of(CarriedToy.TENNIS_BALL, CarriedToy.FRISBEE, CarriedToy.STICK)
        .flatMap(
            toy ->
                Stream.of(
                    Arguments.of(toy, 1f, 1.3f),
                    Arguments.of(toy, 1f, 0.65f),
                    Arguments.of(toy, 1f, 1.7f),
                    Arguments.of(toy, 30f, 1.5f)));
  }

  @ParameterizedTest(name = "{0} at origin scale {1}, dog scale {2}")
  @MethodSource("toysInDogsOfEverySize")
  @DisplayName("a carried toy keeps its world size whatever the dog's render scale")
  void keepsWorldSizeWhateverTheDogScale(
      final CarriedToy toy, final float originScale, final float dogScale) {
    final Matrix4f origin = new Matrix4f().rotateY(0.7f).scale(originScale);
    final Matrix4f mouth =
        new Matrix4f(origin).scale(dogScale).rotateX(-0.4f).translate(0.1f, 0.6f, -0.3f);

    final float scale = toy.scaleIn(origin, mouth);

    final float drawnScale = new Matrix4f(mouth).scale(scale).getScale(new Vector3f()).x;
    final float worldPixels = toy.modelPixels() * drawnScale / originScale;
    assertEquals(toy.worldPixels(), worldPixels, 1e-4f);
  }

  static Stream<Arguments> toysAndTheirModels() {
    return Stream.of(
        Arguments.of(CarriedToy.TENNIS_BALL, "tennis_ball"),
        Arguments.of(CarriedToy.FRISBEE, "frisbee"),
        Arguments.of(CarriedToy.STICK, "stick"));
  }

  @ParameterizedTest(name = "{1}")
  @MethodSource("toysAndTheirModels")
  @DisplayName("each toy's model width and centre height match its shipped model")
  void modelMeasurementsMatchTheShippedModel(final CarriedToy toy, final String model) {
    final Box bounds = UnrotatedGeoBounds.of(ShippedRigs.model(model), 1f);

    assertEquals(toy.modelPixels(), (float) (bounds.getLengthX() * PIXELS_PER_BLOCK), 1e-3f);
    assertEquals(
        toy.centreHeight(), (float) ((bounds.minY + bounds.maxY) / 2 * PIXELS_PER_BLOCK), 1e-3f);
  }
}
