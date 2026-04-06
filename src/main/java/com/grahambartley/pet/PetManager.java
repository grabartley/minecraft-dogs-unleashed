package com.grahambartley.pet;

import com.grahambartley.DogsUnleashed;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public final class PetManager extends PersistentState {

  private static final String DATA_NAME = DogsUnleashed.MOD_ID + "_pets";

  private final Map<UUID, List<PetData>> petsByOwner = new HashMap<>();

  public PetManager() {}

  public static PetManager get(MinecraftServer server) {
    final PersistentStateManager stateManager =
        server.getWorld(World.OVERWORLD).getPersistentStateManager();
    return stateManager.getOrCreate(getType(), DATA_NAME);
  }

  private static PersistentState.Type<PetManager> getType() {
    return new PersistentState.Type<>(PetManager::new, PetManager::fromNbt, null);
  }

  public void registerPet(PetData petData) {
    petsByOwner.computeIfAbsent(petData.getOwnerId(), k -> new ArrayList<>()).add(petData);
    markDirty();
  }

  public void updatePet(PetData petData) {
    final List<PetData> pets = petsByOwner.get(petData.getOwnerId());
    if (pets != null) {
      for (int i = 0; i < pets.size(); i++) {
        if (pets.get(i).getPetId().equals(petData.getPetId())) {
          pets.set(i, petData);
          markDirty();
          return;
        }
      }
    }
  }

  public PetData getPet(UUID ownerId, UUID petId) {
    final List<PetData> pets = petsByOwner.get(ownerId);
    if (pets != null) {
      for (final PetData pet : pets) {
        if (pet.getPetId().equals(petId)) {
          return pet;
        }
      }
    }
    return null;
  }

  public PetData getPetByEntityId(UUID petId) {
    for (final List<PetData> pets : petsByOwner.values()) {
      for (final PetData pet : pets) {
        if (pet.getPetId().equals(petId)) {
          return pet;
        }
      }
    }
    return null;
  }

  public List<PetData> getPetsByOwner(UUID ownerId) {
    return petsByOwner.getOrDefault(ownerId, new ArrayList<>());
  }

  public List<PetData> getPetsByOwnerFiltered(
      UUID ownerId, String breedFilter, Boolean aliveFilter) {
    return getPetsByOwnerFiltered(ownerId, breedFilter, aliveFilter, "");
  }

  public List<PetData> getPetsByOwnerFiltered(
      UUID ownerId, String breedFilter, Boolean aliveFilter, String searchQuery) {
    List<PetData> pets = getPetsByOwner(ownerId);
    if (breedFilter != null && !breedFilter.isEmpty()) {
      pets =
          pets.stream()
              .filter(p -> p.getBreedType().equals(breedFilter))
              .collect(Collectors.toList());
    }
    if (aliveFilter != null) {
      pets = pets.stream().filter(p -> p.isAlive() == aliveFilter).collect(Collectors.toList());
    }
    if (searchQuery != null && !searchQuery.isEmpty()) {
      final String normalizedQuery = searchQuery.toLowerCase();
      pets =
          pets.stream()
              .filter(p -> p.getName().toLowerCase().contains(normalizedQuery))
              .collect(Collectors.toList());
    }
    return pets;
  }

  public List<PetData> searchPetsByName(UUID ownerId, String nameQuery) {
    final String query = nameQuery.toLowerCase();
    return getPetsByOwner(ownerId).stream()
        .filter(p -> p.getName().toLowerCase().contains(query))
        .collect(Collectors.toList());
  }

  public void markPetDeceased(UUID petId) {
    for (final List<PetData> pets : petsByOwner.values()) {
      for (final PetData pet : pets) {
        if (pet.getPetId().equals(petId)) {
          pet.setAlive(false);
          pet.setHealth(0);
          markDirty();
          return;
        }
      }
    }
  }

  @Override
  public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    final NbtList ownersList = new NbtList();
    for (final Map.Entry<UUID, List<PetData>> entry : petsByOwner.entrySet()) {
      final NbtCompound ownerNbt = new NbtCompound();
      ownerNbt.putUuid("OwnerId", entry.getKey());
      final NbtList petsList = new NbtList();
      for (final PetData pet : entry.getValue()) {
        petsList.add(pet.toNbt());
      }
      ownerNbt.put("Pets", petsList);
      ownersList.add(ownerNbt);
    }
    nbt.put("PetsByOwner", ownersList);
    return nbt;
  }

  public static PetManager fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    final PetManager manager = new PetManager();
    final NbtList ownersList = nbt.getList("PetsByOwner", NbtElement.COMPOUND_TYPE);
    for (int i = 0; i < ownersList.size(); i++) {
      final NbtCompound ownerNbt = ownersList.getCompound(i);
      final UUID ownerId = ownerNbt.getUuid("OwnerId");
      final NbtList petsList = ownerNbt.getList("Pets", NbtElement.COMPOUND_TYPE);
      final List<PetData> pets = new ArrayList<>();
      for (int j = 0; j < petsList.size(); j++) {
        pets.add(PetData.fromNbt(petsList.getCompound(j)));
      }
      manager.petsByOwner.put(ownerId, pets);
    }
    return manager;
  }
}
