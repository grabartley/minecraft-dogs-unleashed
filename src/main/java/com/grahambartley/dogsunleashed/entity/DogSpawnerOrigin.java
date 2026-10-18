package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import net.minecraft.nbt.NbtCompound;

public final class DogSpawnerOrigin extends DogComponent {

  private boolean fromSpawner;

  public boolean isFromSpawner() {
    return this.fromSpawner;
  }

  public void setFromSpawner(final boolean fromSpawner) {
    this.fromSpawner = fromSpawner;
  }

  @Override
  void writeNbt(final NbtCompound nbt) {
    nbt.putBoolean(ModNbtKeys.SPAWNED_BY_DOG_SPAWNER, this.fromSpawner);
  }

  @Override
  void readNbt(final NbtCompound nbt) {
    if (nbt.contains(ModNbtKeys.SPAWNED_BY_DOG_SPAWNER)) {
      this.fromSpawner = nbt.getBoolean(ModNbtKeys.SPAWNED_BY_DOG_SPAWNER);
    }
  }
}
