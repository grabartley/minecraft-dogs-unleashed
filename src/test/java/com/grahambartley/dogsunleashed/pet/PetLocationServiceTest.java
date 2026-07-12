package com.grahambartley.dogsunleashed.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PetLocationServiceTest {

  static Stream<Arguments> teleportDistanceCases() {
    return Stream.of(
        Arguments.of("no movement", Vec3d.ZERO, Vec3d.ZERO, false),
        Arguments.of("just under the threshold", Vec3d.ZERO, new Vec3d(15.9, 0, 0), false),
        Arguments.of("exactly the threshold", Vec3d.ZERO, new Vec3d(16, 0, 0), true),
        Arguments.of("far beyond the threshold", Vec3d.ZERO, new Vec3d(10_000, 0, 0), true),
        Arguments.of("vertical distance counts", Vec3d.ZERO, new Vec3d(0, 16, 0), true),
        Arguments.of("short diagonal", Vec3d.ZERO, new Vec3d(9, 0, 9), false),
        Arguments.of("long diagonal", Vec3d.ZERO, new Vec3d(12, 0, 12), true),
        Arguments.of(
            "offset origin under threshold",
            new Vec3d(100, 64, -100),
            new Vec3d(110, 64, -90),
            false),
        Arguments.of(
            "offset origin over threshold",
            new Vec3d(100, 64, -100),
            new Vec3d(120, 64, -120),
            true));
  }

  @ParameterizedTest(name = "{0} -> {3}")
  @MethodSource("teleportDistanceCases")
  @DisplayName("isLongDistanceTeleport fires at 16 blocks or more in any direction")
  void isLongDistanceTeleportFiresAtSixteenBlocksOrMore(
      final String label, final Vec3d from, final Vec3d to, final boolean expected) {
    assertEquals(expected, PetLocationService.isLongDistanceTeleport(from, to));
  }
}
