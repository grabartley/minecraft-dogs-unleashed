package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public final class WildPackGrudges extends PersistentState {

  private static final String DATA_NAME = DogsUnleashed.MOD_ID + "_wild_pack_grudges";

  private final Map<UUID, Set<UUID>> playersByPack = new HashMap<>();

  public static WildPackGrudges get(final MinecraftServer server) {
    return server
        .getWorld(World.OVERWORLD)
        .getPersistentStateManager()
        .getOrCreate(
            new PersistentState.Type<>(WildPackGrudges::new, WildPackGrudges::fromNbt, null),
            DATA_NAME);
  }

  public boolean holds(final UUID packId, final UUID playerId) {
    return this.playersByPack.getOrDefault(packId, Set.of()).contains(playerId);
  }

  public void record(final UUID packId, final UUID playerId) {
    if (this.playersByPack.computeIfAbsent(packId, id -> new HashSet<>()).add(playerId)) {
      this.markDirty();
    }
  }

  @Override
  public NbtCompound writeNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registryLookup) {
    final NbtList packs = new NbtList();
    this.playersByPack.forEach(
        (packId, players) -> {
          final NbtCompound pack = new NbtCompound();
          pack.putUuid(ModNbtKeys.WILD_PACK_ID, packId);
          final NbtList playerList = new NbtList();
          players.forEach(playerId -> playerList.add(NbtHelper.fromUuid(playerId)));
          pack.put(ModNbtKeys.GRUDGE_PLAYERS, playerList);
          packs.add(pack);
        });
    nbt.put(ModNbtKeys.GRUDGES, packs);
    return nbt;
  }

  public static WildPackGrudges fromNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registryLookup) {
    final WildPackGrudges grudges = new WildPackGrudges();
    for (final NbtElement element : nbt.getList(ModNbtKeys.GRUDGES, NbtElement.COMPOUND_TYPE)) {
      final NbtCompound pack = (NbtCompound) element;
      if (!pack.containsUuid(ModNbtKeys.WILD_PACK_ID)) {
        continue;
      }
      final UUID packId = pack.getUuid(ModNbtKeys.WILD_PACK_ID);
      for (final NbtElement player :
          pack.getList(ModNbtKeys.GRUDGE_PLAYERS, NbtElement.INT_ARRAY_TYPE)) {
        grudges
            .playersByPack
            .computeIfAbsent(packId, id -> new HashSet<>())
            .add(NbtHelper.toUuid(player));
      }
    }
    return grudges;
  }
}
