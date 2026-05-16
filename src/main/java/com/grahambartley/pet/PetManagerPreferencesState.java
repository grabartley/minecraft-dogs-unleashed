package com.grahambartley.pet;

import com.grahambartley.DogsUnleashed;
import com.grahambartley.ModNbtKeys;
import com.grahambartley.entity.UnleashedDogBreed;
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

  public record PetManagerPreferences(UnleashedDogBreed breedFilter, PetAliveFilter aliveFilter) {

    public static PetManagerPreferences defaults() {
      return new PetManagerPreferences(null, PetAliveFilter.ALIVE);
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
      final UUID playerId, final UnleashedDogBreed breedFilter, final PetAliveFilter aliveFilter) {
    final PetManagerPreferences preferences =
        new PetManagerPreferences(
            breedFilter, aliveFilter == null ? PetAliveFilter.ALIVE : aliveFilter);
    preferencesByPlayer.put(playerId, preferences);
    markDirty();
  }

  @Override
  public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    final NbtList preferencesList = new NbtList();
    for (final Map.Entry<UUID, PetManagerPreferences> entry : preferencesByPlayer.entrySet()) {
      final NbtCompound preferenceNbt = new NbtCompound();
      preferenceNbt.putUuid(ModNbtKeys.OWNER_ID, entry.getKey());
      if (entry.getValue().breedFilter() != null) {
        preferenceNbt.putString(
            ModNbtKeys.BREED_FILTER, entry.getValue().breedFilter().serializedId());
      }
      preferenceNbt.putString(
          ModNbtKeys.ALIVE_FILTER, entry.getValue().aliveFilter().serializedName());
      preferencesList.add(preferenceNbt);
    }
    nbt.put(ModNbtKeys.PREFERENCES_BY_PLAYER, preferencesList);
    return nbt;
  }

  public static PetManagerPreferencesState fromNbt(
      NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    final PetManagerPreferencesState state = new PetManagerPreferencesState();
    final NbtList preferencesList =
        nbt.getList(ModNbtKeys.PREFERENCES_BY_PLAYER, NbtElement.COMPOUND_TYPE);
    for (int i = 0; i < preferencesList.size(); i++) {
      final NbtCompound preferenceNbt = preferencesList.getCompound(i);
      state.preferencesByPlayer.put(
          preferenceNbt.getUuid(ModNbtKeys.OWNER_ID),
          new PetManagerPreferences(
              UnleashedDogBreed.fromSerializedIdOrNull(
                  preferenceNbt.getString(ModNbtKeys.BREED_FILTER)),
              PetAliveFilter.fromSerializedName(preferenceNbt.getString(ModNbtKeys.ALIVE_FILTER))));
    }
    return state;
  }
}
