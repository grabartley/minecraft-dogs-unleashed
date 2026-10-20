package com.grahambartley.dogsunleashed.entity;

public final class DogMudRules {

  public static final int TICKS_TO_GET_MUDDY = 40;
  public static final int TICKS_TO_SWIM_CLEAN = 200;
  public static final int TICKS_TO_WEAR_OFF = 3 * 24_000;
  public static final int SPATTER_CHANCE_PERCENT = 50;
  public static final double SPATTER_RANGE_BLOCKS = 2.5;

  public static final MudState CLEAN = new MudState(false, 0, 0, 0);

  private DogMudRules() {}

  public record MudState(boolean muddy, int exposureTicks, int muddyTicks, int soakTicks) {}

  public static MudState next(
      final MudState state,
      final boolean enabled,
      final boolean dirtying,
      final boolean inCleanWater) {
    if (!enabled) {
      return CLEAN;
    }
    if (dirtying) {
      final int exposure = Math.min(state.exposureTicks() + 1, TICKS_TO_GET_MUDDY);
      if (exposure >= TICKS_TO_GET_MUDDY) {
        return new MudState(true, exposure, 0, 0);
      }
      return new MudState(state.muddy(), exposure, nextMuddyTicks(state), 0);
    }
    if (!state.muddy()) {
      return CLEAN;
    }
    final int soak = inCleanWater ? state.soakTicks() + 1 : 0;
    final int muddyTicks = nextMuddyTicks(state);
    if (soak >= TICKS_TO_SWIM_CLEAN || muddyTicks >= TICKS_TO_WEAR_OFF) {
      return CLEAN;
    }
    return new MudState(true, 0, muddyTicks, soak);
  }

  public static boolean spatters(
      final boolean muddy, final double distanceToPlayer, final int rollPercent) {
    return muddy
        && distanceToPlayer <= SPATTER_RANGE_BLOCKS
        && rollPercent < SPATTER_CHANCE_PERCENT;
  }

  private static int nextMuddyTicks(final MudState state) {
    return state.muddy() ? state.muddyTicks() + 1 : 0;
  }
}
