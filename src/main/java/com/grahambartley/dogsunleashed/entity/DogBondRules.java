package com.grahambartley.dogsunleashed.entity;

public final class DogBondRules {

  public static final int MAX_POINTS = DogBondStep.DEVOTED.minPoints();
  public static final long TICKS_PER_DAY = 24000L;
  public static final long NEVER = Long.MIN_VALUE;
  public static final double BLOCKS_PER_WALK_POINT = 20.0;
  public static final double MAX_WALK_STEP = 1.0;
  public static final double WALK_RANGE = 12.0;
  public static final double TELEPORT_DISTANCE = 12.0;
  public static final double FIGHT_RANGE = 16.0;
  public static final double NEAR_OWNER_RANGE = 16.0;
  public static final double GREET_DISTANCE = 8.0;
  public static final long ABSENCE_TICKS = 5 * 60 * 20;
  public static final double RESCUE_RANGE = 24.0;
  public static final double RESCUE_DISTANCE = 2.5;
  public static final int RESCUE_TICKS = 20 * 20;
  public static final double RESCUE_SPEED = 1.3;

  public enum Source {
    WALK(1, 20),
    PLAY(4, 20),
    FIGHT(5, 20),
    TREAT(4, 12),
    GROOM(10, 10);

    private final int points;
    private final int dailyCap;

    Source(final int points, final int dailyCap) {
      this.points = points;
      this.dailyCap = dailyCap;
    }

    public int points() {
      return this.points;
    }

    public int dailyCap() {
      return this.dailyCap;
    }
  }

  private DogBondRules() {}

  public static int clamp(final int points) {
    return Math.max(0, Math.min(MAX_POINTS, points));
  }

  public static long dayOf(final long timeOfDay) {
    return Math.floorDiv(timeOfDay, TICKS_PER_DAY);
  }

  public static int allowedGain(final Source source, final int amount, final int earnedToday) {
    return Math.max(0, Math.min(amount, source.dailyCap() - earnedToday));
  }

  public static boolean countsAsWalkStep(final double stepDistance) {
    return stepDistance > 0.0 && stepDistance <= MAX_WALK_STEP;
  }

  public static int walkPoints(final double walkedBlocks) {
    return (int) Math.floor(walkedBlocks / BLOCKS_PER_WALK_POINT);
  }

  public static int startingPoints(final boolean seniorParent) {
    return DogBondStep.STRANGER.raisedBy(DogAgingRules.startingBondSteps(seniorParent)).minPoints();
  }

  public static long absentFor(final long lastNearOwner, final long now) {
    return lastNearOwner == NEVER ? 0L : Math.max(0L, now - lastNearOwner);
  }

  public static boolean greetsOnReturn(final DogBondStep step, final long absentTicks) {
    return step.compareTo(DogBondStep.FRIEND) >= 0 && absentTicks >= ABSENCE_TICKS;
  }

  public static boolean fightCounts(final boolean hostile, final double distanceToOwner) {
    return hostile && distanceToOwner <= FIGHT_RANGE;
  }

  public static int rescueTicksLeft(final boolean ownerHurt, final int ticksLeft) {
    return ownerHurt ? RESCUE_TICKS : Math.max(0, ticksLeft - 1);
  }

  public static boolean answersRescue(final DogCommand command) {
    return command != DogCommand.SIT && !command.isAnchored();
  }

  public static boolean startsRescue(
      final DogBondStep step, final boolean ownerHurt, final double distanceToOwner) {
    return step == DogBondStep.DEVOTED && ownerHurt && distanceToOwner <= RESCUE_RANGE;
  }
}
