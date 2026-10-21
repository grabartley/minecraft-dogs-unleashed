package com.grahambartley.dogsunleashed.entity.trick;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public final class TrickProgressNbt {

  private TrickProgressNbt() {}

  public static NbtCompound write(final TrickProgress progress) {
    final NbtCompound nbt = new NbtCompound();
    progress.rewards().forEach((trick, count) -> nbt.putInt(trick.serializedId(), count));
    return nbt;
  }

  public static TrickProgress read(final NbtCompound nbt) {
    final Map<DogTrick, Integer> rewards = new EnumMap<>(DogTrick.class);
    for (final String key : nbt.getKeys()) {
      if (nbt.contains(key, NbtElement.NUMBER_TYPE)) {
        DogTrick.fromSerializedId(key).ifPresent(trick -> rewards.put(trick, nbt.getInt(key)));
      }
    }
    return TrickProgress.of(rewards);
  }
}
