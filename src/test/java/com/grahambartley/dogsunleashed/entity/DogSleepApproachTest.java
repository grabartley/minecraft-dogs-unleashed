package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogSleepApproachTest {

  private static final Vec3d POINT = new Vec3d(10.5, 64.0, -3.5);

  @ParameterizedTest(name = "dog at ({0}, {1}, {2}) arrived: {3}")
  @CsvSource({
    "10.5, 64.0, -3.5, true",
    "12.5, 64.0, -3.5, true",
    "10.5, 64.0, -5.5, true",
    "11.9, 64.0, -2.1, true",
    "12.6, 64.0, -3.5, false",
    "12.0, 64.0, -1.9, false",
    "10.5, 66.0, -3.5, true",
    "10.5, 66.1, -3.5, false",
    "10.5, 61.9, -3.5, false"
  })
  @DisplayName("a dog has arrived within two blocks of the approach point, sideways and up or down")
  void arrivesWithinTwoBlocks(
      final double x, final double y, final double z, final boolean arrived) {
    assertEquals(arrived, DogSleepApproach.hasArrived(new Vec3d(x, y, z), POINT));
  }
}
