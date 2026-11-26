package com.grahambartley.dogsunleashed.render.pat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogCrownsTest {

  private static final Vec3d DOG = new Vec3d(10.0, 64.0, 10.0);
  private static final Vec3d CROWN = new Vec3d(10.2, 64.6, 9.7);

  @ParameterizedTest(name = "{0} ticks later -> seen {1}")
  @CsvSource({"0, true", "2, true", "3, false"})
  @DisplayName("a crown is trusted only while it is fresh")
  void aCrownIsTrustedWhileFresh(final long later, final boolean seen) {
    final DogCrowns crowns = new DogCrowns();
    crowns.record(1, DOG, CROWN, 100L);
    assertEquals(seen ? Optional.of(CROWN) : Optional.empty(), crowns.of(1, 100L + later));
  }

  @ParameterizedTest(name = "crown {0} blocks off the dog -> kept {1}")
  @CsvSource({"0.5, true", "3.9, true", "4.1, false", "60, false"})
  @DisplayName("a crown drawn somewhere else, like a portrait, is ignored")
  void aCrownAwayFromTheDogIsIgnored(final double offset, final boolean kept) {
    final DogCrowns crowns = new DogCrowns();
    crowns.record(2, DOG, DOG.add(0.0, offset, 0.0), 50L);
    assertEquals(kept, crowns.of(2, 50L).isPresent());
  }

  @ParameterizedTest(name = "next record {0} ticks later -> {1} kept")
  @CsvSource({"1, 2", "2, 2", "3, 1", "500, 1"})
  @DisplayName("crowns of dogs that stop rendering are dropped, not kept for the session")
  void staleCrownsAreDropped(final long later, final int kept) {
    final DogCrowns crowns = new DogCrowns();
    crowns.record(1, Vec3d.ZERO, new Vec3d(0, 1, 0), 100L);
    crowns.record(2, Vec3d.ZERO, new Vec3d(0, 1, 0), 100L + later);
    assertEquals(kept, crowns.size());
  }
}
