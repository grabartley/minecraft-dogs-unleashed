package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.math.MathHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MountTurningTest {

  @ParameterizedTest(name = "pace {0} turns at most {1} degrees a tick")
  @CsvSource({"0, 20", "0.5, 14", "1, 8", "-1, 20", "3, 8"})
  @DisplayName("a mount turns quickest standing and widest at a gallop")
  void turnRateFallsWithPace(final float pace, final float limit) {
    assertEquals(limit, MountTurning.maxTurn(pace), 1.0e-4f);
  }

  @ParameterizedTest(name = "from {0} toward {1} at pace {2} lands on {3}")
  @CsvSource({
    "0, 90, 0, 20",
    "0, -90, 1, -8",
    "0, 10, 0, 5",
    "350, 10, 0, 360",
    "10, 350, 0, 0",
    "0, 0.3, 1, 0.3",
  })
  @DisplayName("a mount eases toward where its rider looks, the short way round, within its limit")
  void easesTowardTheLook(final float yaw, final float look, final float pace, final float next) {
    assertEquals(next, MountTurning.turn(yaw, look, pace), 1.0e-4f);
  }

  @Test
  @DisplayName("a mount catches up with a held look and then stays on it")
  void catchesUpAndSettles() {
    float yaw = 0.0f;
    int ticks = 0;
    while (Math.abs(MathHelper.wrapDegrees(135.0f - yaw)) > 0.0f && ticks < 40) {
      yaw = MountTurning.turn(yaw, 135.0f, 0.0f);
      ticks++;
    }
    assertTrue(ticks < 40, "never settled on the look");
    assertEquals(135.0f, MountTurning.turn(yaw, 135.0f, 0.0f), 0.0f);
  }
}
