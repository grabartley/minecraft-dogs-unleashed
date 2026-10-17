package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SlotRegionTest {

  private static final SlotRegion REGION = new SlotRegion("head", Direction.NORTH, 6, 46, 6, 6);

  @ParameterizedTest(name = "({0}, {1}) inside={2}")
  @CsvSource({
    "6, 46, true",
    "11, 51, true",
    "8, 48, true",
    "5, 46, false",
    "12, 46, false",
    "6, 45, false",
    "6, 52, false"
  })
  @DisplayName("a region holds the texels from its corner up to but not past its size")
  void containsTexelsWithinItsRectangle(final int x, final int y, final boolean inside) {
    assertEquals(inside, REGION.contains(new Texel(x, y)));
  }
}
