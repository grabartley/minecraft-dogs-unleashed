package com.grahambartley.dogsunleashed.pet;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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

  // Lineage searches stay bounded even on pathological worlds; far above any legitimate family.
  private static final int LINEAGE_SEARCH_VISIT_LIMIT = 512;

  private final Map<UUID, List<PetData>> petsByOwner = new HashMap<>();
  private final Map<UUID, PetData> petsById = new HashMap<>();
  private final Map<UUID, Set<UUID>> childIdsByParentId = new HashMap<>();

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
    index(petData);
    markDirty();
  }

  public void updatePet(PetData petData) {
    final List<PetData> pets = petsByOwner.get(petData.getOwnerId());
    if (pets != null) {
      for (int i = 0; i < pets.size(); i++) {
        if (pets.get(i).getPetId().equals(petData.getPetId())) {
          pets.set(i, petData);
          index(petData);
          markDirty();
          return;
        }
      }
    }
  }

  // Idempotent, and parents are set-once, so re-indexing on every update only ever adds the
  // child edges a legacy-record backfill just recovered.
  private void index(final PetData petData) {
    petsById.put(petData.getPetId(), petData);
    for (final UUID parentId : parentIdsOf(petData)) {
      childIdsByParentId
          .computeIfAbsent(parentId, k -> new LinkedHashSet<>())
          .add(petData.getPetId());
    }
  }

  private static List<UUID> parentIdsOf(final PetData petData) {
    final List<UUID> parentIds = new ArrayList<>(2);
    if (petData.getParentAId() != null) {
      parentIds.add(petData.getParentAId());
    }
    if (petData.getParentBId() != null) {
      parentIds.add(petData.getParentBId());
    }
    return parentIds;
  }

  public PetData getPet(UUID ownerId, UUID petId) {
    final PetData pet = petsById.get(petId);
    return pet != null && pet.getOwnerId().equals(ownerId) ? pet : null;
  }

  public PetData getPetByEntityId(UUID petId) {
    return petsById.get(petId);
  }

  public List<BreedComposition.BreedShare> getBreedComposition(
      UUID dogId, UnleashedDogBreed fallbackBreed) {
    return BreedComposition.compute(dogId, fallbackBreed, petsById::get);
  }

  /**
   * Resolves one dog's immediate family from the pet records, or {@code null} for an unknown dog.
   * Mates are co-parents of at least one shared child, siblings share at least one parent.
   */
  public DirectConnections getDirectConnections(UUID dogId) {
    final PetData self = petsById.get(dogId);
    if (self == null) {
      return null;
    }

    final List<UUID> parentIds = parentIdsOf(self);
    final Set<UUID> childIds = childIdsByParentId.getOrDefault(dogId, Set.of());

    final Set<UUID> mateIds = new LinkedHashSet<>();
    for (final UUID childId : childIds) {
      final PetData child = petsById.get(childId);
      if (child != null) {
        for (final UUID coParentId : parentIdsOf(child)) {
          if (!coParentId.equals(dogId)) {
            mateIds.add(coParentId);
          }
        }
      }
    }

    final Set<UUID> siblingIds = new LinkedHashSet<>();
    for (final UUID parentId : parentIds) {
      for (final UUID siblingId : childIdsByParentId.getOrDefault(parentId, Set.of())) {
        if (!siblingId.equals(dogId)) {
          siblingIds.add(siblingId);
        }
      }
    }

    return new DirectConnections(
        self, resolve(parentIds), resolve(mateIds), resolve(siblingIds), resolve(childIds));
  }

  private List<PetData> resolve(final Iterable<UUID> petIds) {
    final List<PetData> pets = new ArrayList<>();
    for (final UUID petId : petIds) {
      final PetData pet = petsById.get(petId);
      if (pet != null) {
        pets.add(pet);
      }
    }
    return pets;
  }

  /**
   * Walks the lineage graph (parent and child edges, both directions) outward from {@code dogId}
   * and reports whether it reaches any pet owned by {@code ownerId}. This is the authorization
   * check for family tree browsing: a player may inspect exactly the dogs connected to their own.
   */
  public boolean isConnectedToOwnedPet(UUID ownerId, UUID dogId) {
    final Set<UUID> visited = new HashSet<>();
    final Deque<UUID> queue = new ArrayDeque<>();
    visited.add(dogId);
    queue.add(dogId);
    while (!queue.isEmpty() && visited.size() <= LINEAGE_SEARCH_VISIT_LIMIT) {
      final PetData pet = petsById.get(queue.poll());
      if (pet == null) {
        continue;
      }
      if (pet.getOwnerId().equals(ownerId)) {
        return true;
      }
      for (final UUID parentId : parentIdsOf(pet)) {
        if (visited.add(parentId)) {
          queue.add(parentId);
        }
      }
      for (final UUID childId : childIdsByParentId.getOrDefault(pet.getPetId(), Set.of())) {
        if (visited.add(childId)) {
          queue.add(childId);
        }
      }
    }
    return false;
  }

  public List<PetData> getPetsByOwner(UUID ownerId) {
    return petsByOwner.getOrDefault(ownerId, List.of());
  }

  public List<PetData> getPetsByOwnerFiltered(
      UUID ownerId, UnleashedDogBreed breedFilter, PetAliveFilter aliveFilter, String searchQuery) {
    List<PetData> pets = getPetsByOwner(ownerId);
    if (breedFilter != null) {
      pets = pets.stream().filter(p -> p.getBreed() == breedFilter).toList();
    }
    if (aliveFilter != null && aliveFilter != PetAliveFilter.ALL) {
      pets = pets.stream().filter(p -> aliveFilter.appliesTo(p.isAlive())).toList();
    }
    if (searchQuery != null && !searchQuery.isEmpty()) {
      final String normalizedQuery = searchQuery.toLowerCase(Locale.ROOT);
      pets =
          pets.stream()
              .filter(p -> p.getName().toLowerCase(Locale.ROOT).contains(normalizedQuery))
              .toList();
    }
    return pets;
  }

  public void markPetDeceased(UUID petId) {
    final PetData pet = petsById.get(petId);
    if (pet != null) {
      pet.setAlive(false);
      pet.setHealth(0);
      markDirty();
    }
  }

  @Override
  public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    final NbtList ownersList = new NbtList();
    for (final Map.Entry<UUID, List<PetData>> entry : petsByOwner.entrySet()) {
      final NbtCompound ownerNbt = new NbtCompound();
      ownerNbt.putUuid(ModNbtKeys.OWNER_ID, entry.getKey());
      final NbtList petsList = new NbtList();
      for (final PetData pet : entry.getValue()) {
        petsList.add(pet.toNbt());
      }
      ownerNbt.put(ModNbtKeys.PETS, petsList);
      ownersList.add(ownerNbt);
    }
    nbt.put(ModNbtKeys.PETS_BY_OWNER, ownersList);
    return nbt;
  }

  public static PetManager fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    final PetManager manager = new PetManager();
    final NbtList ownersList = nbt.getList(ModNbtKeys.PETS_BY_OWNER, NbtElement.COMPOUND_TYPE);
    for (int i = 0; i < ownersList.size(); i++) {
      final NbtCompound ownerNbt = ownersList.getCompound(i);
      final UUID ownerId = ownerNbt.getUuid(ModNbtKeys.OWNER_ID);
      final NbtList petsList = ownerNbt.getList(ModNbtKeys.PETS, NbtElement.COMPOUND_TYPE);
      final List<PetData> pets = new ArrayList<>();
      for (int j = 0; j < petsList.size(); j++) {
        final PetData pet = PetData.fromNbt(petsList.getCompound(j));
        pets.add(pet);
        manager.index(pet);
      }
      manager.petsByOwner.put(ownerId, pets);
    }
    return manager;
  }
}
