package com.grahambartley.dogsunleashed.entity.goal;

public final class TugRules {

  public static final double WIN_BALANCE = 1.0;
  public static final int MAX_TUG_TICKS = 30 * 20;
  public static final int KEEP_AWAY_TICKS = 10 * 20;
  public static final double MAX_DISTANCE = 6.0;
  public static final int RHYTHM_MIN_GAP_TICKS = 5;
  public static final int RHYTHM_MAX_GAP_TICKS = 12;
  public static final double RHYTHM_PULL = 0.18;
  public static final double RUSHED_PULL = 0.04;
  public static final double LATE_PULL = 0.08;
  public static final double BASE_DOG_PULL = 0.004;
  public static final double PULL_PER_HEALTH = 0.0004;
  public static final double SMALLEST_DOG_HEALTH = 10.0;
  public static final double PUPPY_FACTOR = 0.5;
  public static final double BOND_FACTOR_PER_STEP = 0.05;
  public static final double JITTER = 0.25;

  private TugRules() {}

  public enum Outcome {
    ONGOING,
    PLAYER_WINS,
    DOG_WINS
  }

  public record Readiness(
      boolean owner,
      boolean sneaking,
      boolean carrying,
      boolean playingFetch,
      boolean sleeping,
      boolean ridden,
      boolean onGround) {

    public boolean mayStart() {
      return this.owner
          && !this.sneaking
          && !this.carrying
          && !this.playingFetch
          && !this.sleeping
          && !this.ridden
          && this.onGround;
    }
  }

  public static double playerPull(final long ticksSinceLastPull) {
    if (ticksSinceLastPull < RHYTHM_MIN_GAP_TICKS) {
      return RUSHED_PULL;
    }
    return ticksSinceLastPull <= RHYTHM_MAX_GAP_TICKS ? RHYTHM_PULL : LATE_PULL;
  }

  public static double dogStrength(
      final double maxHealth, final boolean puppy, final int bondStep) {
    final double size =
        BASE_DOG_PULL + PULL_PER_HEALTH * Math.max(0.0, maxHealth - SMALLEST_DOG_HEALTH);
    final double bond = 1.0 + BOND_FACTOR_PER_STEP * Math.max(0, bondStep);
    return size * bond * (puppy ? PUPPY_FACTOR : 1.0);
  }

  public static double dogPull(final double strength, final double roll) {
    final double clampedRoll = Math.max(0.0, Math.min(1.0, roll));
    return strength * (1.0 - JITTER + 2.0 * JITTER * clampedRoll);
  }

  public static double balanceAfter(final double balance, final double change) {
    return Math.max(-WIN_BALANCE, Math.min(WIN_BALANCE, balance + change));
  }

  public static Outcome outcomeOf(final double balance) {
    if (balance >= WIN_BALANCE) {
      return Outcome.PLAYER_WINS;
    }
    return balance <= -WIN_BALANCE ? Outcome.DOG_WINS : Outcome.ONGOING;
  }

  public static boolean isAbandoned(final long tugTicks, final double playerDistance) {
    return tugTicks >= MAX_TUG_TICKS || playerDistance > MAX_DISTANCE;
  }

  public static float meterFill(final double balance) {
    final double clamped = Math.max(-WIN_BALANCE, Math.min(WIN_BALANCE, balance));
    return (float) ((clamped + WIN_BALANCE) / (2.0 * WIN_BALANCE));
  }
}
