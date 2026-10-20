package com.grahambartley.dogsunleashed.entity.burial;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

public final class BoneCaches extends PersistentState {

  private static final String DATA_NAME = DogsUnleashed.MOD_ID + "_bone_caches";

  private final Map<Long, BoneCache> byPos = new HashMap<>();

  public static BoneCaches get(final ServerWorld world) {
    return world
        .getPersistentStateManager()
        .getOrCreate(
            new PersistentState.Type<>(BoneCaches::new, BoneCaches::fromNbt, null), DATA_NAME);
  }

  public Optional<BoneCache> at(final BlockPos pos) {
    return Optional.ofNullable(this.byPos.get(pos.asLong()));
  }

  public boolean add(final BoneCache cache) {
    if (this.byPos.putIfAbsent(cache.pos().asLong(), cache) != null) {
      return false;
    }
    this.markDirty();
    return true;
  }

  public Optional<BoneCache> remove(final BlockPos pos) {
    final BoneCache removed = this.byPos.remove(pos.asLong());
    if (removed != null) {
      this.markDirty();
    }
    return Optional.ofNullable(removed);
  }

  public List<BoneCache> buriedBy(final UUID dog) {
    return this.byPos.values().stream().filter(cache -> cache.dog().equals(dog)).toList();
  }

  public Optional<BoneCache> nearestDue(
      final UUID dog, final BlockPos from, final double range, final long now) {
    final double rangeSquared = range * range;
    return this.byPos.values().stream()
        .filter(cache -> cache.dog().equals(dog))
        .filter(cache -> BoneCacheRules.isDue(cache.buriedAt(), now))
        .filter(cache -> cache.pos().getSquaredDistance(from) <= rangeSquared)
        .min(Comparator.comparingDouble(cache -> cache.pos().getSquaredDistance(from)));
  }

  @Override
  public NbtCompound writeNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registryLookup) {
    final NbtList caches = new NbtList();
    this.byPos.values().forEach(cache -> caches.add(cache.toNbt()));
    nbt.put(ModNbtKeys.BONE_CACHES, caches);
    return nbt;
  }

  public static BoneCaches fromNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registryLookup) {
    final BoneCaches caches = new BoneCaches();
    for (final NbtElement element : nbt.getList(ModNbtKeys.BONE_CACHES, NbtElement.COMPOUND_TYPE)) {
      BoneCache.fromNbt((NbtCompound) element)
          .ifPresent(cache -> caches.byPos.putIfAbsent(cache.pos().asLong(), cache));
    }
    return caches;
  }
}
