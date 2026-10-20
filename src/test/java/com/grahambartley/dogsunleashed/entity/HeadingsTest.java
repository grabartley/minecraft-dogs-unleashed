package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.MathHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HeadingsTest {

  @ParameterizedTest(name = "({0}, {1}) toward ({2}, {3}) faces {4}")
  @CsvSource({
    "0, 0, 0, 5, 0",
    "0, 0, 5, 0, -90",
    "0, 0, -5, 0, 90",
    "0, 0, 0, -5, 180",
    "10.5, -3.5, 10.5, 20, 0",
    "2, 2, 7, 7, -45"
  })
  @DisplayName("the yaw points along Minecraft's compass, south 0, east -90, west 90")
  void facesTheTarget(
      final double fromX,
      final double fromZ,
      final double toX,
      final double toZ,
      final float expected) {
    assertEquals(
        0.0f, MathHelper.wrapDegrees(Headings.yawToward(fromX, fromZ, toX, toZ) - expected), 0.01f);
  }
}
