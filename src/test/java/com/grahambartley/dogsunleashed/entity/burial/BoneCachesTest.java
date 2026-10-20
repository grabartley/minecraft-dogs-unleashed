package com.grahambartley.dogsunleashed.entity.burial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BoneCachesTest {

  private static final long DAY = BoneCacheRules.TICKS_PER_DAY;
  private static final UUID OWNER = UUID.randomUUID();
  private static final UUID DOG = UUID.randomUUID();
  private static final UUID OTHER_DOG = UUID.randomUUID();

  private static BoneCache cache(final BlockPos pos, final UUID dog, final long buriedAt) {
    return new BoneCache(pos, OWNER, dog, buriedAt);
  }

  @Test
  @DisplayName("one spot holds one cache, and removing it hands it back once")
  void oneCachePerSpot() {
    final BoneCaches caches = new BoneCaches();
    final BlockPos spot = new BlockPos(1, 64, 1);
    assertTrue(caches.add(cache(spot, DOG, 0)));
    assertFalse(caches.add(cache(spot, OTHER_DOG, 0)));
    assertEquals(DOG, caches.at(spot).orElseThrow().dog());
    assertTrue(caches.isDirty());
    assertTrue(caches.remove(spot).isPresent());
    assertTrue(caches.remove(spot).isEmpty());
    assertTrue(caches.at(spot).isEmpty());
  }

  @Test
  @DisplayName("the dog only digs up its own due caches in range, nearest first")
  void nearestDueOwnCache() {
    final BoneCaches caches = new BoneCaches();
    final BlockPos dog = new BlockPos(0, 64, 0);
    caches.add(cache(new BlockPos(10, 64, 0), DOG, 0));
    caches.add(cache(new BlockPos(4, 64, 0), DOG, 0));
    caches.add(cache(new BlockPos(2, 64, 0), OTHER_DOG, 0));
    caches.add(cache(new BlockPos(1, 64, 0), DOG, DAY));
    caches.add(cache(new BlockPos(60, 64, 0), DOG, 0));

    assertEquals(
        Optional.of(new BlockPos(4, 64, 0)),
        caches.nearestDue(DOG, dog, 48.0, 3 * DAY).map(BoneCache::pos));
    assertTrue(caches.nearestDue(DOG, dog, 48.0, 2 * DAY).isEmpty());
  }

  @Test
  @DisplayName("caches survive a save and load, and broken entries are skipped")
  void roundTripsAndSkipsBrokenEntries() {
    final BoneCaches caches = new BoneCaches();
    final BoneCache first = cache(new BlockPos(3, 70, -2), DOG, 5);
    final BoneCache second = cache(new BlockPos(-8, 12, 30), OTHER_DOG, 9);
    caches.add(first);
    caches.add(second);

    final NbtCompound saved = caches.writeNbt(new NbtCompound(), null);
    ((NbtList) saved.get(ModNbtKeys.BONE_CACHES)).add(new NbtCompound());
    final BoneCaches loaded = BoneCaches.fromNbt(saved, null);

    assertEquals(List.of(first), loaded.buriedBy(DOG));
    assertEquals(List.of(second), loaded.buriedBy(OTHER_DOG));
  }
}
