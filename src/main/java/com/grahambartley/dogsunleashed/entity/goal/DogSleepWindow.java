package com.grahambartley.dogsunleashed.entity.goal;

import net.minecraft.world.World;

/**
 * When a dog wants to be asleep. Adults keep the night, puppies turn in early and lie in, and an
 * undead dog runs the whole schedule backwards: it sleeps through the daylight that would burn it.
 */
public final class DogSleepWindow {

  public static final long DAY_LENGTH_TICKS = 24000;
  public static final long NIGHT_START_TICK = 13000;
  public static final long SUNRISE_TICK = 23000;

  private static final long MINECRAFT_HOUR_TICKS = 1000;

  // Puppies turn in two hours before adults and sleep in one hour past sunrise. Their wake time
  // lands on the 24000 day rollover, so the window is one contiguous block up to the new day.
  public static final long PUPPY_SLEEP_START_TICK = NIGHT_START_TICK - 2 * MINECRAFT_HOUR_TICKS;
  public static final long PUPPY_SLEEP_END_TICK = SUNRISE_TICK + MINECRAFT_HOUR_TICKS;

  private DogSleepWindow() {}

  public static boolean isSleepTime(
      final long worldTime, final boolean baby, final boolean undead) {
    final long timeOfDay = Math.floorMod(worldTime, DAY_LENGTH_TICKS);
    final boolean livingSleepTime =
        baby
            ? isWithinWindow(timeOfDay, PUPPY_SLEEP_START_TICK, PUPPY_SLEEP_END_TICK)
            : isWithinWindow(timeOfDay, NIGHT_START_TICK, SUNRISE_TICK);
    return undead ? !livingSleepTime : livingSleepTime;
  }

  /**
   * Weather rouses a living dog. An undead one sleeps through the day, and rain is the very thing
   * keeping it off fire, so a storm must not turn it out of bed.
   */
  public static boolean isWokenByWeather(final World world, final boolean undead) {
    return !undead && (world.isRaining() || world.isThundering());
  }

  /** Half-open window {@code [start, end)} on the 24000-tick clock, handling midnight wrap. */
  public static boolean isWithinWindow(final long timeOfDay, final long start, final long end) {
    return start <= end
        ? timeOfDay >= start && timeOfDay < end
        : timeOfDay >= start || timeOfDay < end;
  }
}
