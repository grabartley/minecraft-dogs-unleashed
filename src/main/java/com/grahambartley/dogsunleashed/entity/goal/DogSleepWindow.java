package com.grahambartley.dogsunleashed.entity.goal;

import net.minecraft.world.World;

public final class DogSleepWindow {

  public static final long DAY_LENGTH_TICKS = 24000;
  public static final long NIGHT_START_TICK = 13000;
  public static final long SUNRISE_TICK = 23000;

  private static final long MINECRAFT_HOUR_TICKS = 1000;

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

  public static boolean isWokenByWeather(final World world, final boolean undead) {
    return !undead && (world.isRaining() || world.isThundering());
  }

  public static boolean isWithinWindow(final long timeOfDay, final long start, final long end) {
    return start <= end
        ? timeOfDay >= start && timeOfDay < end
        : timeOfDay >= start || timeOfDay < end;
  }
}
