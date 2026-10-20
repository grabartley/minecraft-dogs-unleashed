package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.goal.PointedFetchRules.Step;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PointedFetchRulesTest {

  @ParameterizedTest(name = "tick {0}, cannot pick up={1}, distance² {2} -> {3}")
  @CsvSource({
    "1, false, 100.0, MOVE",
    "10, false, 2.25, PICK_UP",
    "10, false, 2.26, MOVE",
    "10, true, 0.5, MOVE",
    "600, false, 0.5, PICK_UP",
    "601, false, 0.5, GIVE_UP",
    "601, true, 100.0, GIVE_UP"
  })
  @DisplayName("a pointed fetch picks up in reach, walks otherwise, and gives up after 30 seconds")
  void stepsTheFetch(
      final int fetchTicks,
      final boolean cannotPickup,
      final double squaredDistance,
      final Step expected) {
    assertEquals(expected, PointedFetchRules.step(fetchTicks, cannotPickup, squaredDistance));
  }
}
