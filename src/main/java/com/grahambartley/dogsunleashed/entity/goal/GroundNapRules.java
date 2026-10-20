package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import java.util.EnumSet;
import java.util.OptionalDouble;
import java.util.Set;

public final class GroundNapRules {

  public static final long NAP_TICKS = 3 * 60 * 20;
  public static final long SENIOR_NAP_TICKS = NAP_TICKS * 2;
  public static final long PERIOD_TICKS = DogSleepWindow.DAY_LENGTH_TICKS / 2;
  public static final long START_GRACE_TICKS = 2 * 60 * 20;
  public static final long HURT_CALM_TICKS = 30 * 20;
  public static final double HOSTILE_RANGE = 16.0;
  public static final long HOSTILE_CHECK_TICKS = 10;
  public static final double OWNER_NEARBY_RANGE = 10.0;
  public static final long NEVER = Long.MIN_VALUE;

  private static final Set<DogCommand> NAPPING_COMMANDS =
      EnumSet.of(DogCommand.FOLLOW, DogCommand.STAY, DogCommand.GUARD, DogCommand.FREE_ROAM);

  private GroundNapRules() {}

  public static long periodOf(final long timeOfDay) {
    return Math.floorDiv(timeOfDay, PERIOD_TICKS);
  }

  public static long napTicks(final boolean senior) {
    return senior ? SENIOR_NAP_TICKS : NAP_TICKS;
  }

  public static boolean napsOnGround(final boolean sleepsInBeds, final boolean senior) {
    return !sleepsInBeds || senior;
  }

  public static long dueOffset(final long seed, final long napTicks) {
    return Math.floorMod(seed, PERIOD_TICKS - napTicks - START_GRACE_TICKS);
  }

  public static boolean isDue(
      final long timeOfDay, final long lastNappedPeriod, final long seed, final long napTicks) {
    final long intoPeriod = Math.floorMod(timeOfDay, PERIOD_TICKS);
    return periodOf(timeOfDay) != lastNappedPeriod
        && intoPeriod >= dueOffset(seed, napTicks)
        && intoPeriod <= PERIOD_TICKS - napTicks;
  }

  public static boolean isOver(final long now, final long startedAt, final long napTicks) {
    return now - startedAt >= napTicks;
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
      boolean playing,
      boolean ownerAway) {

    public boolean isCalm() {
      return !this.hasTarget
          && !this.hostileNearby
          && !this.hurtRecently
          && !this.ridden
          && !this.leashed
          && !this.playing
          && !this.ownerAway;
    }

    public boolean allowsNapToStart() {
      return this.isCalm() && !this.busy && this.onSolidGround;
    }
  }

  public static boolean hostileCheckDue(final long now, final long checkedAt) {
    return checkedAt == NEVER || now - checkedAt >= HOSTILE_CHECK_TICKS;
  }

  public static boolean isOwnerAway(
      final boolean followsOwner, final OptionalDouble ownerSquaredDistance) {
    return followsOwner
        && ownerSquaredDistance.isPresent()
        && ownerSquaredDistance.getAsDouble() > OWNER_NEARBY_RANGE * OWNER_NEARBY_RANGE;
  }
}
