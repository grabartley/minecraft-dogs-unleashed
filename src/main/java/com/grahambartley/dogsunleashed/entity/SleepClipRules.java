package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.goal.DogSleepWindow;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public final class SleepClipRules {

  private static final Pattern VARIANT = Pattern.compile("sleep_([1-9][0-9]{0,8})");
  private static final long MIX_MULTIPLIER = 0x9E3779B97F4A7C15L;

  private SleepClipRules() {}

  public static String clipFor(
      final Collection<String> rigClips,
      final UUID dogId,
      final long worldTime,
      final boolean baby,
      final boolean undead) {
    return pick(variantsOf(rigClips), dogId, sessionSeed(worldTime, baby, undead));
  }

  public static List<String> variantsOf(final Collection<String> clipNames) {
    return clipNames.stream()
        .filter(name -> DogAnimationKeys.SLEEP.equals(name) || VARIANT.matcher(name).matches())
        .sorted(Comparator.comparingInt(SleepClipRules::variantNumber))
        .toList();
  }

  public static long sessionSeed(final long worldTime, final boolean baby, final boolean undead) {
    return DogSleepWindow.currentSleepWindowEnd(worldTime, baby, undead)
        .orElse(Math.floorDiv(worldTime, DogSleepWindow.DAY_LENGTH_TICKS));
  }

  public static String pick(final List<String> variants, final UUID dogId, final long seed) {
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
