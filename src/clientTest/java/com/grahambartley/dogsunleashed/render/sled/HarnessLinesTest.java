package com.grahambartley.dogsunleashed.render.sled;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HarnessLinesTest {

  @ParameterizedTest(name = "eye ({1} left, {2} up, {3} ahead) at yaw {0} -> ({4}, {5}, {6})")
  @CsvSource({
    "0, 0.3125, 0.125, 1.0625, 10.3125, 64.125, 21.0625",
    "90, 0.3125, 0.125, 1.0625, 8.9375, 64.125, 20.3125",
    "180, -0.3125, 0, 1, 10.3125, 64, 19",
    "-90, 0, 0, 1, 11, 64, 20"
  })
  @DisplayName("an eye turns with the sled, ahead toward the team and left on its left")
  void eyeInWorld(
      final float yaw,
      final float left,
      final float up,
      final float ahead,
      final double x,
      final double y,
      final double z) {
    final Vec3d eye =
        HarnessLines.eyeInWorld(new Vec3d(10.0, 64.0, 20.0), yaw, new Vector3f(left, up, ahead));
    assertEquals(x, eye.x, 1.0e-5);
    assertEquals(y, eye.y, 1.0e-5);
    assertEquals(z, eye.z, 1.0e-5);
  }
}
