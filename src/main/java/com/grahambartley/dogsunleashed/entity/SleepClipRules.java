package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.goal.DogSleepWindow;
import com.grahambartley.dogsunleashed.entity.goal.GroundNapRules;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public final class SleepClipRules {

  private static final Pattern VARIANT = Pattern.compile("sleep_([2-9]|[1-9][0-9]{1,8})");
  private static final long MIX_MULTIPLIER = 0x9E3779B97F4A7C15L;
  private static final long NAP_SALT = 0x5DEECE66DL << 20;

  private SleepClipRules() {}

  public static String clipFor(
      final List<String> sleepVariants,
      final UUID dogId,
      final long worldTime,
      final boolean baby,
      final boolean undead,
      final boolean napping) {
    return pick(sleepVariants, dogId, sessionSeed(worldTime, baby, undead, napping));
  }

  static List<String> variantsOf(final Collection<String> clipNames) {
    return clipNames.stream()
        .filter(name -> DogAnimationKeys.SLEEP.equals(name) || VARIANT.matcher(name).matches())
        .sorted(Comparator.comparingInt(SleepClipRules::variantNumber))
        .toList();
  }

  static long sessionSeed(
      final long worldTime, final boolean baby, final boolean undead, final boolean napping) {
    final long napPeriod = GroundNapRules.periodOf(worldTime);
    return napping
        ? napPeriod ^ NAP_SALT
        : DogSleepWindow.currentSleepWindowEnd(worldTime, baby, undead).orElse(napPeriod);
  }

  static String pick(final List<String> variants, final UUID dogId, final long seed) {
    if (variants.isEmpty()) {
      return DogAnimationKeys.SLEEP;
    }
    final long mixed =
        (dogId.getMostSignificantBits() ^ dogId.getLeastSignificantBits() ^ seed) * MIX_MULTIPLIER;
    return variants.get(Math.floorMod(mixed >>> 32, variants.size()));
  }

  private static int variantNumber(final String clipName) {
    return DogAnimationKeys.SLEEP.equals(clipName)
        ? 0
        : Integer.parseInt(clipName.substring(DogAnimationKeys.SLEEP.length() + 1));
  }
}
