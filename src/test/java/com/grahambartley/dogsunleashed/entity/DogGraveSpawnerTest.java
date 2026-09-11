package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.math.Vec3i;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DogGraveSpawnerTest {

  private static int ring(final Vec3i offset) {
    return Math.max(Math.abs(offset.getX()), Math.abs(offset.getZ()));
  }

  @Test
  @DisplayName("search offsets contain no duplicates")
  void searchOffsetsContainNoDuplicates() {
    final List<Vec3i> offsets = DogGraveSpawner.searchOffsets();
    final Set<Vec3i> unique = new HashSet<>(offsets);
    assertEquals(offsets.size(), unique.size());
  }

  @Test
  @DisplayName("search offsets start at the death position itself")
  void searchOffsetsStartAtDeathPosition() {
    assertTrue(DogGraveSpawner.searchOffsets().contains(new Vec3i(0, 0, 0)));
    assertEquals(0, ring(DogGraveSpawner.searchOffsets().get(0)));
  }

  @Test
  @DisplayName("search offsets expand outward, never revisiting a closer ring")
  void searchOffsetsExpandOutward() {
    int highestRingSeen = 0;
    for (final Vec3i offset : DogGraveSpawner.searchOffsets()) {
      final int r = ring(offset);
      assertTrue(
          r >= highestRingSeen,
          "offset " + offset + " at ring " + r + " follows ring " + highestRingSeen);
      highestRingSeen = r;
    }
    assertEquals(3, highestRingSeen);
  }

  @Test
  @DisplayName("every offset sits on the perimeter of its own ring")
  void everyOffsetSitsOnItsRingPerimeter() {
    for (final Vec3i offset : DogGraveSpawner.searchOffsets()) {
      final int r = ring(offset);
      assertTrue(
          Math.abs(offset.getX()) == r || Math.abs(offset.getZ()) == r,
          "offset " + offset + " is interior to ring " + r);
    }
  }

  static Stream<Arguments> ringSizes() {
    return Stream.of(
        Arguments.of("centre", 0, 1 * 5),
        Arguments.of("first ring", 1, 8 * 5),
        Arguments.of("second ring", 2, 16 * 5),
        Arguments.of("third ring", 3, 24 * 5));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("ringSizes")
  @DisplayName("each ring contributes its full perimeter across the vertical band")
  void eachRingContributesFullPerimeter(final String label, final int radius, final int expected) {
    final long count =
        DogGraveSpawner.searchOffsets().stream().filter(offset -> ring(offset) == radius).count();
    assertEquals(expected, count);
  }

  @Test
  @DisplayName("search spans two blocks above and below the death position")
  void searchSpansVerticalBand() {
    final List<Vec3i> offsets = DogGraveSpawner.searchOffsets();
    assertEquals(-2, offsets.stream().mapToInt(Vec3i::getY).min().orElseThrow());
    assertEquals(2, offsets.stream().mapToInt(Vec3i::getY).max().orElseThrow());
  }

  @Test
  @DisplayName("search offsets are immutable so callers cannot corrupt the shared order")
  void searchOffsetsAreImmutable() {
    final List<Vec3i> offsets = DogGraveSpawner.searchOffsets();
    assertThrows(UnsupportedOperationException.class, () -> offsets.add(new Vec3i(9, 9, 9)));
  }
}
