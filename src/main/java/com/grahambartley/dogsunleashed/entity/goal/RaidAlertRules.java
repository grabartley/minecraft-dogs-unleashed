package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogCommand;

public final class RaidAlertRules {

  public static final double ALERT_RANGE = 48.0;
  public static final int SCAN_INTERVAL_TICKS = 20;
  public static final int REPEAT_TICKS = 4 * 20;

  private RaidAlertRules() {}

  public static boolean listens(
      final boolean tamed, final boolean asleep, final DogCommand command) {
    return tamed && !asleep && command != DogCommand.SIT && command != DogCommand.STAY;
  }

  public static boolean isIntruder(
      final boolean illager, final boolean inRaid, final boolean patrolling) {
    return illager || inRaid || patrolling;
  }

  public static boolean inRange(final double squaredDistance) {
    return squaredDistance <= ALERT_RANGE * ALERT_RANGE;
  }
}
