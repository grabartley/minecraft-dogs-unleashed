package com.grahambartley.dogsunleashed.network.handler;

import static com.grahambartley.dogsunleashed.network.PacketLimits.CONNECTIONS_LIST_MAX_SIZE;

import com.grahambartley.dogsunleashed.network.payload.ConnectionDogSyncData;
import com.grahambartley.dogsunleashed.network.payload.PetSyncData;
import com.grahambartley.dogsunleashed.network.payload.RequestDogConnectionsPayload;
import com.grahambartley.dogsunleashed.network.payload.SyncDogConnectionsPayload;
import com.grahambartley.dogsunleashed.pet.DirectConnections;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class DogConnectionsNetworkHandler {

  private DogConnectionsNetworkHandler() {}

  public static void handleRequestDogConnections(
      final RequestDogConnectionsPayload payload, final ServerPlayNetworking.Context context) {
    final ServerPlayerEntity player = context.player();
    final ServerWorld world = player.getServerWorld();

    world
        .getServer()
        .execute(
            () -> {
              final MinecraftServer server = world.getServer();
              final PetManager petManager = PetManager.get(server);
              final DirectConnections connections =
                  petManager.getDirectConnections(payload.dogId());
              if (connections == null) {
                return;
              }
              final boolean requesterOwnsDog =
                  connections.self().getOwnerId().equals(player.getUuid());
              if (!requesterOwnsDog
                  && !petManager.isConnectedToOwnedPet(player.getUuid(), payload.dogId())) {
                return;
              }
              ServerPlayNetworking.send(
                  player, buildConnectionsPayload(server, petManager, connections));
            });
  }

  private static SyncDogConnectionsPayload buildConnectionsPayload(
      final MinecraftServer server,
      final PetManager petManager,
      final DirectConnections connections) {
    final List<PetData> parents = capConnectionList(connections.parents());
    final List<PetData> mates = capConnectionList(connections.mates());
    final List<PetData> siblings = capConnectionList(connections.siblings());
    final List<PetData> children = capConnectionList(connections.children());
    final boolean truncated =
        parents.size() < connections.parents().size()
            || mates.size() < connections.mates().size()
            || siblings.size() < connections.siblings().size()
            || children.size() < connections.children().size();

    final Set<PetData> allPets = new LinkedHashSet<>();
    allPets.add(connections.self());
    allPets.addAll(parents);
    allPets.addAll(mates);
    allPets.addAll(siblings);
    allPets.addAll(children);
    PetSyncRefresher.refreshAndProject(server, petManager, List.copyOf(allPets));

    return new SyncDogConnectionsPayload(
        toConnectionSyncData(server, connections.self()),
        petManager.getBreedComposition(
            connections.self().getPetId(), connections.self().getBreed()),
        toConnectionSyncDataList(server, parents),
        toConnectionSyncDataList(server, mates),
        toConnectionSyncDataList(server, siblings),
        toConnectionSyncDataList(server, children),
        truncated);
  }

  private static List<PetData> capConnectionList(final List<PetData> pets) {
    return pets.size() > CONNECTIONS_LIST_MAX_SIZE
        ? pets.subList(0, CONNECTIONS_LIST_MAX_SIZE)
        : pets;
  }

  private static List<ConnectionDogSyncData> toConnectionSyncDataList(
      final MinecraftServer server, final List<PetData> pets) {
    return pets.stream().map(pet -> toConnectionSyncData(server, pet)).toList();
  }

  private static ConnectionDogSyncData toConnectionSyncData(
      final MinecraftServer server, final PetData pet) {
    return new ConnectionDogSyncData(
        PetSyncData.from(pet),
        pet.getOwnerId().toString(),
        OwnerNameResolver.resolve(server, pet.getOwnerId()));
  }
}
