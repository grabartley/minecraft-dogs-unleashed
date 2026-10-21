package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ThreatStanceRulesTest {

  @ParameterizedTest(name = "{0}, offset {1}, target {2}, sitting {3} -> {4}")
  @CsvSource({
    "FOLLOW, 2.0, false, false, true",
    "HEEL, -2.5, false, false, true",
    "FOLLOW, 0.0, false, false, false",
    "FOLLOW, 2.0, true, false, false",
    "FOLLOW, 2.0, false, true, false",
    "STAY, 2.0, false, false, false",
    "SIT, -2.5, false, false, false",
    "GUARD, 2.0, false, false, false",
    "HUNT, 2.0, false, false, false",
    "FREE_ROAM, 2.0, false, false, false"
  })
  @DisplayName("only a free Follow or Heel dog with a stance takes one")
  void takesStance(
      final DogCommand command,
      final double offset,
      final boolean hasTarget,
      final boolean sitting,
      final boolean expected) {
    assertEquals(expected, ThreatStanceRules.takesStance(command, offset, hasTarget, sitting));
  }

  @ParameterizedTest(name = "threat at ({0}, {1}), offset {2} -> ({3}, {4})")
  @CsvSource({
    "10.0, 0.0, 2.0, 2.0, 0.0",
    "10.0, 0.0, -2.5, -2.5, 0.0",
    "0.0, -4.0, 2.0, 0.0, -2.0",
    "3.0, 4.0, 5.0, 3.0, 4.0",
    "0.0, 0.0, 2.0, 0.0, 0.0"
  })
  @DisplayName("brave dogs stand between owner and threat, timid ones behind the owner")
  void stancePoint(
      final double threatX,
      final double threatZ,
      final double offset,
      final double expectedX,
      final double expectedZ) {
    final Vec3d spot =
        ThreatStanceRules.stancePoint(Vec3d.ZERO, new Vec3d(threatX, 7.0, threatZ), offset);
    assertEquals(expectedX, spot.x, 1.0E-9);
    assertEquals(0.0, spot.y, 1.0E-9);
    assertEquals(expectedZ, spot.z, 1.0E-9);
  }

  @ParameterizedTest(name = "{0} squared vs range {1} -> {2}")
  @CsvSource({"63.9, 8.0, true", "64.0, 8.0, true", "64.1, 8.0, false"})
  void inRange(final double squared, final double range, final boolean expected) {
    assertEquals(expected, ThreatStanceRules.inRange(squared, range));
  }

  @ParameterizedTest(name = "tick {0} -> {1}")
  @CsvSource({"0, true", "1, false", "59, false", "60, true", "120, true"})
  @DisplayName("a timid dog cowers again every few seconds")
  void cowersNow(final int ticks, final boolean expected) {
    assertEquals(expected, ThreatStanceRules.cowersNow(ticks));
  }

  @ParameterizedTest(
      name = "monster {0}, alive {1}, calm {2}, friendly {3}, {4} squared, seen {5} -> {6}")
  @CsvSource({
    "true, true, false, false, 36.0, true, true",
    "true, true, false, false, 64.0, true, true",
    "true, true, false, false, 64.1, true, false",
    "false, true, false, false, 36.0, true, false",
    "true, false, false, false, 36.0, true, false",
    "true, true, true, false, 36.0, true, false",
    "true, true, false, true, 36.0, true, false",
    "true, true, false, false, 36.0, false, false"
  })
  @DisplayName(
      "only a hostile, angry or always-hostile mob the dog can see near its owner is a threat")
  void isThreat(
      final boolean monster,
      final boolean alive,
      final boolean calmNeutral,
      final boolean friendly,
      final double squaredDistance,
      final boolean visible,
      final boolean expected) {
    assertEquals(
        expected,
        ThreatStanceRules.isThreat(
            monster, alive, calmNeutral, friendly, squaredDistance, () -> visible));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"false", "true"})
  @DisplayName("line of sight is only checked once every cheaper test passes")
  void visibilityCheckedLast(final boolean monster) {
    final boolean[] looked = {false};
    ThreatStanceRules.isThreat(monster, true, false, false, 100.0, () -> looked[0] = true);
    assertEquals(false, looked[0]);
  }
}
