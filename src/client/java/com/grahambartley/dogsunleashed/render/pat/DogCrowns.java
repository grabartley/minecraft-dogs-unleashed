package com.grahambartley.dogsunleashed.render.pat;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.math.Vec3d;

public final class DogCrowns {

  public static final DogCrowns SHARED = new DogCrowns();
  static final long FRESH_TICKS = 2L;
  static final double MAX_DRIFT_BLOCKS = 4.0;

  private record Seen(Vec3d crown, long worldTime) {}

  private final Map<Integer, Seen> byDog = new HashMap<>();

  public void record(final int dogId, final Vec3d dogPos, final Vec3d crown, final long worldTime) {
    this.byDog.values().removeIf(seen -> worldTime - seen.worldTime() > FRESH_TICKS);
    if (crown.squaredDistanceTo(dogPos) <= MAX_DRIFT_BLOCKS * MAX_DRIFT_BLOCKS) {
      this.byDog.put(dogId, new Seen(crown, worldTime));
    }
  }

  public Optional<Vec3d> of(final int dogId, final long worldTime) {
    final Seen seen = this.byDog.get(dogId);
    if (seen == null || worldTime - seen.worldTime() > FRESH_TICKS) {
      return Optional.empty();
    }
    return Optional.of(seen.crown());
  }

  int size() {
    return this.byDog.size();
  }

  public void clear() {
    this.byDog.clear();
  }
}
