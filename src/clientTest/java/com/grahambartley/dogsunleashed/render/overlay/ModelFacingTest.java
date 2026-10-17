package com.grahambartley.dogsunleashed.render.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class ModelFacingTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(Direction.class)
  @DisplayName("a rig drawn without a turn keeps every face")
  void noTurnKeepsFaces(final Direction face) {
    assertEquals(face, ModelFacing.turnedBy(0.0f).apply(face));
    assertEquals(face, ModelFacing.turnedBy(360.0f).apply(face));
  }

  @ParameterizedTest(name = "{0} turned {1} is {2}")
  @CsvSource({
    "NORTH, 180, SOUTH",
    "SOUTH, 180, NORTH",
    "EAST, 180, WEST",
    "WEST, 180, EAST",
    "UP, 180, UP",
    "DOWN, 180, DOWN",
    "EAST, 90, NORTH",
    "NORTH, 90, WEST",
    "NORTH, -90, EAST",
    "NORTH, 270, EAST",
    "NORTH, 89, WEST"
  })
  @DisplayName("a turned rig maps each model face to the side it ends up on")
  void turnMapsFaces(final Direction face, final float degrees, final Direction expected) {
    assertEquals(expected, ModelFacing.turnedBy(degrees).apply(face));
  }
}
