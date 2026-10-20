package com.grahambartley.dogsunleashed.entity.expedition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.random.RandomGenerator;

public record ExpeditionRoute(int originX, int originZ, List<Stop> stops) {

  public static final int MAX_RADIUS = 1000;
  public static final int STOP_COUNT = 6;
  public static final int MIN_LEG = 80;
  public static final int MAX_LEG = 260;

  public record Stop(int x, int z) {}

  public ExpeditionRoute {
    if (stops.isEmpty()) {
      throw new IllegalArgumentException("A route needs at least one stop");
    }
    stops = List.copyOf(stops);
  }

  public static ExpeditionRoute plan(
      final int originX, final int originZ, final RandomGenerator random) {
    final List<Stop> stops = new ArrayList<>(STOP_COUNT);
    double heading = random.nextDouble() * Math.PI * 2.0;
    double x = 0.0;
    double z = 0.0;
    for (int i = 0; i < STOP_COUNT; i++) {
      heading += (random.nextDouble() - 0.5) * Math.PI / 2.0;
      final double leg = MIN_LEG + random.nextDouble() * (MAX_LEG - MIN_LEG);
      x += Math.cos(heading) * leg;
      z += Math.sin(heading) * leg;
      final double distance = Math.hypot(x, z);
      if (distance > MAX_RADIUS) {
        x *= MAX_RADIUS / distance;
        z *= MAX_RADIUS / distance;
      }
      stops.add(new Stop(originX + (int) Math.round(x), originZ + (int) Math.round(z)));
    }
    return new ExpeditionRoute(originX, originZ, stops);
  }

  public Stop furthest() {
    return this.stops.stream()
        .max(Comparator.comparingLong(this::squaredDistanceFromOrigin))
        .orElseThrow();
  }

  public long squaredDistanceFromOrigin(final Stop stop) {
    final long dx = stop.x() - this.originX;
    final long dz = stop.z() - this.originZ;
    return dx * dx + dz * dz;
  }
}
