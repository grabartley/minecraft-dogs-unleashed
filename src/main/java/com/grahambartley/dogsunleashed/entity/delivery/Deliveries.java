package com.grahambartley.dogsunleashed.entity.delivery;

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

public final class Deliveries extends PersistentState {

  private static final String DATA_NAME = DogsUnleashed.MOD_ID + "_deliveries";

  private final Map<UUID, Delivery> byPet = new LinkedHashMap<>();

  public static Deliveries get(final MinecraftServer server) {
    return server
        .getOverworld()
        .getPersistentStateManager()
        .getOrCreate(
            new PersistentState.Type<>(Deliveries::new, Deliveries::fromNbt, null), DATA_NAME);
  }

  public Optional<Delivery> of(final UUID pet) {
    return Optional.ofNullable(this.byPet.get(pet));
  }

  public boolean isOffWorld(final UUID pet) {
    return this.of(pet).map(delivery -> delivery.leg().offWorld()).orElse(false);
  }

  public boolean isWaitingFor(final UUID pet, final UUID player) {
    return this.of(pet)
        .filter(delivery -> delivery.leg() == DeliveryLeg.WAITING)
        .map(delivery -> delivery.recipient().equals(player))
        .orElse(false);
  }

  public boolean add(final Delivery delivery) {
    if (this.byPet.putIfAbsent(delivery.pet(), delivery) != null) {
      return false;
    }
    this.markDirty();
    return true;
  }

  public void replace(final Delivery delivery) {
    this.byPet.put(delivery.pet(), delivery);
    this.markDirty();
  }

  public boolean remove(final UUID pet) {
    if (this.byPet.remove(pet) == null) {
      return false;
    }
    this.markDirty();
    return true;
  }

  public List<Delivery> all() {
    return List.copyOf(this.byPet.values());
  }

  @Override
  public NbtCompound writeNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registryLookup) {
    final NbtList list = new NbtList();
    this.byPet.values().forEach(delivery -> list.add(delivery.toNbt()));
    nbt.put(ModNbtKeys.DELIVERIES, list);
    return nbt;
  }

  public static Deliveries fromNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registryLookup) {
    final Deliveries deliveries = new Deliveries();
    for (final NbtElement element : nbt.getList(ModNbtKeys.DELIVERIES, NbtElement.COMPOUND_TYPE)) {
      Delivery.fromNbt((NbtCompound) element)
          .ifPresent(delivery -> deliveries.byPet.putIfAbsent(delivery.pet(), delivery));
    }
    return deliveries;
  }
}
