package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.goal.DogBowlVisitRules.Bowl;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogBowlVisitRulesTest {

  @ParameterizedTest(
      name = "{0} bowl, health {1}/{2}, undead {3}, regenerating {4} -> wants it {5}")
  @CsvSource({
    "FOOD, 10, 20, false, false, true",
    "FOOD, 10, 20, false, true, true",
    "FOOD, 20, 20, false, false, false",
    "FOOD, 10, 20, true, false, false",
    "WATER, 10, 20, false, false, true",
    "WATER, 10, 20, false, true, false",
    "WATER, 20, 20, false, false, false",
    "WATER, 10, 20, true, false, false"
  })
  @DisplayName("only a hurt living dog visits a bowl, and not for water it is already healing from")
  void wantsABowlOnlyWhenItHelps(
      final Bowl bowl,
      final float health,
      final float maxHealth,
      final boolean undead,
      final boolean regenerating,
      final boolean expected) {
    assertEquals(expected, DogBowlVisitRules.wants(bowl, health, maxHealth, undead, regenerating));
  }

  @ParameterizedTest(name = "a dog at ({0}, {1}, {2}) is beside a bowl at 0,64,0: {3}")
  @CsvSource({
    "0.5, 64, 0.5, true",
    "1.9, 64, 0.5, true",
    "2.1, 64, 0.5, false",
    "1.5, 64, 1.5, true",
    "0.5, 65, 0.5, true",
    "0.5, 65.5, 0.5, false",
    "0.5, 63, 0.5, true",
    "0.5, 62.5, 0.5, false"
  })
  @DisplayName("a dog eats once it is within reach of the bowl, on the same floor")
  void besideMeansWithinReachOnTheSameFloor(
      final double x, final double y, final double z, final boolean expected) {
    assertEquals(expected, DogBowlVisitRules.isBeside(0, 64, 0, x, y, z));
  }

  @ParameterizedTest(name = "on fire {0}, attacked {1}, freezing {2} -> safe {3}")
  @CsvSource({
    "false, false, false, true",
    "true, false, false, false",
    "false, true, false, false",
    "false, false, true, false"
  })
  @DisplayName("a dog in danger gets to safety before it goes to a bowl")
  void dangerComesBeforeABowl(
      final boolean onFire,
      final boolean attacked,
      final boolean freezing,
      final boolean expected) {
    assertEquals(expected, DogBowlVisitRules.isSafe(onFire, attacked, freezing));
  }

  @ParameterizedTest(name = "anchored {0}, bowl {1} blocks from the post -> within post {2}")
  @CsvSource({"false, 16, true", "true, 0, true", "true, 8, true", "true, 8.5, false"})
  @DisplayName("a staying or guarding dog only uses bowls close to its post")
  void anchoredDogsStayNearTheirPost(
      final boolean anchored, final double distance, final boolean expected) {
    assertEquals(expected, DogBowlVisitRules.isWithinPost(anchored, distance * distance));
  }

  @Test
  @DisplayName("a hurt dog drinks first for regeneration, then eats while it heals")
  void waterComesBeforeFood() {
    assertEquals(List.of(Bowl.WATER, Bowl.FOOD), List.of(Bowl.values()));
  }
}
