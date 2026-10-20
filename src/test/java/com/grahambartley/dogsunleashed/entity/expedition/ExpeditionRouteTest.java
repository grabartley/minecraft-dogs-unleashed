package com.grahambartley.dogsunleashed.entity.expedition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import java.util.stream.LongStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ExpeditionRouteTest {

  static LongStream seeds() {
    return LongStream.range(0, 50);
  }

  @ParameterizedTest(name = "seed {0}")
  @MethodSource("seeds")
  @DisplayName("every stop stays within the expedition radius of where the dog set off")
  void staysWithinRadius(final long seed) {
    final ExpeditionRoute route = ExpeditionRoute.plan(500, -300, new Random(seed));
    final long limit = (long) (ExpeditionRoute.MAX_RADIUS + 1) * (ExpeditionRoute.MAX_RADIUS + 1);
    assertEquals(ExpeditionRoute.STOP_COUNT, route.stops().size());
    route.stops().forEach(stop -> assertTrue(route.squaredDistanceFromOrigin(stop) <= limit));
  }

  @ParameterizedTest(name = "seed {0}")
  @MethodSource("seeds")
  @DisplayName("the same seed plans the same route")
  void deterministicForSeed(final long seed) {
    assertEquals(
        ExpeditionRoute.plan(0, 0, new Random(seed)), ExpeditionRoute.plan(0, 0, new Random(seed)));
  }

  @Test
  @DisplayName("the furthest stop is the one farthest from the start")
  void furthestStop() {
    final ExpeditionRoute route =
        new ExpeditionRoute(
            10,
            10,
            List.of(
                new ExpeditionRoute.Stop(110, 10),
                new ExpeditionRoute.Stop(10, -390),
                new ExpeditionRoute.Stop(-190, 10)));
    assertEquals(new ExpeditionRoute.Stop(10, -390), route.furthest());
  }

  @Test
  @DisplayName("a route needs at least one stop")
  void rejectsEmptyRoute() {
    assertThrows(IllegalArgumentException.class, () -> new ExpeditionRoute(0, 0, List.of()));
  }
}
