package com.grahambartley.dogsunleashed.entity;

import net.minecraft.nbt.NbtCompound;

public abstract class DogComponent {

  DogComponent() {}

  void tick() {}

  void writeNbt(final NbtCompound nbt) {}

  void readNbt(final NbtCompound nbt) {}
}
