package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogBowlShapeTest {

  @ParameterizedTest(name = "along {0} the bowl spans {1} to {2} sixteenths")
  @CsvSource({"X, 2, 14", "Y, 0, 5", "Z, 2, 14"})
  @DisplayName("the bowl is twelve pixels across and five high, centred in its cell")
  void bowlMatchesTheModel(final Direction.Axis axis, final double min, final double max) {
    assertEquals(min / 16.0, DogBowlShape.SHAPE.getMin(axis), 1.0e-9);
    assertEquals(max / 16.0, DogBowlShape.SHAPE.getMax(axis), 1.0e-9);
  }
}
