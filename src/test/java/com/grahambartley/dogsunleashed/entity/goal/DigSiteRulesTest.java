package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DigSiteRulesTest {

  private static final BlockPos GROUND = new BlockPos(0, 64, 0);

  @ParameterizedTest(name = "dog at ({0}, {1}, {2}) -> {3}")
  @CsvSource({
    "0.5, 65, 0.5, true",
    "2.0, 65, 0.5, true",
    "2.1, 65, 0.5, false",
    "1.5, 65, 1.5, true",
    "1.6, 65, 1.6, false",
    "0.5, 66.5, 0.5, true",
    "0.5, 66.6, 0.5, false",
    "0.5, 63.4, 0.5, false",
    "-0.5, 65, 0.5, true"
  })
  @DisplayName("a dog is beside the dig site within a block and a half of its top centre")
  void besideWithinReach(final double x, final double y, final double z, final boolean expected) {
    assertEquals(expected, DigSiteRules.isBeside(GROUND, x, y, z));
  }
}
