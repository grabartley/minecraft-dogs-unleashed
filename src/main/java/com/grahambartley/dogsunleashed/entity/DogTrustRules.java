package com.grahambartley.dogsunleashed.entity;

import java.util.Map;
import net.minecraft.util.Identifier;

public final class DogTrustRules {

  public static final int MAX_TRUST = 100;
  public static final int FOLLOW_TRUST = 25;
  public static final int SPOOK_LOSS = 15;
  public static final int FEED_COOLDOWN_TICKS = 2 * 20;
  public static final int KILL_COOLDOWN_TICKS = 5 * 20;
  public static final int DECAY_INTERVAL_TICKS = 10 * 20;
  public static final double DECAY_DISTANCE = 24.0;
  public static final double KILL_RANGE = 16.0;
  public static final double GUARD_RANGE = 12.0;
  public static final double CROWDING_DISTANCE = 3.0;
  public static final double FOLLOW_RANGE = 24.0;
  public static final double FOLLOW_NEAREST = 6.0;
  public static final double FOLLOW_FARTHEST = 10.0;
  public static final int STRAY_FEED_MULTIPLIER = 2;
  private static final int MILESTONE_STEP = 25;
  private static final Map<Identifier, Food> FOODS =
      Map.ofEntries(
          Map.entry(Identifier.ofVanilla("mutton"), Food.RAW_MEAT),
          Map.entry(Identifier.ofVanilla("beef"), Food.RAW_MEAT),
          Map.entry(Identifier.ofVanilla("rabbit"), Food.RAW_MEAT),
          Map.entry(Identifier.ofVanilla("chicken"), Food.RAW_MEAT),
          Map.entry(Identifier.ofVanilla("porkchop"), Food.RAW_MEAT),
          Map.entry(Identifier.ofVanilla("bone"), Food.BONE),
          Map.entry(Identifier.ofVanilla("cooked_mutton"), Food.COOKED_MEAT),
          Map.entry(Identifier.ofVanilla("cooked_beef"), Food.COOKED_MEAT),
          Map.entry(Identifier.ofVanilla("cooked_rabbit"), Food.COOKED_MEAT),
          Map.entry(Identifier.ofVanilla("cooked_chicken"), Food.COOKED_MEAT),
          Map.entry(Identifier.ofVanilla("cooked_porkchop"), Food.COOKED_MEAT));

  public enum Food {
    RAW_MEAT(20),
    BONE(10),
    COOKED_MEAT(10),
    REFUSED(0);

    private final int gain;

    Food(final int gain) {
      this.gain = gain;
    }

    public int gain() {
      return this.gain;
    }
  }

  public enum Kill {
    SMALL_PREY(15),
    HOSTILE(20),
    OTHER(0);

    private final int gain;

    Kill(final int gain) {
      this.gain = gain;
    }

    public int gain() {
      return this.gain;
    }
  }

  private DogTrustRules() {}

  public static Food foodOf(final Identifier itemId) {
    return FOODS.getOrDefault(itemId, Food.REFUSED);
  }

  public static int feedGain(final Food food, final boolean stray) {
    return stray ? food.gain() * STRAY_FEED_MULTIPLIER : food.gain();
  }

  public static boolean isWary(final boolean stray) {
    return !stray;
  }

  public static int clamp(final int trust) {
    return Math.max(0, Math.min(MAX_TRUST, trust));
  }

  public static boolean cooledDown(final long now, final long lastTime, final int cooldownTicks) {
    return now - lastTime >= cooldownTicks;
  }

  public static int milestoneCrossed(final int before, final int after) {
    for (int milestone = MAX_TRUST - MILESTONE_STEP;
        milestone >= MILESTONE_STEP;
        milestone -= MILESTONE_STEP) {
      if (before < milestone && after >= milestone) {
        return milestone;
      }
    }
    return 0;
  }

  public static boolean isWon(final int trust) {
    return trust >= MAX_TRUST;
  }

  public static boolean follows(final int trust) {
    return trust >= FOLLOW_TRUST;
  }

  public static boolean provokes(
      final boolean sneaking,
      final boolean sprinting,
      final boolean holdingWeapon,
      final double distanceToPup) {
    return !sneaking && (sprinting || holdingWeapon || distanceToPup < CROWDING_DISTANCE);
  }
}
