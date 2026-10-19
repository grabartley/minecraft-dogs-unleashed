package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import java.util.EnumSet;
import java.util.Set;

public final class GroundNapRules {

  public static final long NAP_TICKS = 3 * 60 * 20;
  public static final long PERIOD_TICKS = DogSleepWindow.DAY_LENGTH_TICKS / 2;
  public static final long HURT_CALM_TICKS = 30 * 20;
  public static final double HOSTILE_RANGE = 16.0;
  public static final long NEVER = Long.MIN_VALUE;

  private static final Set<DogCommand> NAPPING_COMMANDS =
      EnumSet.of(DogCommand.FOLLOW, DogCommand.STAY, DogCommand.GUARD, DogCommand.FREE_ROAM);

  private GroundNapRules() {}

  public static long periodOf(final long timeOfDay) {
    return Math.floorDiv(timeOfDay, PERIOD_TICKS);
  }

  public static long dueOffset(final long seed) {
    return Math.floorMod(seed, PERIOD_TICKS - NAP_TICKS);
  }

  public static boolean isDue(final long timeOfDay, final long lastNappedPeriod, final long seed) {
    return periodOf(timeOfDay) != lastNappedPeriod
        && Math.floorMod(timeOfDay, PERIOD_TICKS) >= dueOffset(seed);
  }

  public static boolean isOver(final long now, final long startedAt) {
    return now - startedAt >= NAP_TICKS;
  }

  public static boolean isHurtRecently(final long now, final long lastHurtAt) {
    return lastHurtAt != NEVER && now - lastHurtAt < HURT_CALM_TICKS;
  }

  public static boolean allowsCommand(final DogCommand command) {
    return NAPPING_COMMANDS.contains(command);
  }

  public record Situation(
      boolean busy,
      boolean onSolidGround,
      boolean hasTarget,
      boolean hostileNearby,
      boolean hurtRecently,
      boolean ridden,
      boolean leashed,
      boolean playing) {

    public boolean isCalm() {
      return !this.hasTarget
          && !this.hostileNearby
          && !this.hurtRecently
          && !this.ridden
          && !this.leashed
          && !this.playing;
    }

    public boolean allowsNapToStart() {
      return this.isCalm() && !this.busy && this.onSolidGround;
    }
  }
}
