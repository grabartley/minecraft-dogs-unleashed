package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class SavedDimensionTest {

  private static final String KEY = "SomeDimension";

  static Stream<RegistryKey<World>> dimensions() {
    return Stream.of(World.OVERWORLD, World.NETHER, World.END);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("dimensions")
  @DisplayName("a written dimension reads back as itself")
  void aWrittenDimensionReadsBackAsItself(final RegistryKey<World> dimension) {
    final NbtCompound nbt = new NbtCompound();
    SavedDimension.write(nbt, KEY, dimension);
    final RegistryKey<World> fallback = dimension.equals(World.END) ? World.OVERWORLD : World.END;
    assertEquals(dimension, SavedDimension.readOr(nbt, KEY, fallback));
  }

  @Test
  @DisplayName("writing no dimension leaves the key out")
  void writingNoDimensionLeavesTheKeyOut() {
    final NbtCompound nbt = new NbtCompound();
    SavedDimension.write(nbt, KEY, null);
    assertFalse(nbt.contains(KEY));
  }

  @Test
  @DisplayName("a save with no dimension falls back to the given one")
  void aSaveWithNoDimensionFallsBackToTheGivenOne() {
    assertEquals(World.NETHER, SavedDimension.readOr(new NbtCompound(), KEY, World.NETHER));
  }

  @ParameterizedTest(name = "\"{0}\"")
  @ValueSource(strings = {"Not A Valid Id", "bad:id:twice", "Upper:Case"})
  @DisplayName("an unreadable dimension id falls back to the given one")
  void anUnreadableDimensionIdFallsBackToTheGivenOne(final String storedId) {
    final NbtCompound nbt = new NbtCompound();
    nbt.putString(KEY, storedId);
    assertEquals(World.END, SavedDimension.readOr(nbt, KEY, World.END));
  }

  @Test
  @DisplayName("a dimension stored as the wrong type falls back to the given one")
  void aDimensionStoredAsTheWrongTypeFallsBackToTheGivenOne() {
    final NbtCompound nbt = new NbtCompound();
    nbt.putInt(KEY, 3);
    assertEquals(World.END, SavedDimension.readOr(nbt, KEY, World.END));
  }

  static Stream<Arguments> placements() {
    return Stream.of(
        Arguments.of(World.OVERWORLD, World.OVERWORLD, true),
        Arguments.of(World.NETHER, World.NETHER, true),
        Arguments.of(World.OVERWORLD, World.NETHER, false),
        Arguments.of(World.NETHER, World.OVERWORLD, false),
        Arguments.of(World.END, World.OVERWORLD, false),
        Arguments.of(null, World.NETHER, true));
  }

  @ParameterizedTest(name = "saved {0}, now in {1} -> {2}")
  @MethodSource("placements")
  @DisplayName("a place counts as here only in its own dimension or when its dimension is unknown")
  void aPlaceCountsAsHereOnlyInItsOwnDimension(
      final @Nullable RegistryKey<World> saved,
      final RegistryKey<World> current,
      final boolean expected) {
    assertEquals(expected, SavedDimension.isHere(saved, current));
  }
}
