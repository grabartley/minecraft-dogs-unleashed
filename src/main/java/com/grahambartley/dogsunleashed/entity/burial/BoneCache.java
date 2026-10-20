package com.grahambartley.dogsunleashed.entity.burial;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;

public record BoneCache(BlockPos pos, UUID owner, UUID dog, long buriedAt) {

  public NbtCompound toNbt() {
    final NbtCompound nbt = new NbtCompound();
    nbt.putLong(ModNbtKeys.BONE_CACHE_POS, this.pos.asLong());
    nbt.putUuid(ModNbtKeys.BONE_CACHE_OWNER, this.owner);
    nbt.putUuid(ModNbtKeys.BONE_CACHE_DOG, this.dog);
    nbt.putLong(ModNbtKeys.BONE_CACHE_BURIED_AT, this.buriedAt);
    return nbt;
  }

  public static Optional<BoneCache> fromNbt(final NbtCompound nbt) {
    if (!nbt.contains(ModNbtKeys.BONE_CACHE_POS, NbtElement.LONG_TYPE)
        || !nbt.containsUuid(ModNbtKeys.BONE_CACHE_OWNER)
        || !nbt.containsUuid(ModNbtKeys.BONE_CACHE_DOG)
        || !nbt.contains(ModNbtKeys.BONE_CACHE_BURIED_AT, NbtElement.LONG_TYPE)) {
      return Optional.empty();
    }
    return Optional.of(
        new BoneCache(
            BlockPos.fromLong(nbt.getLong(ModNbtKeys.BONE_CACHE_POS)),
            nbt.getUuid(ModNbtKeys.BONE_CACHE_OWNER),
            nbt.getUuid(ModNbtKeys.BONE_CACHE_DOG),
            nbt.getLong(ModNbtKeys.BONE_CACHE_BURIED_AT)));
  }
}
