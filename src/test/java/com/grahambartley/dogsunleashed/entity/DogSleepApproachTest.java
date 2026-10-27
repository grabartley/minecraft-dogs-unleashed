package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.OptionalDouble;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogSleepApproachTest {

  @ParameterizedTest(name = "{0}: {4}")
  @CsvSource({
    "on the spot, 0.0, -1, true, SLEEP",
    "a block from the spot, 1.0, -1, true, SLEEP",
    "a path leads to the spot, 6.0, 0.0, true, WALK",
    "a partial path toward a far bed, 200.0, 40.0, true, WALK",
    "mid jump with no path, 6.0, -1, false, WALK",
    "no path at all, 6.0, -1, true, GIVE_UP",
    "beside a walled-off door, 1.5, 1.5, true, GIVE_UP",
    "the best path gets barely closer, 3.0, 2.6, true, GIVE_UP",
    "the best path gets half a block closer, 3.0, 2.5, true, WALK"
  })
  @DisplayName(
      "a dog sleeps at its spot, walks while it can get closer, and gives up when it cannot")
  void reachesTheSpotOrGivesUp(
      final String when,
      final double dogToSpot,
      final double pathEndToSpot,
      final boolean onGround,
      final DogSleepApproach.Step step) {
    final OptionalDouble end =
        pathEndToSpot < 0 ? OptionalDouble.empty() : OptionalDouble.of(pathEndToSpot);

    assertEquals(step, DogSleepApproach.next(dogToSpot, end, onGround), when);
  }
}
