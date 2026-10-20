package com.grahambartley.dogsunleashed.entity.burial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class BoneCacheTest {

  private static final BoneCache CACHE =
      new BoneCache(new BlockPos(12, -40, 7), UUID.randomUUID(), UUID.randomUUID(), 48_000L);

  @Test
  @DisplayName("a cache survives a save and load")
  void roundTrips() {
    assertEquals(Optional.of(CACHE), BoneCache.fromNbt(CACHE.toNbt()));
  }

  static Stream<String> requiredKeys() {
    return Stream.of(
        ModNbtKeys.BONE_CACHE_POS,
        ModNbtKeys.BONE_CACHE_OWNER,
        ModNbtKeys.BONE_CACHE_DOG,
        ModNbtKeys.BONE_CACHE_BURIED_AT);
  }

  @ParameterizedTest(name = "missing {0}")
  @MethodSource("requiredKeys")
  @DisplayName("a damaged entry loads as nothing instead of throwing")
  void damagedEntryIsSkipped(final String key) {
    final NbtCompound nbt = CACHE.toNbt();
    nbt.remove(key);
    assertTrue(BoneCache.fromNbt(nbt).isEmpty());
  }
}
