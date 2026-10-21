package com.grahambartley.dogsunleashed.entity.goal;

import java.util.function.IntUnaryOperator;

public final class PlayChaseRules {

  public static final double START_RANGE = 10.0;
  public static final double PLAY_AREA_RADIUS = 12.0;
  public static final double TAG_RANGE = 1.5;
  public static final double FLEE_DISTANCE = 6.0;
  public static final int CHECK_INTERVAL_TICKS = 100;
  public static final int START_ONE_IN = 12;
  public static final int PLAYFUL_START_ONE_IN = 4;
  public static final int MIN_GAME_TICKS = 10 * 20;
  public static final int MAX_GAME_TICKS = 20 * 20;
  public static final int BOW_TICKS = 30;
  public static final int MAX_CHASE_LEG_TICKS = 5 * 20;
  public static final long REST_TICKS = 60 * 20;

  private PlayChaseRules() {}

  public enum Playground {
    NONE,
    FREE_ROAM,
    WILD_PACK
  }

  public record Readiness(
      Playground playground,
      boolean idle,
      boolean onGround,
      boolean carrying,
      boolean sleepTime,
      boolean hurtRecently,
      boolean inDanger) {

    public boolean mayPlay() {
      return this.playground != Playground.NONE
          && this.idle
          && this.onGround
          && !this.carrying
          && !this.sleepTime
          && !this.hurtRecently
          && !this.inDanger;
    }
  }

  public record Spot(double x, double z) {

    double distanceTo(final Spot other) {
      return Math.hypot(this.x - other.x, this.z - other.z);
    }
  }

  public static Spot midpoint(final Spot a, final Spot b) {
    return new Spot((a.x + b.x) / 2, (a.z + b.z) / 2);
  }

  public static Playground playgroundOf(
      final boolean tamed, final boolean freeRoaming, final boolean wildPackPup) {
    if (tamed) {
      return freeRoaming ? Playground.FREE_ROAM : Playground.NONE;
    }
    return wildPackPup ? Playground.WILD_PACK : Playground.NONE;
  }

  public static boolean arePlaymates(
      final Playground a, final Playground b, final boolean samePack) {
    return a != Playground.NONE && a == b && (a != Playground.WILD_PACK || samePack);
  }

  public static int startOneIn(final boolean playful) {
    return playful ? PLAYFUL_START_ONE_IN : START_ONE_IN;
  }

  public static boolean startsGame(final boolean playful, final IntUnaryOperator nextInt) {
    return nextInt.applyAsInt(startOneIn(playful)) == 0;
  }

  public static int gameTicks(final IntUnaryOperator nextInt) {
    return MIN_GAME_TICKS + nextInt.applyAsInt(MAX_GAME_TICKS - MIN_GAME_TICKS + 1);
  }

  public static boolean swapsRoles(final long legTicks, final double distance) {
    return distance <= TAG_RANGE || legTicks >= MAX_CHASE_LEG_TICKS;
  }

  public static boolean isInPlayArea(final Spot centre, final Spot spot) {
    return centre.distanceTo(spot) <= PLAY_AREA_RADIUS;
  }

  public static Spot fleeTarget(final Spot centre, final Spot runner, final Spot chaser) {
    double dx = runner.x - chaser.x;
    double dz = runner.z - chaser.z;
    double length = Math.hypot(dx, dz);
    if (length < 1.0E-6) {
      dx = runner.x - centre.x;
      dz = runner.z - centre.z;
      length = Math.hypot(dx, dz);
    }
    if (length < 1.0E-6) {
      dx = 1.0;
      dz = 0.0;
      length = 1.0;
    }
    final Spot away =
        new Spot(runner.x + dx / length * FLEE_DISTANCE, runner.z + dz / length * FLEE_DISTANCE);
    final double fromCentre = centre.distanceTo(away);
    final double limit = PLAY_AREA_RADIUS - TAG_RANGE;
    if (fromCentre <= limit) {
      return away;
    }
    final double scale = limit / fromCentre;
    return new Spot(centre.x + (away.x - centre.x) * scale, centre.z + (away.z - centre.z) * scale);
  }
}
