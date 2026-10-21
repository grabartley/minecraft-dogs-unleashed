package com.grahambartley.dogsunleashed.entity.sled;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SledFormationTest {

  private static final double EPSILON = 1.0e-6;

  @ParameterizedTest(name = "dog {0} of {1}, {2} wide -> {3} ahead, {4} left")
  @CsvSource({
    "0, 2, 0.6, 2.44, 0.45",
    "1, 2, 0.6, 2.44, -0.45",
    "2, 6, 0.6, 3.82, 0.45",
    "5, 6, 0.6, 5.2, -0.45",
    "0, 1, 0.6, 2.44, 0.0",
    "2, 3, 0.6, 3.82, 0.0",
    "1, 4, 2.0, 3.7, -1.1",
    "3, 4, 2.0, 7.6, -1.1"
  })
  @DisplayName(
      "dogs run in pairs clear of the sled and each other, a lone last dog down the middle")
  void slots(
      final int index,
      final int size,
      final double width,
      final double forward,
      final double left) {
    final SledFormation.Slot slot = SledFormation.slot(index, size, width);
    assertEquals(forward, slot.forward(), EPSILON);
    assertEquals(left, slot.left(), EPSILON);
  }

  @ParameterizedTest(name = "{0} dogs {1} wide rest {2} from the sled")
  @CsvSource({"0, 0.6, 0.0", "1, 0.6, 2.44", "2, 0.6, 2.44", "6, 0.6, 3.82", "4, 2.0, 5.65"})
  @DisplayName("the team's centre sits at the average distance of its slots")
  void restDistance(final int size, final double width, final double expected) {
    assertEquals(expected, SledFormation.restDistance(size, width), EPSILON);
  }

  @ParameterizedTest(name = "yaw {0} -> ({1}, {2})")
  @CsvSource({"0, 0, 1", "90, -1, 0", "180, 0, -1", "-90, 1, 0"})
  @DisplayName("forward follows vanilla yaw, with 0 facing south")
  void forward(final float yaw, final double x, final double z) {
    final Vec3d forward = SledFormation.forward(yaw);
    assertEquals(x, forward.x, EPSILON);
    assertEquals(z, forward.z, EPSILON);
  }

  @ParameterizedTest(name = "slot ({1} ahead, {2} left) at yaw {0} with lead {3} -> ({4}, {5})")
  @CsvSource({
    "0, 2.0, 0.5, 0.0, 0.5, 2.0",
    "0, 2.0, -0.5, 1.0, -0.5, 3.0",
    "90, 2.0, 0.5, 0.0, -2.0, 0.5",
    "180, 2.0, 0.5, 0.0, -0.5, -2.0"
  })
  @DisplayName("a slot turns with the heading, left staying on the team's left")
  void offset(
      final float yaw,
      final double ahead,
      final double left,
      final double lead,
      final double x,
      final double z) {
    final Vec3d offset = SledFormation.offset(new SledFormation.Slot(ahead, left), yaw, lead);
    assertEquals(x, offset.x, EPSILON);
    assertEquals(z, offset.z, EPSILON);
  }

  @ParameterizedTest(name = "{0} from a team resting {1} away at speed {2} -> {3}")
  @CsvSource({
    "3.0, 3.0, 0.3, 0.0",
    "3.1, 3.0, 0.3, 0.1",
    "4.0, 3.0, 0.3, 0.3",
    "2.0, 3.0, 0.3, 0.0",
    "5.0, 3.0, 0.0, 0.0"
  })
  @DisplayName("the sled only follows its team, never faster than the team's speed")
  void pull(final double distance, final double rest, final double speed, final double expected) {
    assertEquals(expected, SledFormation.pull(distance, rest, speed), EPSILON);
  }
}
