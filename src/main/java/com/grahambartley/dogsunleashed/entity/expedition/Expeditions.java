package com.grahambartley.dogsunleashed.entity.expedition;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

public final class Expeditions extends PersistentState {

  private static final String DATA_NAME = DogsUnleashed.MOD_ID + "_expeditions";

  private final Map<UUID, Expedition> byPet = new LinkedHashMap<>();

  public static Expeditions get(final MinecraftServer server) {
    return server
        .getOverworld()
        .getPersistentStateManager()
        .getOrCreate(
            new PersistentState.Type<>(Expeditions::new, Expeditions::fromNbt, null), DATA_NAME);
  }

  public boolean isAway(final UUID pet) {
    return this.byPet.containsKey(pet);
  }

  public Optional<Expedition> of(final UUID pet) {
    return Optional.ofNullable(this.byPet.get(pet));
  }

  public boolean add(final Expedition expedition) {
    if (this.byPet.putIfAbsent(expedition.pet(), expedition) != null) {
      return false;
    }
    this.markDirty();
    return true;
  }

  public boolean remove(final UUID pet) {
    if (this.byPet.remove(pet) == null) {
      return false;
    }
    this.markDirty();
    return true;
  }

  public List<Expedition> due(final long timeOfDay, final long gameTime) {
    return this.byPet.values().stream()
        .filter(expedition -> expedition.isDue(timeOfDay, gameTime))
        .toList();
  }

  @Override
  public NbtCompound writeNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registryLookup) {
    final NbtList list = new NbtList();
    this.byPet.values().forEach(expedition -> list.add(expedition.toNbt()));
    nbt.put(ModNbtKeys.EXPEDITIONS, list);
    return nbt;
  }

  public static Expeditions fromNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registryLookup) {
    final Expeditions expeditions = new Expeditions();
    for (final NbtElement element : nbt.getList(ModNbtKeys.EXPEDITIONS, NbtElement.COMPOUND_TYPE)) {
      Expedition.fromNbt((NbtCompound) element)
          .ifPresent(expedition -> expeditions.byPet.putIfAbsent(expedition.pet(), expedition));
    }
    return expeditions;
  }
}
