package com.grahambartley.pet;

import com.grahambartley.DogsUnleashed;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public final class PetManagerPreferencesState extends PersistentState {

  private static final String DATA_NAME = DogsUnleashed.MOD_ID + "_pet_manager_preferences";

  public record PetManagerPreferences(String breedFilter, String aliveFilter) {

    public static final String DEFAULT_BREED_FILTER = "";
    public static final String DEFAULT_ALIVE_FILTER = "ALIVE";

    public static PetManagerPreferences defaults() {
      return new PetManagerPreferences(DEFAULT_BREED_FILTER, DEFAULT_ALIVE_FILTER);
    }
  }

  private final Map<UUID, PetManagerPreferences> preferencesByPlayer = new HashMap<>();

  public static PetManagerPreferencesState get(final MinecraftServer server) {
    final PersistentStateManager stateManager =
        server.getWorld(World.OVERWORLD).getPersistentStateManager();
    return stateManager.getOrCreate(getType(), DATA_NAME);
  }

  private static PersistentState.Type<PetManagerPreferencesState> getType() {
    return new PersistentState.Type<>(
        PetManagerPreferencesState::new, PetManagerPreferencesState::fromNbt, null);
  }

  public PetManagerPreferences getPreferences(final UUID playerId) {
    return preferencesByPlayer.getOrDefault(playerId, PetManagerPreferences.defaults());
  }

  public void setPreferences(
      final UUID playerId, final String breedFilter, final String aliveFilter) {
    final PetManagerPreferences preferences =
        new PetManagerPreferences(
            breedFilter == null ? PetManagerPreferences.DEFAULT_BREED_FILTER : breedFilter,
            aliveFilter == null || aliveFilter.isEmpty()
                ? PetManagerPreferences.DEFAULT_ALIVE_FILTER
                : aliveFilter);
    preferencesByPlayer.put(playerId, preferences);
    markDirty();
  }

  @Override
  public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    final NbtList preferencesList = new NbtList();
    for (final Map.Entry<UUID, PetManagerPreferences> entry : preferencesByPlayer.entrySet()) {
      final NbtCompound preferenceNbt = new NbtCompound();
      preferenceNbt.putUuid("PlayerId", entry.getKey());
      preferenceNbt.putString("BreedFilter", entry.getValue().breedFilter());
      preferenceNbt.putString("AliveFilter", entry.getValue().aliveFilter());
      preferencesList.add(preferenceNbt);
    }
    nbt.put("PreferencesByPlayer", preferencesList);
    return nbt;
  }

  public static PetManagerPreferencesState fromNbt(
      NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    final PetManagerPreferencesState state = new PetManagerPreferencesState();
    final NbtList preferencesList = nbt.getList("PreferencesByPlayer", NbtElement.COMPOUND_TYPE);
    for (int i = 0; i < preferencesList.size(); i++) {
      final NbtCompound preferenceNbt = preferencesList.getCompound(i);
      state.preferencesByPlayer.put(
          preferenceNbt.getUuid("PlayerId"),
          new PetManagerPreferences(
              preferenceNbt.getString("BreedFilter"), preferenceNbt.getString("AliveFilter")));
    }
    return state;
  }
}
