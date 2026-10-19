package com.grahambartley.dogsunleashed.entity;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class SavedDimension {

  private SavedDimension() {}

  public static void write(
      final NbtCompound nbt, final String key, final @Nullable RegistryKey<World> dimension) {
    if (dimension != null) {
      nbt.putString(key, dimension.getValue().toString());
    }
  }

  public static RegistryKey<World> readOr(
      final NbtCompound nbt, final String key, final RegistryKey<World> fallback) {
    if (!nbt.contains(key, NbtElement.STRING_TYPE)) {
      return fallback;
    }
    final Identifier id = Identifier.tryParse(nbt.getString(key));
    return id == null ? fallback : RegistryKey.of(RegistryKeys.WORLD, id);
  }

  public static boolean isHere(
      final @Nullable RegistryKey<World> saved, final RegistryKey<World> current) {
    return saved == null || saved.equals(current);
  }
}
