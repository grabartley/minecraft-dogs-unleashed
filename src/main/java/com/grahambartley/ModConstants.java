package com.grahambartley;

public class ModConstants {

  public static final int MINECRAFT_TICK_RATE = 20;

  public static final int BARK_COOLDOWN_TICKS = 6 * MINECRAFT_TICK_RATE;
  public static final int RANDOM_BARK_CHANCE = 7200;
  public static final int HOWL_COOLDOWN_TICKS = 30 * MINECRAFT_TICK_RATE;
  public static final int RANDOM_HOWL_CHANCE = 4000;
  public static final int HOWL_DURATION_TICKS = (int) (4.5f * MINECRAFT_TICK_RATE);
  public static final double HOWL_HEARING_RANGE_BLOCKS = 64.0;
  public static final double HOWL_HEARING_RANGE_SQUARED =
      HOWL_HEARING_RANGE_BLOCKS * HOWL_HEARING_RANGE_BLOCKS;
  public static final float LOW_HEALTH_THRESHOLD = 0.3f;
  public static final float BARK_PITCH = 1.0f;
  public static final float HOWL_PITCH = 1.0f;
  public static final int FULL_MOON_PHASE = 0;
}
