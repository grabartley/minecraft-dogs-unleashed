package com.grahambartley.dogsunleashed.network.handler;

import static com.grahambartley.dogsunleashed.network.PacketLimits.CONNECTIONS_LIST_MAX_SIZE;

import com.grahambartley.dogsunleashed.entity.away.AwayDogs;
import com.grahambartley.dogsunleashed.network.payload.ConnectionDogSyncData;
import com.grahambartley.dogsunleashed.network.payload.GeneSyncData;
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

public final class DogConnectionsNetworkHandler {

  private DogConnectionsNetworkHandler() {}

  public static void handleRequestDogConnections(
      final RequestDogConnectionsPayload payload, final ServerPlayNetworking.Context context) {
    final ServerPlayerEntity player = context.player();
    final MinecraftServer server = player.getServer();
    final PetManager petManager = PetManager.get(server);
    final DirectConnections connections = petManager.getDirectConnections(payload.dogId());
    if (connections == null) {
      return;
    }
    final boolean requesterOwnsDog = connections.self().getOwnerId().equals(player.getUuid());
    if (!requesterOwnsDog && !petManager.isConnectedToOwnedPet(player.getUuid(), payload.dogId())) {
      return;
    }
    ServerPlayNetworking.send(player, buildConnectionsPayload(server, petManager, connections));
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
        toConnectionSyncData(server, petManager, connections.self()),
        petManager.getBreedComposition(
            connections.self().getPetId(), connections.self().getBreed()),
        toConnectionSyncDataList(server, petManager, parents),
        toConnectionSyncDataList(server, petManager, mates),
        toConnectionSyncDataList(server, petManager, siblings),
        toConnectionSyncDataList(server, petManager, children),
        truncated);
  }

  private static List<PetData> capConnectionList(final List<PetData> pets) {
    return pets.size() > CONNECTIONS_LIST_MAX_SIZE
        ? pets.subList(0, CONNECTIONS_LIST_MAX_SIZE)
        : pets;
  }

  private static List<ConnectionDogSyncData> toConnectionSyncDataList(
      final MinecraftServer server, final PetManager petManager, final List<PetData> pets) {
    return pets.stream().map(pet -> toConnectionSyncData(server, petManager, pet)).toList();
  }

  private static ConnectionDogSyncData toConnectionSyncData(
      final MinecraftServer server, final PetManager petManager, final PetData pet) {
    final boolean carriedGenesRevealed = petManager.hasChildren(pet.getPetId());
    return new ConnectionDogSyncData(
        PetSyncData.from(pet, AwayDogs.statusOf(server, pet.getPetId())),
        pet.getOwnerId().toString(),
        OwnerNameResolver.resolve(server, pet.getOwnerId()),
        GeneSyncData.visibleGenes(pet.getGenes(), pet.getRigSourceBreed(), carriedGenesRevealed),
        carriedGenesRevealed);
  }
}
